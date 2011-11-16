package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchResultsFragmentActivity;
import com.expedia.bookings.activity.TabletUserReviewsListActivity;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.HotelDescription;
import com.expedia.bookings.data.HotelDescription.DescriptionSection;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Review;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.AvailabilitySummaryLayoutUtils;
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

	private static final int ANIMATION_SPEED = 350;

	//----------------------------------
	// VIEWS
	//----------------------------------

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
	private View mSelectRoomButton;

	//----------------------------------
	// OTHERS
	//----------------------------------
	private LayoutInflater mInflater;
	private HotelCollage mCollageHandler;

	//////////////////////////////////////////////////////////////////////////
	// LIFECYCLE EVENTS

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((SearchResultsFragmentActivity) getActivity()).mEventManager.registerEventHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_details, container, false);
		mInflater = inflater;

		mHotelNameTextView = (TextView) view.findViewById(R.id.hotel_name_text_view);
		mHotelLocationTextView = (TextView) view.findViewById(R.id.hotel_address_text_view);
		mCollageHandler = new HotelCollage(view, mPictureClickedListener);
		mReviewsTitle = (TextView) view.findViewById(R.id.reviews_title);
		mUserRating = (RatingBar) view.findViewById(R.id.user_rating_bar);
		mSomeReviewsContainer = (ViewGroup) view.findViewById(R.id.some_reviews_container);
		mReviewsSection = view.findViewById(R.id.reviews_section);
		mReviewsContainer = view.findViewById(R.id.reviews_container);
		mAmenitiesContainer = (ViewGroup) view.findViewById(R.id.amenities_table_row);
		mHotelDescriptionContainer = (ViewGroup) view.findViewById(R.id.hotel_description_section);
		mSeeAllReviewsButton = view.findViewById(R.id.see_all_reviews_button);
		mReviewsLoadingContainer = view.findViewById(R.id.reviews_loading_container);
		mSelectRoomButton = view.findViewById(R.id.book_now_button);

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
	public void onDetach() {
		((SearchResultsFragmentActivity) getActivity()).mEventManager.unregisterEventHandler(this);
		super.onDetach();
	}

	//////////////////////////////////////////////////////////////////////////
	// VIEWS

	public void updateViews() {
		updateViews(getInstance().mProperty);
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

		AvailabilitySummaryLayoutUtils.setupAvailabilitySummary(getActivity(), property, getView());

		// update the summarized rates if they are available
		AvailabilityResponse availabilityResponse = ((SearchResultsFragmentActivity) getActivity())
				.getRoomsAndRatesAvailability();
		mSelectRoomButton.setEnabled((availabilityResponse != null));

	AvailabilitySummaryLayoutUtils.updateSummarizedRates(getActivity(), property, availabilityResponse,
			getView(), getString(R.string.select_room), mSelectRoomButtonOnClickListener,
			((SearchResultsFragmentActivity) getActivity()).mOnRateClickListener);

		int dimenResId = (property.getTotalReviews() > 3) ? R.dimen.min_height_two_rows_reviews
				: R.dimen.min_height_one_row_review;
		mReviewsContainer.setMinimumHeight((int) getActivity().getResources().getDimension(dimenResId));
		mReviewsLoadingContainer.setVisibility(View.VISIBLE);

		addReviews(((SearchResultsFragmentActivity) getActivity()).getReviewsForProperty());

		mAmenitiesContainer.removeAllViews();

		//#10588 disabling amenities layout animations
		mAmenitiesContainer.setLayoutAnimation(null);
		LayoutUtils.addAmenities(getActivity(), property, mAmenitiesContainer);

		addHotelDescription(property);
	}

	//////////////////////////////////////////////////////////////////////////
	// CALLBACKS

	private OnCollageImageClickedListener mPictureClickedListener = new OnCollageImageClickedListener() {

		@Override
		public void onImageClicked(Media media) {
			((SearchResultsFragmentActivity) getActivity()).showHotelGalleryDialog(media);
		}
	};

	private OnClickListener mSelectRoomButtonOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			SummarizedRoomRates summarizedRoomRates = ((SearchResultsFragmentActivity) getActivity())
					.getSummarizedRoomRates();
			((SearchResultsFragmentActivity) getActivity()).bookRoom(summarizedRoomRates.getMinimumRateAvaialable());
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// EVENTHANDLER IMPLEMENTATION

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case SearchResultsFragmentActivity.EVENT_AVAILABILITY_SEARCH_STARTED:
			mSelectRoomButton.setEnabled(false);
			AvailabilitySummaryLayoutUtils.showLoadingForRates(getActivity(), getView());
			break;
		case SearchResultsFragmentActivity.EVENT_AVAILABILITY_SEARCH_ERROR:
			mSelectRoomButton.setEnabled(false);
			AvailabilitySummaryLayoutUtils.showErrorForRates(getView(), (String) data);
			break;
		case SearchResultsFragmentActivity.EVENT_AVAILABILITY_SEARCH_COMPLETE:
			mSelectRoomButton.setEnabled(true);
			AvailabilitySummaryLayoutUtils.showRatesContainer(getView());
			AvailabilitySummaryLayoutUtils.updateSummarizedRates(getActivity(), getInstance().mProperty,
					(AvailabilityResponse) data, getView(), getString(R.string.select_room),
					mSelectRoomButtonOnClickListener,
					((SearchResultsFragmentActivity) getActivity()).mOnRateClickListener);
			break;
		case SearchResultsFragmentActivity.EVENT_PROPERTY_SELECTED:
			updateViews((Property) data);
			break;
		case SearchResultsFragmentActivity.EVENT_REVIEWS_QUERY_STARTED:
			mSomeReviewsContainer.removeAllViews();
			mSomeReviewsContainer.setVisibility(View.GONE);
			mReviewsLoadingContainer.setVisibility(View.VISIBLE);
			break;
		case SearchResultsFragmentActivity.EVENT_REVIEWS_QUERY_COMPLETE:
			ReviewsResponse reviewsResposne = (ReviewsResponse) data;
			addReviews(reviewsResposne);
			break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS

	private void addReviews(ReviewsResponse reviewsResponse) {
		mSomeReviewsContainer.removeAllViews();
		mSomeReviewsContainer.setVisibility(View.GONE);

		if (reviewsResponse == null) {
			return;
		}

		int tenDp = (int) Math.ceil(getResources().getDisplayMetrics().density * 10);
		int fiveDp = (int) Math.ceil(getResources().getDisplayMetrics().density * 5);

		int numReviewsPerRow = getResources().getInteger(R.integer.num_reviews_per_row);
		int numReviewColumns = getResources().getInteger(R.integer.num_review_columns);

		int reviewCount = reviewsResponse.getReviewCount();
		if (reviewCount > 0) {
			for (int i = 0; i < numReviewsPerRow && reviewCount > 0; i++) {
				LinearLayout row = new LinearLayout(getActivity());
				LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0);

				rowParams.weight = 1;
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.setLayoutParams(rowParams);
				row.setPadding(0, tenDp, 0, 0);

				for (int j = 0; j < numReviewColumns && reviewCount > 0; j++) {
					final Review review = reviewsResponse.getReviews().get((i * numReviewsPerRow + j));
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

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
		lp.weight = 1;

		// setup the number of columns we expect to have
		int numHotelDescriptionColumns = getResources().getInteger(R.integer.num_hotel_description_sections_per_row);
		ArrayList<LinearLayout> columns = new ArrayList<LinearLayout>();
		for (int i = 0; i < numHotelDescriptionColumns; i++) {
			LinearLayout column = new LinearLayout(getActivity());
			column.setOrientation(LinearLayout.VERTICAL);
			column.setLayoutParams(lp);

			columns.add(column);
			mHotelDescriptionContainer.addView(column);
		}

		String description = property.getDescriptionText();
		HotelDescription hotelDescription = new HotelDescription(getActivity());

		if (description != null && description.length() > 0) {
			hotelDescription.parseDescription(property.getDescriptionText());
		}

		int sectionCount = hotelDescription.getSections().size();
		int tenDp = (int) Math.ceil(getResources().getDisplayMetrics().density * 10);
		int fiveDp = (int) Math.ceil(getResources().getDisplayMetrics().density * 5);
		int numHotelDescriptionsPerRow = getResources().getInteger(R.integer.num_hotel_description_sections_per_row);
		for (int i = 0; sectionCount > 0; i++) {

			for (int j = 0; j < numHotelDescriptionsPerRow && sectionCount > 0; j++) {
				DescriptionSection section = hotelDescription.getSections().get(i * numHotelDescriptionsPerRow + j);
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

				columns.get(i % columns.size()).addView(descriptionSection);

				sectionCount--;
			}
		}

	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public SearchResultsFragmentActivity.InstanceFragment getInstance() {
		return ((SearchResultsFragmentActivity) getActivity()).mInstance;
	}
}