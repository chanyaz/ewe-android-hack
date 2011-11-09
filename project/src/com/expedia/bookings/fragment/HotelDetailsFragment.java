package com.expedia.bookings.fragment;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.activity.TabletUserReviewsListActivity;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.HotelDescription;
import com.expedia.bookings.data.HotelDescription.DescriptionSection;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.BedType;
import com.expedia.bookings.data.Rate.BedTypeId;
import com.expedia.bookings.data.Review;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.HotelCollage;
import com.expedia.bookings.widget.HotelCollage.OnCollageImageClickedListener;
import com.expedia.bookings.widget.SummarizedRoomRates;

public class HotelDetailsFragment extends Fragment implements EventHandler {

	public static HotelDetailsFragment newInstance() {
		HotelDetailsFragment fragment = new HotelDetailsFragment();
		return fragment;
	}

	//////////////////////////////////////////////////////////////////////////
	// MEMBER VARIABLES

	//----------------------------------
	// CONSTANTS
	//----------------------------------

	private static final int MAX_SUMMARIZED_RATE_RESULTS = 3;
	private static final int MAX_REVIEWS_PER_ROW = 2;
	private static final int MAX_DESCRIPTION_SECTIONS_PER_ROW = 2;
	private static final int ANIMATION_SPEED = 350;

	//----------------------------------
	// VIEWS
	//----------------------------------

	private ViewGroup mAvailabilitySummaryContainer;
	private ViewGroup mAvailabilityRatesContainer;
	private TextView mEmptyAvailabilitySummaryTextView;
	private TextView mHotelLocationTextView;
	private TextView mHotelNameTextView;
	private TextView mReviewsTitle;
	private View mReviewsSection;
	private View mReviewsContainer;
	private ViewGroup mSomeReviewsContainer;
	private View mReviewsLoadingContainer;
	private ViewGroup mAmenitiesContainer;
	private RatingBar mUserRating;
	private ViewGroup mHotelDescriptionContainer;
	private View mSeeAllReviewsButton;
	private ProgressBar mRatesProgressBar;

	//----------------------------------
	// OTHERS
	//----------------------------------
	private LayoutInflater mInflater;
	private HotelCollage mCollageHandler;
	private SummarizedRoomRates mSummarizedRoomRates;

