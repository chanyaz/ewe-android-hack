package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.activity.UserReviewsListActivity;
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

	//----------------------------------
	// VIEWS
	//----------------------------------

	private ViewGroup mAvailabilitySummaryContainer;
	private TextView mEmptyAvailabilitySummaryTextView;
	private TextView mHotelLocationTextView;
	private TextView mHotelNameTextView;
	private RatingBar mHotelRatingBar;
	private TextView mReviewsTitle;
	private ViewGroup mReviewsSection;
	private ViewGroup mAmenitiesContainer;
	private RatingBar mUserRating;
	private ViewGroup mHotelDescriptionContainer;
	private View mBookNowButton;
	private View mSeeAllReviewsButton;

	//----------------------------------
	// OTHERS
	//----------------------------------
	private LayoutInflater mInflater;
	private HotelCollage mCollageHandler;

	//////////////////////////////////////////////////////////////////////////
	// LIFECYCLE EVENTS

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_details, container, false);
		mInflater = inflater;

		mHotelNameTextView = (TextView) view.findViewById(R.id.hotel_name_text_view);
		mHotelLocationTextView = (TextView) view.findViewById(R.id.hotel_address_text_view);
		mHotelRatingBar = (RatingBar) view.findViewById(R.id.hotel_rating_bar);
		mAvailabilitySummaryContainer = (ViewGroup) view.findViewById(R.id.availability_summary_container);
		mEmptyAvailabilitySummaryTextView = (TextView) view.findViewById(R.id.empty_summart_container);
		mCollageHandler = new HotelCollage(view, mPictureClickedListener);
		mReviewsTitle = (TextView) view.findViewById(R.id.reviews_title);
		mUserRating = (RatingBar) view.findViewById(R.id.user_rating_bar);
		mReviewsSection = (ViewGroup) view.findViewById(R.id.reviews_container);
		mAmenitiesContainer = (ViewGroup) view.findViewById(R.id.amenities_table_row);
		mHotelDescriptionContainer = (ViewGroup) view.findViewById(R.id.hotel_description_section);
		mBookNowButton = view.findViewById(R.id.book_now_button);
		mSeeAllReviewsButton = view.findViewById(R.id.see_all_reviews_button);

		mSeeAllReviewsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_Light_Fullscreen_Preferences);
				Intent i = new Intent(getActivity(), UserReviewsListActivity.class);
				i.putExtra(Codes.PROPERTY, ((TabletActivity) getActivity()).getPropertyToDisplay().toJson().toString());
				i.putExtra(Codes.DISPLAY_MODAL_VIEW, true);
				contextWrapper.startActivity(i);
			}
		});

		mBookNowButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if the user just presses the book now button,
				// default to giving the user the minimum rate available
				((TabletActivity) getActivity()).bookRoom(mMinimumRateAvailable);
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
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

	public void updateViews(Property property) {
		mHotelNameTextView.setText(property.getName());
		String hotelAddressWithNewLine = StrUtils.formatAddress(property.getLocation(), StrUtils.F_STREET_ADDRESS
				+ StrUtils.F_CITY + StrUtils.F_STATE_CODE);
		mHotelLocationTextView.setText(hotelAddressWithNewLine.replace("\n", ", "));
		mHotelRatingBar.setRating((float) property.getHotelRating());
		mCollageHandler.updateCollage(property);
		mReviewsTitle.setText(getString(R.string.reviews_recommended_template, property.getTotalRecommendations(),
				property.getTotalReviews()));
		mUserRating.setRating((float) property.getAverageExpediaRating());

		// update the summarized rates if they are available
		AvailabilityResponse availabilityResponse = ((TabletActivity) getActivity()).getRoomsAndRatesAvailability();
		updateSummarizedRates(availabilityResponse);

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
			clearOutData();
			mBookNowButton.setEnabled(false);
			break;
		case TabletActivity.EVENT_AVAILABILITY_SEARCH_ERROR:
			mEmptyAvailabilitySummaryTextView.setText((String) data);
			mAvailabilitySummaryContainer.setVisibility(View.GONE);
			mBookNowButton.setEnabled(false);
			break;
		case TabletActivity.EVENT_AVAILABILITY_SEARCH_COMPLETE:
			mEmptyAvailabilitySummaryTextView.setVisibility(View.GONE);
			mAvailabilitySummaryContainer.setVisibility(View.VISIBLE);
			updateSummarizedRates((AvailabilityResponse) data);
			break;
		case TabletActivity.EVENT_PROPERTY_SELECTED:
			updateViews((Property) data);
			break;
		case TabletActivity.EVENT_REVIEWS_QUERY_STARTED:
			mReviewsSection.removeAllViews();
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
		mEmptyAvailabilitySummaryTextView.setText(getString(R.string.room_rates_loading));
		mAvailabilitySummaryContainer.setVisibility(View.GONE);
	}

	private void clearOutData() {
		mAvailabilitySummaryContainer.removeAllViews();
		mBedTypeToMinRateMap.clear();
		mSummarizedRates.clear();
		mAvailableDoubleBedTypes.clear();
		mAvailableFullBedTypes.clear();
		mAvailableKingBedTypes.clear();
		mAvailableQueenBedTypes.clear();
		mAvailableTwinBedTypes.clear();
		mAvailableSingleBedTypes.clear();
		mAvailableRemainingBedTypes.clear();
		mMinimumRateAvailable = null;
	}

	private void layoutAvailabilitySummary() {

		if (mSummarizedRates.size() > 0) {
			View minPriceRow = mInflater.inflate(R.layout.snippet_min_room_price_summary, null);

			TextView minPrice = (TextView) minPriceRow.findViewById(R.id.min_price_text_view);
			StyleSpan span = new StyleSpan(Typeface.BOLD);
			String minPriceString = getString(R.string.min_room_price_template,
					StrUtils.formatHotelPrice(mMinimumRateAvailable.getDisplayRate()));
			Spannable str = new SpannableString(minPriceString);
			str.setSpan(span, 0, minPriceString.length(), 0);
			minPrice.setText(str);

			TextView perNighTextView = (TextView) minPriceRow.findViewById(R.id.per_night_text_view);
			if (Rate.showInclusivePrices()) {
				perNighTextView.setVisibility(View.GONE);
			}
			else {
				perNighTextView.setVisibility(View.VISIBLE);
			}
			mAvailabilitySummaryContainer.addView(minPriceRow);
		}

		for (int i = 0; i < MAX_SUMMARIZED_RATE_RESULTS && i < mSummarizedRates.size(); i++) {
			View summaryRow = mInflater.inflate(R.layout.snippet_availability_summary_row, null);
			TextView summaryDescription = (TextView) summaryRow.findViewById(R.id.availability_description_text_view);
			TextView priceTextView = (TextView) summaryRow.findViewById(R.id.availability_summary_price_text_view);

			Pair<BedTypeId, Rate> pair = mSummarizedRates.get(i);
			for (BedType bedType : pair.second.getBedTypes()) {
				if (bedType.bedTypeId == pair.first) {
					summaryDescription.setText(Html.fromHtml(getString(R.string.bed_type_start_value_template,
							bedType.bedTypeDescription)));
					break;
				}
			}
			priceTextView.setText(StrUtils.formatHotelPrice(pair.second.getDisplayRate()));

			mAvailabilitySummaryContainer.addView(summaryRow);
		}
	}

	private void updateSummarizedRates(AvailabilityResponse availabilityResponse) {
		clearOutData();
		if (availabilityResponse != null) {
			createBedTypeToMinRateMapping(availabilityResponse);
			clusterByBedType();
			summarizeRates();
			layoutAvailabilitySummary();
			mBookNowButton.setEnabled(true);
		}
		else {
			// since the data is not yet available,
			// make sure to clean out any old data and show the loading screen
			showLoadingForRates();
		}
	}

	private void addReviews(ReviewsResponse reviewsResponse) {
		mReviewsSection.removeAllViews();

		if (reviewsResponse == null) {
			return;
		}

		int tenDp = (int) Math.ceil(getActivity().getResources().getDisplayMetrics().density * 10);
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
				mReviewsSection.addView(row);
			}
		}
	}

	private void addHotelDescription(Property property) {
		mHotelDescriptionContainer.removeAllViews();

		String description = property.getDescriptionText();
		HotelDescription hotelDescription = new HotelDescription(getActivity());

		if (description != null && description.length() > 0) {
			hotelDescription.parseDescription(property.getDescriptionText());
		}

		int sectionCount = hotelDescription.getSections().size();
		int tenDp = (int) Math.ceil(getActivity().getResources().getDisplayMetrics().density * 10);
		for (int i = 0; sectionCount > 0; i++) {
			LinearLayout row = new LinearLayout(getActivity());
			row.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0);
			rowParams.weight = 1;
			row.setLayoutParams(rowParams);
			row.setPadding(0, tenDp, 0, 0);
			row.setWeightSum(2);

			for (int j = 0; j < MAX_DESCRIPTION_SECTIONS_PER_ROW && sectionCount > 0; j++) {
				DescriptionSection section = hotelDescription.getSections().get(
						i * MAX_DESCRIPTION_SECTIONS_PER_ROW + j);
				ViewGroup descriptionSection = (ViewGroup) mInflater.inflate(
						R.layout.snippet_hotel_description_section, null);

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
				params.weight = 1;
				descriptionSection.setLayoutParams(params);

				TextView descriptionTitle = (TextView) descriptionSection
						.findViewById(R.id.title_description_text_view);
				descriptionTitle.setText(section.title);

				TextView descriptionBody = (TextView) descriptionSection.findViewById(R.id.body_description_text_view);
				descriptionBody.setText(section.description);

				row.addView(descriptionSection);
				sectionCount--;
			}
			mHotelDescriptionContainer.addView(row);
		}

	}

	//----------------------------------------------
	// AVAILABILITY CLUSTERING BOOKKEEPING + METHODS
	//----------------------------------------------

	/**
	 * The clustering goal is as follows: To summarize the available
	 * rooms for a particular hotel into a distribution that represents the top 3
	 * most relevant rooms to the user.
	 * 
	 * The algorithm is as follows:
	 * a) Start off with pre-defined buckets of all bed types categorized by the size of the bed 
	 *    (eg. KING, QUEEN, DOUBLE, etc). The relative ordering of the hotels in the same bucket
	 *    is defined by the position of the enum {@link com.expedia.bookings.data.Rate.BedTypeId}
	 *   
	 * b) Go through all rates to create a mapping of bedType to the minimum possible rate
	 *    available for that bed type.
	 *    
	 * c) Cluster the minimum rates for each bedTypeId into the pre-defined buckets using 
	 *    priority queues that maintain the relative priority of bedTypes in the same bucket
	 * 
	 * d) Pick one hotel from each queue until all hotels have been picked. The order in which
	 *    to pick hotels from the bucket is: King > Queen > Double > Twin > Single > Full > Rest
	 *    
	 * e) Display as many as you'd like
	 */
	private HashMap<BedTypeId, Rate> mBedTypeToMinRateMap = new HashMap<BedTypeId, Rate>();
	private ArrayList<Pair<BedTypeId, Rate>> mSummarizedRates = new ArrayList<Pair<BedTypeId, Rate>>();
	private Rate mMinimumRateAvailable;

	/*
	 * This comparator is used to determine the relative priority between 
	 * bed types of the same kind (king, queen, full, single, etc)
	 */
	private Comparator<BedTypeId> BED_TYPE_COMPARATOR = new Comparator<Rate.BedTypeId>() {

		@Override
		public int compare(BedTypeId object1, BedTypeId object2) {
			if (object2 == null) {
				return 1;
			}
			return object1.compareTo(object2);
		}
	};

	/*
	 * These priority queues each keep track of the relative priority of bed types available within
	 * each defined bucket/category. The reason for this is so that we can display the relevant results
	 * to the user in the summary and give them a good distribution of the different kind of rooms available
	 */
	private PriorityQueue<BedTypeId> mAvailableKingBedTypes = new PriorityQueue<Rate.BedTypeId>(1, BED_TYPE_COMPARATOR);
	private PriorityQueue<BedTypeId> mAvailableQueenBedTypes = new PriorityQueue<Rate.BedTypeId>(1, BED_TYPE_COMPARATOR);
	private PriorityQueue<BedTypeId> mAvailableDoubleBedTypes = new PriorityQueue<Rate.BedTypeId>(1,
			BED_TYPE_COMPARATOR);
	private PriorityQueue<BedTypeId> mAvailableTwinBedTypes = new PriorityQueue<Rate.BedTypeId>(1, BED_TYPE_COMPARATOR);
	private PriorityQueue<BedTypeId> mAvailableFullBedTypes = new PriorityQueue<Rate.BedTypeId>(1, BED_TYPE_COMPARATOR);
	private PriorityQueue<BedTypeId> mAvailableSingleBedTypes = new PriorityQueue<Rate.BedTypeId>(1,
			BED_TYPE_COMPARATOR);
	private PriorityQueue<BedTypeId> mAvailableRemainingBedTypes = new PriorityQueue<Rate.BedTypeId>(1,
			BED_TYPE_COMPARATOR);

	private static final List<BedTypeId> KING_BED_TYPES;
	private static final List<BedTypeId> QUEEN_BED_TYPES;
	private static final List<BedTypeId> DOUBLE_BED_TYPES;
	private static final List<BedTypeId> TWIN_BED_TYPES;
	private static final List<BedTypeId> FULL_BED_TYPES;
	private static final List<BedTypeId> SINGLE_BED_TYPES;

	/*
	 * Creating the pre-defined groupings of bed types
	 */
	static {
		List<BedTypeId> bedTypes = new ArrayList<Rate.BedTypeId>();
		bedTypes.add(BedTypeId.ONE_KING_BED);
		bedTypes.add(BedTypeId.TWO_KING_BEDS);
		KING_BED_TYPES = Collections.unmodifiableList(new ArrayList<Rate.BedTypeId>(bedTypes));

		bedTypes.clear();
		bedTypes.add(BedTypeId.ONE_QUEEN_BED);
		bedTypes.add(BedTypeId.TWO_QUEEN_BEDS);
		QUEEN_BED_TYPES = Collections.unmodifiableList(new ArrayList<Rate.BedTypeId>(bedTypes));

		bedTypes.clear();
		bedTypes.add(BedTypeId.ONE_DOUBLE_BED);
		bedTypes.add(BedTypeId.TWO_DOUBLE_BEDS);
		DOUBLE_BED_TYPES = Collections.unmodifiableList(new ArrayList<Rate.BedTypeId>(bedTypes));

		bedTypes.clear();
		bedTypes.add(BedTypeId.ONE_TWIN_BED);
		bedTypes.add(BedTypeId.TWO_TWIN_BEDS);
		bedTypes.add(BedTypeId.THREE_TWIN_BEDS);
		bedTypes.add(BedTypeId.FOUR_TWIN_BEDS);
		TWIN_BED_TYPES = Collections.unmodifiableList(new ArrayList<Rate.BedTypeId>(bedTypes));

		bedTypes.clear();
		bedTypes.add(BedTypeId.ONE_FULL_BED);
		bedTypes.add(BedTypeId.TWO_FULL_BEDS);
		FULL_BED_TYPES = Collections.unmodifiableList(new ArrayList<Rate.BedTypeId>(bedTypes));

		bedTypes.clear();
		bedTypes.add(BedTypeId.ONE_SINGLE_BED);
		bedTypes.add(BedTypeId.TWO_SINGLE_BEDS);
		bedTypes.add(BedTypeId.THREE_SINGLE_BEDS);
		bedTypes.add(BedTypeId.FOUR_SINGLE_BEDS);
		SINGLE_BED_TYPES = Collections.unmodifiableList(new ArrayList<Rate.BedTypeId>(bedTypes));
	}

	/*
	 * This method creates a mapping from bed type to the minimum
	 * rate available for that bed type
	 */
	private void createBedTypeToMinRateMapping(AvailabilityResponse response) {
		mBedTypeToMinRateMap.clear();

		if (response.getRates() == null) {
			return;
		}

		for (Rate rate : response.getRates()) {
			if (rate.getBedTypes() == null) {
				continue;
			}
			for (BedType bedType : rate.getBedTypes()) {
				BedTypeId bedTypeId = bedType.bedTypeId;
				/*
				 * If a rate already exists for this bed type, 
				 * check if its the minimum possible rate
				 */
				if (mBedTypeToMinRateMap.containsKey(bedTypeId)) {
					Rate currentMinimumRate = mBedTypeToMinRateMap.get(bedTypeId);
					if (currentMinimumRate.getDisplayRate().getAmount() > rate.getDisplayRate().getAmount()) {
						mBedTypeToMinRateMap.put(bedTypeId, rate);
					}
				}
				else {
					mBedTypeToMinRateMap.put(bedTypeId, rate);
				}
			}
			// also keep track of the minimum of all rates to display\
			if (mMinimumRateAvailable == null
					|| mMinimumRateAvailable.getDisplayRate().getAmount() > rate.getDisplayRate().getAmount()) {
				mMinimumRateAvailable = rate;
			}
		}
	}

	/*
	 * This method clusters hotels into the above defined buckets
	 */
	private void clusterByBedType() {
		for (BedTypeId id : mBedTypeToMinRateMap.keySet()) {
			if (KING_BED_TYPES.contains(id)) {
				mAvailableKingBedTypes.add(id);
			}
			else if (QUEEN_BED_TYPES.contains(id)) {
				mAvailableQueenBedTypes.add(id);
			}
			else if (DOUBLE_BED_TYPES.contains(id)) {
				mAvailableDoubleBedTypes.add(id);
			}
			else if (TWIN_BED_TYPES.contains(id)) {
				mAvailableTwinBedTypes.add(id);
			}
			else if (FULL_BED_TYPES.contains(id)) {
				mAvailableFullBedTypes.add(id);
			}
			else if (SINGLE_BED_TYPES.contains(id)) {
				mAvailableSingleBedTypes.add(id);
			}
			else {
				mAvailableRemainingBedTypes.add(id);
			}
		}
	}

	/*
	 * This method picks one rate to display from each bucket
	 * and loops through this process until all queues are 
	 * emptied out and we have a list summarized rates ordered 
	 * by relevance
	 */
	private void summarizeRates() {
		while (!mAvailableKingBedTypes.isEmpty() || !mAvailableQueenBedTypes.isEmpty()
				|| !mAvailableTwinBedTypes.isEmpty() || !mAvailableSingleBedTypes.isEmpty()
				|| !mAvailableDoubleBedTypes.isEmpty() || !mAvailableFullBedTypes.isEmpty()
				|| !mAvailableRemainingBedTypes.isEmpty()) {
			addRateFromQueue(mAvailableKingBedTypes);
			addRateFromQueue(mAvailableQueenBedTypes);
			addRateFromQueue(mAvailableDoubleBedTypes);
			addRateFromQueue(mAvailableTwinBedTypes);
			addRateFromQueue(mAvailableSingleBedTypes);
			addRateFromQueue(mAvailableFullBedTypes);
			addRateFromQueue(mAvailableRemainingBedTypes);
		}
	}

	private void addRateFromQueue(PriorityQueue<BedTypeId> queue) {
		BedTypeId id = queue.poll();
		if (id != null) {
			mSummarizedRates.add(new Pair<Rate.BedTypeId, Rate>(id, mBedTypeToMinRateMap.get(id)));
		}
	}
}