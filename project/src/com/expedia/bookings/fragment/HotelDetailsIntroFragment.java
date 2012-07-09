package com.expedia.bookings.fragment;

import java.util.List;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

public class HotelDetailsIntroFragment extends Fragment {

	private boolean mIsStartingReviewsActivity = false;

	public static HotelDetailsIntroFragment newInstance() {
		return new HotelDetailsIntroFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_hotel_details_intro, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		populateViews();
		mIsStartingReviewsActivity = false;
	}

	public void populateViews() {
		View view = getView();
		populateBannerSection(view, DbPropertyHelper.getBestRateProperty().getLowestRate());
		populateReviewsSection(view, DbPropertyHelper.getBestReviewsProperty(),
				Db.getSelectedReviewsStatisticsResponse());
		populateIntroParagraph(view, DbPropertyHelper.getBestDescriptionProperty());
	}

	private void populateBannerSection(View view, Rate rate) {
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

		// Promo text, i.e. "Mobile Only!"
		// TODO: figure out the promo text and the correct value for hasPromo
		boolean hasPromo = rate.isOnSale();
		if (hasPromo) {
			promoTextView.setVisibility(View.VISIBLE);
		}
		else {
			promoTextView.setVisibility(View.GONE);
		}

		// "From <strike>$400</strike>" (if it's on sale) or else just "From"
		TextView fromTextView = Ui.findView(view, R.id.from_text_view);
		if (rate.isOnSale()) {
			fromTextView.setText(Html.fromHtml(
					getString(R.string.from_template, StrUtils.formatHotelPrice(rate.getAverageBaseRate())), null,
					new StrikethroughTagHandler()));

		}
		else {
			fromTextView.setText(R.string.from);
		}

		// Rate
		TextView rateTextView = Ui.findView(view, R.id.rate_text_view);
		rateTextView.setText(StrUtils.formatHotelPrice(rate.getDisplayRate()));
	}

	// Reviews
	private void populateReviewsSection(View view, Property property, ReviewsStatisticsResponse statistics) {
		int numReviews = 0;
		float percentRecommend = 0;
		float userRating = 0f;
		if (statistics != null) {
			numReviews = statistics.getTotalReviewCount();
			percentRecommend = numReviews == 0 ? 0f : statistics.getRecommendedCount() * 100f / numReviews;
			userRating = (float) statistics.getAverageOverallRating();
		}
		else {
			numReviews = 0;
			percentRecommend = 0;
			userRating = 0;
		}

		TextView reviewsTextView = Ui.findView(view, R.id.user_rating_text_view);
		reviewsTextView.setText(getResources().getQuantityString(R.plurals.number_of_reviews, numReviews, numReviews));
		OnClickListener onReviewsClick = (!property.hasExpediaReviews()) ? null : new OnClickListener() {
			public synchronized void onClick(final View v) {
				if (!mIsStartingReviewsActivity) {
					mIsStartingReviewsActivity = true;
					Intent newIntent = new Intent(getActivity(), UserReviewsListActivity.class);
					newIntent.fillIn(getActivity().getIntent(), 0);
					startActivity(newIntent);
				}
			}
		};
		view.findViewById(R.id.reviews_summary_layout).setOnClickListener(onReviewsClick);

		// User Rating Bar
		RatingBar userRatingBar = Ui.findView(view, R.id.user_rating_bar);
		// A little animation for v11
		if (AndroidUtils.getSdkVersion() >= 11) {
			userRatingBar.setRating(0f);
			if (numReviews > 0) {
				//TODO: ObjectAnimator doesn't exist pre v11
				ObjectAnimator.ofFloat(userRatingBar, "rating", userRating).start();
			}
		}
		else {
			userRatingBar.setRating(userRating);
		}

		// xx% recommend this hotel
		TextView recommendTextView = Ui.findView(view, R.id.percent_recommend_text_view);
		recommendTextView.setText(getString(R.string.x_percent_guests_recommend, percentRecommend));

		// Thumbs up / thumbs down
		ImageView ratingThumbImage = Ui.findView(view, R.id.rating_thumb_image);
		// A little animation for v11
		if (AndroidUtils.getSdkVersion() >= 11) {
			ratingThumbImage.setImageResource(R.drawable.rating_good);
			ratingThumbImage.setRotation(90);
			if (numReviews > 0) {
				ratingThumbImage.animate().rotationBy(percentRecommend > 50 ? -90 : 90);
			}
		}
		else {
			ratingThumbImage.setImageResource(percentRecommend > 50 ? R.drawable.rating_good : R.drawable.rating_bad);
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
		int cutoff = 90;
		if (body.length() > cutoff) {
			final CharSequence untruncated = body;
			final View readMoreView = Ui.findView(view, R.id.read_more_layout);
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
			body = body.subSequence(0, cutoff) + "...";
		}

		titleView.setVisibility(TextUtils.isEmpty(title) ? View.INVISIBLE : View.VISIBLE);
		titleView.setText(title);
		bodyView.setText(body);
	}
}