	//////////////////////////////////////////////////////////////////////////
	// LIFECYCLE EVENTS

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_details, container, false);
		mInflater = inflater;

		mHotelNameTextView = (TextView) view.findViewById(R.id.hotel_name_text_view);
		mHotelLocationTextView = (TextView) view.findViewById(R.id.hotel_address_text_view);
		mAvailabilitySummaryContainer = (ViewGroup) view.findViewById(R.id.availability_summary_container);
		mAvailabilityRatesContainer = (ViewGroup) view.findViewById(R.id.rates_container);
		mEmptyAvailabilitySummaryTextView = (TextView) view.findViewById(R.id.empty_summart_container);
		mCollageHandler = new HotelCollage(view, mPictureClickedListener);
		mReviewsTitle = (TextView) view.findViewById(R.id.reviews_title);
		mUserRating = (RatingBar) view.findViewById(R.id.user_rating_bar);
		mSomeReviewsContainer = (ViewGroup) view.findViewById(R.id.some_reviews_container);
		mReviewsSection = view.findViewById(R.id.reviews_section);
		mReviewsContainer = view.findViewById(R.id.reviews_container);
		mAmenitiesContainer = (ViewGroup) view.findViewById(R.id.amenities_table_row);
		mHotelDescriptionContainer = (ViewGroup) view.findViewById(R.id.hotel_description_section);
		mSeeAllReviewsButton = view.findViewById(R.id.see_all_reviews_button);
		mRatesProgressBar = (ProgressBar) view.findViewById(R.id.rates_progress_bar);
		mReviewsLoadingContainer =  view.findViewById(R.id.reviews_loading_container);
		
		mSummarizedRoomRates = new SummarizedRoomRates();

		// Disable the scrollbar on the amenities HorizontalScrollView
		HorizontalScrollView amenitiesScrollView = (HorizontalScrollView) view.findViewById(R.id.amenities_scroll_view);
		amenitiesScrollView.setHorizontalScrollBarEnabled(false);
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		updateViews();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((TabletActivity) getActivity()).registerEventHandler(this);
	}

	@Override
	public void onDetach() {
		((TabletActivity) getActivity()).unregisterEventHandler(this);
		super.onDetach();
	}

	//////////////////////////////////////////////////////////////////////////
	// VIEWS

	public void updateViews() {

		updateViews(((TabletActivity) getActivity()).getPropertyToDisplay());
	}

	public void updateViews(final Property property) {
		mHotelNameTextView.setText(property.getName());
		String hotelAddressWithNewLine = StrUtils.formatAddress(property.getLocation(), StrUtils.F_STREET_ADDRESS
				+ StrUtils.F_CITY + StrUtils.F_STATE_CODE);
		mHotelLocationTextView.setText(hotelAddressWithNewLine.replace("\n", ", "));
		mCollageHandler.updateCollage(property);
		mReviewsTitle.setText(getString(R.string.reviews_recommended_template, property.getTotalRecommendations(),
				property.getTotalReviews()));
		mUserRating.setRating((float) property.getAverageExpediaRating());

		if (property.hasExpediaReviews()) {
			mReviewsSection.setVisibility(View.VISIBLE);
			mSeeAllReviewsButton.setVisibility(View.VISIBLE);
			mSeeAllReviewsButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent i = new Intent(getActivity(), TabletUserReviewsListActivity.class);
					i.putExtra(Codes.PROPERTY, property.toJson().toString());
					i.putExtra(Codes.DISPLAY_MODAL_VIEW, true);
					startActivity(i);
				}
			});
		}
		else {
			mSeeAllReviewsButton.setVisibility(View.GONE);
			mReviewsLoadingContainer.setVisibility(View.GONE);
			mReviewsSection.setVisibility(View.GONE);
		}
		
		setupAvailabilitySummary();
		// update the summarized rates if they are available
		AvailabilityResponse availabilityResponse = ((TabletActivity) getActivity()).getRoomsAndRatesAvailability();
		updateSummarizedRates(availabilityResponse);

		
		int dimenResId = (property.getTotalReviews() > 3) ? R.dimen.min_height_two_rows_reviews : R.dimen.min_height_one_row_review;
		mReviewsContainer.setMinimumHeight((int) getActivity().getResources().getDimension(dimenResId));
		mReviewsLoadingContainer.setVisibility(View.VISIBLE);
		
		addReviews(((TabletActivity) getActivity()).getReviewsForProperty());

		mAmenitiesContainer.removeAllViews();
		LayoutUtils.addAmenities(getActivity(), property, mAmenitiesContainer);
		
		addHotelDescription(property);
	}

	//////////////////////////////////////////////////////////////////////////
	// CALLBACKS

	private OnCollageImageClickedListener mPictureClickedListener = new OnCollageImageClickedListener() {

		@Override
		public void onImageClicked(String url) {
			((TabletActivity) getActivity()).showPictureGalleryForHotel(url);
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// EVENTHANDLER IMPLEMENTATION

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_AVAILABILITY_SEARCH_STARTED:
			showLoadingForRates();
			mSummarizedRoomRates.clearOutData();
			break;
		case TabletActivity.EVENT_AVAILABILITY_SEARCH_ERROR:
			mEmptyAvailabilitySummaryTextView.setVisibility(View.VISIBLE);
			mRatesProgressBar.setVisibility(View.INVISIBLE);
			mEmptyAvailabilitySummaryTextView.setText((String) data);
			mAvailabilityRatesContainer.setVisibility(View.GONE);
			break;
		case TabletActivity.EVENT_AVAILABILITY_SEARCH_COMPLETE:
			mEmptyAvailabilitySummaryTextView.setVisibility(View.GONE);
			mRatesProgressBar.setVisibility(View.GONE);
			mAvailabilityRatesContainer.setVisibility(View.VISIBLE);
			updateSummarizedRates((AvailabilityResponse) data);
			break;
		case TabletActivity.EVENT_PROPERTY_SELECTED:
			updateViews((Property) data);
			break;
		case TabletActivity.EVENT_REVIEWS_QUERY_STARTED:
			mSomeReviewsContainer.removeAllViews();
			mSomeReviewsContainer.setVisibility(View.GONE);
			mReviewsLoadingContainer.setVisibility(View.VISIBLE);
			break;
		case TabletActivity.EVENT_REVIEWS_QUERY_COMPLETE:
			ReviewsResponse reviewsResposne = (ReviewsResponse) data;
 			addReviews(reviewsResposne);
			break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS

	private void showLoadingForRates() {
		mEmptyAvailabilitySummaryTextView.setVisibility(View.VISIBLE);
		mRatesProgressBar.setVisibility(View.VISIBLE);
		mEmptyAvailabilitySummaryTextView.setText(getString(R.string.room_rates_loading));
		mAvailabilityRatesContainer.setVisibility(View.GONE);
	}


	private void setupAvailabilitySummary() {
		final Property property = ((TabletActivity) getActivity()).getPropertyToDisplay();
		boolean isPropertyOnSale = property.getLowestRate().getSavingsPercent() > 0;
		if (isPropertyOnSale) {
			mAvailabilitySummaryContainer.setBackgroundResource(R.drawable.bg_summarized_room_rates_sale);
		}
		else {
			mAvailabilitySummaryContainer.setBackgroundResource(R.drawable.bg_summarized_room_rates);
		}

		View minPriceRow = getView().findViewById(R.id.min_price_row_container);
		TextView minPrice = (TextView) minPriceRow.findViewById(R.id.min_price_text_view);

		String displayRateString = StrUtils.formatHotelPrice(property.getLowestRate().getDisplayRate());
		String minPriceString = getString(R.string.min_room_price_template, displayRateString);
		int startingIndexOfDisplayRate = minPriceString.indexOf(displayRateString);

		// style the minimum available price text
		StyleSpan textStyleSpan = new StyleSpan(Typeface.BOLD);
		ForegroundColorSpan textColorSpan = new ForegroundColorSpan(getResources().getColor(
				R.color.hotel_price_text_color));
		ForegroundColorSpan textWhiteColorSpan = new ForegroundColorSpan(getResources().getColor(android.R.color.white));
		ForegroundColorSpan textBlackColorSpan = new ForegroundColorSpan(getResources().getColor(android.R.color.black));

		Spannable str = new SpannableString(minPriceString);

		str.setSpan(textStyleSpan, 0, minPriceString.length(), 0);

		if (isPropertyOnSale) {
			str.setSpan(textWhiteColorSpan, 0, minPriceString.length(), 0);
		}
		else {
			str.setSpan(textColorSpan, startingIndexOfDisplayRate,
					startingIndexOfDisplayRate + displayRateString.length(), 0);
			str.setSpan(textBlackColorSpan, 0, startingIndexOfDisplayRate - 1, 0);
		}

		minPrice.setText(str);

		TextView perNighTextView = (TextView) minPriceRow.findViewById(R.id.per_night_text_view);
		perNighTextView.setTextColor(isPropertyOnSale ? getResources().getColor(android.R.color.white) : getResources()
				.getColor(android.R.color.black));

		if (Rate.showInclusivePrices()) {
			perNighTextView.setVisibility(View.GONE);
		}
		else {
			perNighTextView.setVisibility(View.VISIBLE);
		}
	}

	private void layoutAvailabilitySummary() {
		final Property property = ((TabletActivity) getActivity()).getPropertyToDisplay();
		boolean isPropertyOnSale = property.getLowestRate().getSavingsPercent() > 0;
		mAvailabilityRatesContainer.removeAllViews();

		// first adding all rows since the rows will exist regardless of whether
		// there are enough rooms available or not 
		for (int i = 0; i < MAX_SUMMARIZED_RATE_RESULTS; i++) {
			View summaryRow = mInflater.inflate(R.layout.snippet_availability_summary_row, null);
			setHeightOfWeightOneForRow(summaryRow);

			if (i == (MAX_SUMMARIZED_RATE_RESULTS - 1)) {
				summaryRow.findViewById(R.id.divider).setVisibility(View.GONE);
			}
			mAvailabilityRatesContainer.addView(summaryRow);
		}

		for (int i = 0; i < MAX_SUMMARIZED_RATE_RESULTS; i++) {
			View summaryRow = mAvailabilityRatesContainer.getChildAt(i);
			ObjectAnimator animator = ObjectAnimator.ofFloat(summaryRow, "alpha", 0, 1);
			animator.setDuration(ANIMATION_SPEED);
			animator.start();

			if (i > (mSummarizedRoomRates.numSummarizedRates() - 1)) {
				continue;
			}

			final Rate rate = mSummarizedRoomRates.getRate(i);
			summaryRow.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					((TabletActivity) getActivity()).bookRoom(rate);
				}
			});

			View chevron = summaryRow.findViewById(R.id.availability_chevron_image_view);
			chevron.setVisibility(View.VISIBLE);
			TextView summaryDescription = (TextView) summaryRow.findViewById(R.id.availability_description_text_view);
			TextView priceTextView = (TextView) summaryRow.findViewById(R.id.availability_summary_price_text_view);

			Pair<BedTypeId, Rate> pair = mSummarizedRoomRates.getBedTypeToRatePair(i);
			for (BedType bedType : pair.second.getBedTypes()) {
				if (bedType.bedTypeId == pair.first) {
					summaryDescription.setText(Html.fromHtml(getString(R.string.bed_type_start_value_template,
							bedType.bedTypeDescription)));
					break;
				}
			}

			priceTextView.setText(StrUtils.formatHotelPrice(pair.second.getDisplayRate()));
			if (isPropertyOnSale) {
				priceTextView.setTextColor(getResources().getColor(R.color.hotel_price_sale_text_color));
			}
			else {
				priceTextView.setTextColor(getResources().getColor(R.color.hotel_price_text_color));
			}
		}

		View selectRoomContainer = mInflater.inflate(R.layout.snippet_select_room_button, null);
		setHeightOfWeightOneForRow(selectRoomContainer);

		View selectRoomButton = selectRoomContainer.findViewById(R.id.book_now_button);
		selectRoomButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if the user just presses the book now button,
				// default to giving the user the minimum rate available
				((TabletActivity) getActivity()).bookRoom(mSummarizedRoomRates.getMinimumRateAvaialable());
			}
		});
		mAvailabilityRatesContainer.addView(selectRoomContainer);

	}

	private void setHeightOfWeightOneForRow(View view) {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0);
		lp.weight = 1;
		view.setLayoutParams(lp);
	}

	private void updateSummarizedRates(AvailabilityResponse availabilityResponse) {
		mSummarizedRoomRates.clearOutData();

		if (availabilityResponse != null) {
			mSummarizedRoomRates.updateSummarizedRoomRates(availabilityResponse);
			layoutAvailabilitySummary();
			mEmptyAvailabilitySummaryTextView.setVisibility(View.GONE);
			mRatesProgressBar.setVisibility(View.GONE);
		}
		else {
			// since the data is not yet available,
			// make sure to clean out any old data and show the loading screen
			showLoadingForRates();
		}
	}

	private void addReviews(ReviewsResponse reviewsResponse) {
		mSomeReviewsContainer.removeAllViews();
		mSomeReviewsContainer.setVisibility(View.GONE);

		if (reviewsResponse == null) {
			return;
		}

		int tenDp = (int) Math.ceil(getActivity().getResources().getDisplayMetrics().density * 10);
		int fiveDp = (int) Math.ceil(getActivity().getResources().getDisplayMetrics().density * 5);
		int reviewCount = reviewsResponse.getReviewCount();
		if (reviewCount > 0) {
			for (int i = 0; i < MAX_REVIEWS_PER_ROW && reviewCount > 0; i++) {
				LinearLayout row = new LinearLayout(getActivity());
				LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0);

				rowParams.weight = 1;
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.setLayoutParams(rowParams);
				row.setPadding(0, tenDp, 0, 0);

				for (int j = 0; j < MAX_REVIEWS_PER_ROW && reviewCount > 0; j++) {
					final Review review = reviewsResponse.getReviews().get((i * MAX_REVIEWS_PER_ROW + j));
					ViewGroup reviewSection = (ViewGroup) mInflater.inflate(R.layout.snippet_review, null);

					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
					params.weight = 1;
					params.rightMargin = fiveDp;
					reviewSection.setLayoutParams(params);

					TextView reviewTitle = (TextView) reviewSection.findViewById(R.id.review_title);
					reviewTitle.setText(review.getTitle());

					RatingBar reviewRating = (RatingBar) reviewSection.findViewById(R.id.user_review_rating);
					reviewRating.setRating((float) review.getRating().getOverallSatisfaction());

					final TextView reviewBody = (TextView) reviewSection.findViewById(R.id.review_body);
					reviewBody.setLines(2);
					reviewBody.setText(review.getBody());

					row.addView(reviewSection);

					reviewCount--;
				}
				mSomeReviewsContainer.addView(row);
			}
			mReviewsLoadingContainer.setVisibility(View.GONE);
			mSomeReviewsContainer.setVisibility(View.VISIBLE);
			ObjectAnimator animator = ObjectAnimator.ofFloat(mSomeReviewsContainer, "alpha", 0, 1);
			animator.setDuration(ANIMATION_SPEED);
			animator.start();
		} 
	}

	private void addHotelDescription(Property property) {
		mHotelDescriptionContainer.removeAllViews();

		LinearLayout column1 = new LinearLayout(getActivity());
		column1.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
		lp.weight = 1;
		column1.setLayoutParams(lp);

		LinearLayout column2 = new LinearLayout(getActivity());
		column2.setOrientation(LinearLayout.VERTICAL);
		column2.setLayoutParams(lp);

		mHotelDescriptionContainer.addView(column1);
		mHotelDescriptionContainer.addView(column2);

		String description = property.getDescriptionText();
		HotelDescription hotelDescription = new HotelDescription(getActivity());

		if (description != null && description.length() > 0) {
			hotelDescription.parseDescription(property.getDescriptionText());
		}

		int sectionCount = hotelDescription.getSections().size();
		int tenDp = (int) Math.ceil(getActivity().getResources().getDisplayMetrics().density * 10);
		int fiveDp = (int) Math.ceil(getActivity().getResources().getDisplayMetrics().density * 5);

		for (int i = 0; sectionCount > 0; i++) {

			for (int j = 0; j < MAX_DESCRIPTION_SECTIONS_PER_ROW && sectionCount > 0; j++) {
				DescriptionSection section = hotelDescription.getSections().get(
						i * MAX_DESCRIPTION_SECTIONS_PER_ROW + j);
				ViewGroup descriptionSection = (ViewGroup) mInflater.inflate(
						R.layout.snippet_hotel_description_section, null);

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.WRAP_CONTENT);
				params.bottomMargin = tenDp;
				params.rightMargin = fiveDp;
				descriptionSection.setLayoutParams(params);

				TextView descriptionTitle = (TextView) descriptionSection
						.findViewById(R.id.title_description_text_view);
				descriptionTitle.setText(section.title);

				TextView descriptionBody = (TextView) descriptionSection.findViewById(R.id.body_description_text_view);
				descriptionBody.setText(section.description);

				if (i % 2 == 0) {
					column1.addView(descriptionSection);
				}
				else {
					column2.addView(descriptionSection);
				}
				sectionCount--;
			}
		}

	}
}