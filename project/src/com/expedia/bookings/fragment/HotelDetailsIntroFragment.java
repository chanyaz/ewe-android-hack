package com.expedia.bookings.fragment;

import java.util.List;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.UserReviewsListActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelDescription;
import com.expedia.bookings.data.HotelDescription.DescriptionSection;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.expedia.bookings.utils.DbPropertyHelper;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class HotelDetailsIntroFragment extends Fragment {

	private static final int ROOMS_LEFT_CUTOFF = 5;

	private static final int INTRO_PARAGRAPH_CUTOFF = 90;

	public static HotelDetailsIntroFragment newInstance() {
		return new HotelDetailsIntroFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_details_intro, container, false);
		populateViews(view);
		return view;
	}

	public void populateViews() {
		populateViews(getView());
	}

	private void populateViews(View view) {
		populateBannerSection(view, DbPropertyHelper.getBestRateProperty());
		populateBannerSection(view, DbPropertyHelper.getBestReviewsProperty(),
				Db.getSelectedReviewsStatisticsResponse());
		populateIntroParagraph(view, DbPropertyHelper.getBestDescriptionProperty());
	}

	private void populateBannerSection(View view, Property property) {
		Rate rate = property.getLowestRate();

		// Sale banner
		TextView saleBannerTextView = Ui.findView(view, R.id.sale_banner_text_view);
		TextView promoTextView = Ui.findView(view, R.id.promo_text_view);
		if (rate.isSaleTenPercentOrBetter()) {
			saleBannerTextView.setVisibility(View.VISIBLE);
			saleBannerTextView.setText(getString(R.string.minus_x_percent, rate.getSavingsPercent() * 100));
		}
		else {
			saleBannerTextView.setVisibility(View.GONE);
		}

		// Promo text, i.e. "Mobile Exclusive!" or "Tonight Only!"
		if (property.isLowestRateMobileExclusive()) {
			promoTextView.setText(getString(R.string.mobile_exclusive));
			promoTextView.setVisibility(View.VISIBLE);
		}
		else if (property.isLowestRateTonightOnly()) {
			promoTextView.setText(getString(R.string.tonight_only));
			promoTextView.setVisibility(View.VISIBLE);
		}
		else {
			promoTextView.setVisibility(View.GONE);

		}

		// "<strike>$400</strike>" (if it's on sale)
		TextView strikethroughTextView = Ui.findView(view, R.id.strikethrough_price_text_view);
		if (rate.isOnSale()) {
			strikethroughTextView.setText(Html.fromHtml(
					getString(R.string.strike_template, StrUtils.formatHotelPrice(rate.getDisplayBaseRate())), null,
					new StrikethroughTagHandler()));
			strikethroughTextView.setVisibility(View.VISIBLE);
		}
		else {
			strikethroughTextView.setVisibility(View.GONE);
		}

		// Rate
		TextView rateTextView = Ui.findView(view, R.id.rate_text_view);
		rateTextView.setText(StrUtils.formatHotelPrice(rate.getDisplayRate()));
	}

	// Reviews
	private void populateBannerSection(View view, Property property, ReviewsStatisticsResponse statistics) {

		Resources resources = getResources();
		View reviewsLayout = Ui.findView(view, R.id.user_review_layout);
		TextView reviewsTextView = Ui.findView(view, R.id.user_rating_text_view);
		View verticalSep = Ui.findView(view, R.id.vertical_sep);
		TextView bannerTextView = Ui.findView(view, R.id.banner_message_text_view);
		RatingBar userRatingBar = Ui.findView(view, R.id.user_rating_bar);

		if (statistics == null) {
			reviewsLayout.setVisibility(View.INVISIBLE);
			bannerTextView.setVisibility(View.INVISIBLE);
			verticalSep.setVisibility(View.INVISIBLE);
			return;
		}

		reviewsLayout.setVisibility(View.VISIBLE);
		bannerTextView.setVisibility(View.VISIBLE);
		verticalSep.setVisibility(View.VISIBLE);

		// Reviews
		int numReviews = statistics.getTotalReviewCount();
		float percentRecommend = numReviews == 0 ? 0f : statistics.getRecommendedCount() * 100f / numReviews;
		float userRating = (float) statistics.getAverageOverallRating();

		reviewsTextView.setText(resources.getQuantityString(R.plurals.number_of_reviews, numReviews, numReviews));

		OnClickListener onReviewsClick = (!property.hasExpediaReviews()) ? null : new OnClickListener() {
			public synchronized void onClick(final View v) {
				Intent newIntent = new Intent(getActivity(), UserReviewsListActivity.class);
				newIntent.fillIn(getActivity().getIntent(), 0);
				startActivity(newIntent);
			}
		};
		reviewsLayout.setOnClickListener(onReviewsClick);

		// User Rating Bar
		userRatingBar.setRating(0f);
		ObjectAnimator animRating = ObjectAnimator.ofFloat(userRatingBar, "rating", userRating);

		// Banner messages
		int roomsLeft = property.getRoomsLeftAtThisRate();

		// xx booked in the past x hours
		/*if (TODO: xx booked in the past x hours) {
			bannerTextView.setText(TODO);
			bannerTextView.setVisibility(View.VISIBLE);
			bannerTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_urgency_clock, 0, 0, 0);
		}*/
		// Only xx rooms left
		/*else*/if (roomsLeft > 0 && roomsLeft <= ROOMS_LEFT_CUTOFF) {
			bannerTextView.setText(resources.getQuantityString(R.plurals.num_rooms_left, roomsLeft, roomsLeft));
			bannerTextView.setVisibility(View.VISIBLE);
			bannerTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_urgency_clock, 0, 0, 0);
		}

		// Special case if no urgency and no recommendations: hide this while banner section.
		else if (percentRecommend == 0 && numReviews == 0) {
			view.findViewById(R.id.reviews_summary_layout).setVisibility(View.GONE);
			view.findViewById(R.id.horizontal_sep).setVisibility(View.GONE);
			return;
		}

		// xx% recommend this hotel
		else {
			bannerTextView.setText(resources.getString(R.string.x_percent_guests_recommend, percentRecommend));
			bannerTextView.setVisibility(View.VISIBLE);
			bannerTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_urgency_users, 0, 0, 0);
		}

		// Please to be doing all the animations.
		ObjectAnimator anim1 = ObjectAnimator.ofFloat(reviewsLayout, "alpha", 0f, 1f);
		ObjectAnimator anim2 = ObjectAnimator.ofFloat(bannerTextView, "alpha", 0f, 1f);
		ObjectAnimator anim3 = ObjectAnimator.ofFloat(verticalSep, "alpha", 0f, 1f);
		AnimatorSet s = new AnimatorSet();
		s.play(anim1).with(anim2).with(anim3).before(animRating);
		s.start();
	}

	private void populateIntroParagraph(View view, Property property) {
		String unparsedDescriptionText = property.getDescriptionText();

		HotelDescription.SectionStrings.initSectionStrings(getActivity());
		HotelDescription hotelDescription = new HotelDescription(getActivity());

		hotelDescription.parseDescription(unparsedDescriptionText);
		List<DescriptionSection> sections = hotelDescription.getSections();

		final TextView titleView = Ui.findView(view, R.id.title_text);
		final TextView bodyView = Ui.findView(view, R.id.body_text);

		CharSequence title, body;
		if (sections != null && sections.size() >= 1) {
			title = Html.fromHtml(sections.get(0).title);
			body = Html.fromHtml(sections.get(0).description);
		}
		else {
			title = Html.fromHtml("");
			body = Html.fromHtml(unparsedDescriptionText);
		}

		// Add "read more" button if the intro paragraph is too long
		if (body.length() > INTRO_PARAGRAPH_CUTOFF) {
			final CharSequence untruncated = body;
			final View readMoreView = Ui.findView(view, R.id.read_more);
			final View fadeOverlay = Ui.findView(view, R.id.body_text_fade_bottom);
			readMoreView.setVisibility(View.VISIBLE);
			fadeOverlay.setVisibility(View.VISIBLE);
			readMoreView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					bodyView.setText(untruncated);
					readMoreView.setVisibility(View.GONE);
					fadeOverlay.setVisibility(View.GONE);
				}
			});

			body = String.format(getString(R.string.ellipsize_text_template), body.subSequence(0, cutAtWordBarrier(body)));
		}

		titleView.setVisibility(TextUtils.isEmpty(title) ? View.INVISIBLE : View.VISIBLE);
		titleView.setText(title);
		bodyView.setText(body);
	}

	public static int cutAtWordBarrier(CharSequence body) {
		int before = INTRO_PARAGRAPH_CUTOFF;
		for (int i = INTRO_PARAGRAPH_CUTOFF; i > 0; i --) {
			char c = body.charAt(i);
			if (c == ' ' || c == ',' || c == '.') {
				before = i;
				break;
			}
		}
		while (body.charAt(before) == ' ' || body.charAt(before) == ',' || body.charAt(before) == '.') {
			before --;
		}
		before ++;
		int after = INTRO_PARAGRAPH_CUTOFF;
		for (int i = INTRO_PARAGRAPH_CUTOFF; i < body.length(); i ++) {
			char c = body.charAt(i);
			if (c == ' ' || c == ',' || c == '.') {
				after = i;
				break;
			}
		}
		int leftDistance = Math.abs(INTRO_PARAGRAPH_CUTOFF - before);
		int rightDistance = Math.abs(after - INTRO_PARAGRAPH_CUTOFF);
		return (leftDistance < rightDistance) ? before : after;
	}
}
