package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.BedType;
import com.expedia.bookings.data.Rate.BedTypeId;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;

public class HotelDetailsFragment extends Fragment implements EventHandler {

	public static HotelDetailsFragment newInstance() {
		HotelDetailsFragment fragment = new HotelDetailsFragment();
		return fragment;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// CONSTANTS
	//----------------------------------

	private static final int MAX_SUMMARIZED_RATE_RESULTS = 3;

	//----------------------------------
	// VIEWS
	//----------------------------------

	private ViewGroup mAvailabilitySummaryContainer;
	private TextView mEmptyAvailabilitySummaryTextView;
	private TextView mHotelLocationTextView;
	private TextView mHotelNameTextView;
	private RatingBar mHotelRatingBar;
	private ArrayList<ImageView> propertyImages;

	//----------------------------------
	// OTHERS
	//----------------------------------
	private LayoutInflater mInflater;

	//////////////////////////////////////////////////////////////////////////////////////////
	// LIFECYCLE EVENTS
	//////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_details, container, false);
		mInflater = inflater;

		mHotelNameTextView = (TextView) view.findViewById(R.id.hotel_name_text_view);
		mHotelLocationTextView = (TextView) view.findViewById(R.id.hotel_address_text_view);
		mHotelRatingBar = (RatingBar) view.findViewById(R.id.hotel_rating_bar);
		mAvailabilitySummaryContainer = (ViewGroup) view.findViewById(R.id.availability_summary_container);
		mEmptyAvailabilitySummaryTextView = (TextView) view.findViewById(R.id.empty_summart_container);

		propertyImages = new ArrayList<ImageView>(4);
		propertyImages.add((ImageView) view.findViewById(R.id.big_left_property_image_view));
		propertyImages.add((ImageView) view.findViewById(R.id.top_right_property_image_view));
		propertyImages.add((ImageView) view.findViewById(R.id.bottom_right_property_image_view_1));
		propertyImages.add((ImageView) view.findViewById(R.id.bottom_right_property_image_view_2));
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
	// Views

	public void updateViews() {

		updateViews(((TabletActivity) getActivity()).getPropertyToDisplay());
	}

	public void updateViews(Property property) {
		mHotelNameTextView.setText(property.getName());
		String hotelAddressWithNewLine = StrUtils.formatAddress(property.getLocation(), StrUtils.F_STREET_ADDRESS
				+ StrUtils.F_CITY + StrUtils.F_STATE_CODE);
		mHotelLocationTextView.setText(hotelAddressWithNewLine.replace("\n", ", "));
		mHotelRatingBar.setRating((float) property.getHotelRating());

		// set the default thumbnails for all images
		for (ImageView imageView : propertyImages) {
			imageView.setImageResource(R.drawable.ic_row_thumb_placeholder);
		}

		for (int i = 0; i < property.getMediaCount() && i < propertyImages.size(); i++) {
			ImageCache.loadImage(property.getMedia(i).getUrl(), propertyImages.get(i));
		}

		// update the summarized rates if they are available
		AvailabilityResponse availabilityResponse = ((TabletActivity) getActivity()).getRoomsAndRatesAvailability();
		updateSummarizedRates(availabilityResponse);

	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_AVAILABILITY_SEARCH_STARTED:
			mEmptyAvailabilitySummaryTextView.setVisibility(View.VISIBLE);
			mEmptyAvailabilitySummaryTextView.setText(getString(R.string.room_rates_loading));
			mAvailabilitySummaryContainer.setVisibility(View.GONE);
			clearOutData();
			break;
		case TabletActivity.EVENT_AVAILABILITY_SEARCH_ERROR:
			mEmptyAvailabilitySummaryTextView.setText((String) data);
			mAvailabilitySummaryContainer.setVisibility(View.GONE);
			break;
		case TabletActivity.EVENT_AVAILABILITY_SEARCH_COMPLETE:
			mEmptyAvailabilitySummaryTextView.setVisibility(View.GONE);
			mAvailabilitySummaryContainer.setVisibility(View.VISIBLE);
			updateSummarizedRates(data);
			break;
		case TabletActivity.EVENT_DETAILS_OPENED:
		case TabletActivity.EVENT_PROPERTY_SELECTED:
			updateViews((Property) data);
			break;

		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

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

	private void updateSummarizedRates(Object data) {
		if (data != null) {
			createBedTypeToMinRateMapping((AvailabilityResponse) data);
			clusterByBedType();
			summarizeRates();
			layoutAvailabilitySummary();
		}
		else {
			// TODO: Remove data shown by Views in the case that summarized rates don't exist? 
			// (Or is this not necessary?)
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

		for (Rate rate : response.getRates()) {
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