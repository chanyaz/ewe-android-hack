package com.expedia.bookings.fragment;

import java.util.List;

import android.content.Intent;
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
import com.nineoldandroids.animation.ObjectAnimator;

public class HotelDetailsIntroFragment extends Fragment {

	private static final int ROOMS_LEFT_CUTOFF = 5;

	private static final int INTRO_PARAGRAPH_CUTOFF = 180;

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
		populateReviewsSection(view, DbPropertyHelper.getBestReviewsProperty(),
				Db.getSelectedReviewsStatisticsResponse());
		populateIntroParagraph(view, DbPropertyHelper.getBestDescriptionProperty());
	}

	private void populateBannerSection(View view, Property property) {
		Rate rate = property.getLowestRate();

		// Sale banner
		TextView saleBannerTextView = Ui.findView(view, R.id.sale_banner_text_view);
		TextView promoTextView = Ui.findView(view, R.id.promo_text_view);
		if (rate.isOnSale()) {
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
	private void populateReviewsSection(View view, Property property, ReviewsStatisticsResponse statistics) {

		// Reviews
		int numReviews = 0;
		float percentRecommend = 0;
		float userRating = 0f;
		if (statistics != null) {
			numReviews = statistics.getTotalReviewCount();
			percentRecommend = numReviews == 0 ? 0f : statistics.getRecommendedCount() * 100f / numReviews;
			userRating = (float) statistics.getAverageOverallRating();
		}

		TextView reviewsTextView = Ui.findView(view, R.id.user_rating_text_view);
		reviewsTextView.setText(getResources().getQuantityString(R.plurals.number_of_reviews, numReviews, numReviews));
		OnClickListener onReviewsClick = (!property.hasExpediaReviews()) ? null : new OnClickListener() {
			public synchronized void onClick(final View v) {
				Intent newIntent = new Intent(getActivity(), UserReviewsListActivity.class);
				newIntent.fillIn(getActivity().getIntent(), 0);
				startActivity(newIntent);
			}
		};
		view.findViewById(R.id.user_review_layout).setOnClickListener(onReviewsClick);

		// User Rating Bar
		RatingBar userRatingBar = Ui.findView(view, R.id.user_rating_bar);
		userRatingBar.setRating(0f);
		if (numReviews > 0) {
			ObjectAnimator.ofFloat(userRatingBar, "rating", userRating).start();
		}

		// Urgency messaging
		TextView urgencyTextView = Ui.findView(view, R.id.urgency_message_text_view);

		int roomsLeft = property.getRoomsLeftAtThisRate();

		// xx booked in the past x hours
		/*if (TODO: xx booked in the past x hours) {
		}*/
		// Only xx rooms left
		/*else*/if (roomsLeft > 0 && roomsLeft <= ROOMS_LEFT_CUTOFF) {
			urgencyTextView.setText(getResources().getQuantityString(R.plurals.num_rooms_left, roomsLeft, roomsLeft));
			urgencyTextView.setVisibility(View.VISIBLE);
			//TODO: better drawable
			urgencyTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.review_thumbs_up, 0, 0, 0);
		}

		// xx% recommend this hotel
		else {
			urgencyTextView.setText(getString(R.string.x_percent_guests_recommend, percentRecommend));
			urgencyTextView.setVisibility(View.VISIBLE);
			//TODO: better drawable
			urgencyTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.review_thumbs_up, 0, 0, 0);
		}
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
			body = body.subSequence(0, INTRO_PARAGRAPH_CUTOFF) + "É";
		}

		titleView.setVisibility(TextUtils.isEmpty(title) ? View.INVISIBLE : View.VISIBLE);
		titleView.setText(title);
		bodyView.setText(body);
	}
}
