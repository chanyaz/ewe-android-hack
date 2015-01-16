package com.expedia.bookings.fragment;

import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.HotelPayLaterInfoActivity;
import com.expedia.bookings.activity.UserReviewsListActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.HotelTextSection;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.util.Ui;

public class HotelDetailsIntroFragment extends Fragment {

	private static final int ROOMS_LEFT_CUTOFF = 5;

	private static final int INTRO_PARAGRAPH_CUTOFF = 120;

	private AnimatorSet mAnimSet;

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
		Property property = Db.getHotelSearch().getSelectedProperty();
		if (property != null) {
			populateBannerSection(view, property);
			populateIntroParagraph(view, property);
		}
	}

	// Reviews
	private void populateBannerSection(View view, Property property) {
		Resources resources = getResources();
		View reviewsSummaryLayout = Ui.findView(view, R.id.reviews_summary_layout);
		View reviewsLayout = Ui.findView(view, R.id.user_review_layout);
		TextView reviewsTextView = Ui.findView(view, R.id.user_rating_text_view);
		View verticalSep = Ui.findView(view, R.id.vertical_sep);
		TextView bannerTextView = Ui.findView(view, R.id.banner_message_text_view);
		RatingBar userRatingBar = Ui.findView(view, R.id.user_rating_bar);
		View payLaterInfo = Ui.findView(view, R.id.pay_later_info_banner);

		reviewsLayout.setVisibility(View.VISIBLE);
		bannerTextView.setVisibility(View.VISIBLE);
		verticalSep.setVisibility(View.VISIBLE);

		// Search params (if it's a specific hotel search)
		HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();
		if (searchParams.getSearchType() == SearchType.HOTEL) {
			View searchLayout = Ui.findView(view, R.id.search_params_layout);
			searchLayout.setVisibility(View.VISIBLE);

			TextView calendarTextView = Ui.findView(view, R.id.calendar_text_view);
			calendarTextView.setText(Integer.toString(searchParams.getCheckInDate().getDayOfMonth()));

			TextView searchDatesTextView = Ui.findView(view, R.id.search_dates_text_view);
			searchDatesTextView.setText(DateFormatUtils.formatDateRange(getActivity(), searchParams, DateFormatUtils.FLAGS_DATE_ABBREV_ALL));

			TextView searchGuestsTextView = Ui.findView(view, R.id.search_guests_text_view);
			searchGuestsTextView.setText(StrUtils.formatGuests(getActivity(), searchParams));

			View changeSearchView = Ui.findView(view, R.id.change_search_view);
			changeSearchView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					NavUtils.goToHotels(getActivity(), null, null, NavUtils.FLAG_OPEN_SEARCH);
				}
			});
		}

		// Reviews
		int numReviews = property.getTotalReviews();
		float percentRecommend = property.getPercentRecommended();
		float userRating = (float) property.getAverageExpediaRating();

		String reviewsText = numReviews == 0
				? resources.getString(R.string.no_reviews)
				: resources.getQuantityString(R.plurals.number_of_reviews, numReviews, numReviews);
		if (!reviewsTextView.getText().equals(reviewsText)) {
			reviewsTextView.setText(reviewsText);
		}

		if (!property.hasExpediaReviews() && numReviews == 0) {
			reviewsSummaryLayout.setOnClickListener(null);
		}
		else {
			OnClickListener userReviewsClickListener = new OnClickListener() {
				public synchronized void onClick(final View v) {
					Intent newIntent = new Intent(getActivity(), UserReviewsListActivity.class);
					newIntent.fillIn(getActivity().getIntent(), 0);
					OmnitureTracking.trackPageLoadHotelsDetailsReviews(getActivity());
					startActivity(newIntent);
				}
			};
			reviewsSummaryLayout.setBackgroundResource(R.drawable.bg_clickable_row);
			reviewsSummaryLayout.setOnClickListener(userReviewsClickListener);
		}

		// Banner messages
		int roomsLeft = property.getRoomsLeftAtThisRate();

		// xx booked in the past x hours
		/*if (TODO: xx booked in the past x hours) {
			String banner = TODO
			if (!bannerTextView.getText().equals(banner)) {
			bannerTextView.setText(banner);
			bannerTextView.setVisibility(View.VISIBLE);
			bannerTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_urgency_clock, 0, 0, 0);
			}
		}*/
		// Only xx rooms left
		/*else*/
		if (roomsLeft > 0 && roomsLeft <= ROOMS_LEFT_CUTOFF) {
			String banner = resources.getQuantityString(R.plurals.num_rooms_left, roomsLeft, roomsLeft);
			if (!bannerTextView.getText().equals(banner)) {
				bannerTextView.setText(banner);
				bannerTextView.setVisibility(View.VISIBLE);
				bannerTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_urgency_clock, 0, 0, 0);
			}
		}

		// Special case if no urgency and no recommendations: hide this while banner section.
		else if (percentRecommend == 0 && numReviews == 0) {
			reviewsSummaryLayout.setVisibility(View.GONE);
			view.findViewById(R.id.reviews_banner_divider).setVisibility(View.GONE);
			return;
		}

		// xx% recommend this hotel
		else {
			String banner = resources.getString(R.string.x_percent_guests_recommend, percentRecommend);
			if (!bannerTextView.getText().equals(banner)) {
				bannerTextView.setText(banner);
				bannerTextView.setVisibility(View.VISIBLE);
				bannerTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_urgency_users, 0, 0, 0);
			}
		}

		// User Rating Bar
		if (userRatingBar.getRating() != userRating && (mAnimSet == null || !mAnimSet.isRunning())) {
			userRatingBar.setRating(0f);

			ObjectAnimator animRating = ObjectAnimator.ofFloat(userRatingBar, "rating", userRating);
			ObjectAnimator anim1 = ObjectAnimator.ofFloat(reviewsLayout, "alpha", 0f, 1f);
			ObjectAnimator anim2 = ObjectAnimator.ofFloat(bannerTextView, "alpha", 0f, 1f);
			ObjectAnimator anim3 = ObjectAnimator.ofFloat(verticalSep, "alpha", 0f, 1f);

			mAnimSet = new AnimatorSet();
			mAnimSet.play(anim1).with(anim2).with(anim3).before(animRating);

			mAnimSet.start();
		}

		// ETP pay later offer info
		if (ProductFlavorFeatureConfiguration.getInstance().isETPEnabled() && property.hasEtpOffer()) {
			payLaterInfo.setVisibility(View.VISIBLE);
			payLaterInfo.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent payLaterIntent = new Intent(getActivity(), HotelPayLaterInfoActivity.class);
					startActivity(payLaterIntent);
				}
			});
		}
		else {
			payLaterInfo.setVisibility(View.GONE);
		}
	}

	private void populateIntroParagraph(View view, Property property) {
		List<HotelTextSection> sections = property.getAllHotelText();

		final TextView titleView = Ui.findView(view, R.id.title_text);
		final TextView bodyView = Ui.findView(view, R.id.body_text);

		CharSequence title, body;
		if (sections != null && sections.size() >= 1) {
			title = sections.get(0).getNameWithoutHtml();
			body = Html.fromHtml(sections.get(0).getContentFormatted(getActivity()));
		}
		else {
			title = Html.fromHtml("");
			body = Html.fromHtml(property.getDescriptionText());
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

			body = String.format(getString(R.string.ellipsize_text_template),
					body.subSequence(0, cutAtWordBarrier(body)));
		}

		// Always hide this for the intro
		titleView.setVisibility(View.GONE);
		bodyView.setText(body);
	}

	public static int cutAtWordBarrier(CharSequence body) {
		int before = INTRO_PARAGRAPH_CUTOFF;
		for (int i = INTRO_PARAGRAPH_CUTOFF; i > 0; i--) {
			char c = body.charAt(i);
			if (c == ' ' || c == ',' || c == '.') {
				before = i;
				break;
			}
		}
		while (body.charAt(before) == ' ' || body.charAt(before) == ',' || body.charAt(before) == '.') {
			before--;
		}
		before++;
		int after = INTRO_PARAGRAPH_CUTOFF;
		for (int i = INTRO_PARAGRAPH_CUTOFF; i < body.length(); i++) {
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
