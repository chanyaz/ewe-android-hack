package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import org.json.JSONException;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.BedType;
import com.expedia.bookings.data.Rate.BedTypeId;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.Session;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;

public class HotelDetailsFragment extends Fragment {

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// CONSTANTS
	//----------------------------------

	private static final int MAX_SUMMARIZED_RATE_RESULTS = 3;
	private static final String KEY_AVAILABILITY = "KEY_AVAILABILITY";

	//----------------------------------
	// VIEWS
	//----------------------------------

	private ViewGroup mAvailabilitySummaryContainer;

	//----------------------------------
	// OTHERS
	//----------------------------------

	private SearchParams mSearchParams;
	private Property mProperty;
	private Session mSession;
	private Context mContext;
	private LayoutInflater mInflater;

	private BackgroundDownloader mDownloader = BackgroundDownloader.getInstance();
	private AvailabilityResponse mAvailabilityResponse;
	private ReviewsResponse mReviewsResponse;

	//----------------------------------
	// THREADS/CALLBACKS
	//----------------------------------

	private Download mRoomAvailabilityDownload = new Download() {

		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext, mSession);
			return services.availability(mSearchParams, mProperty);
		}
	};

	private Download mReviewsDownload = new Download() {

		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext, mSession);
			return services.reviews(mProperty, 1, ReviewSort.HIGHEST_RATING_FIRST);
		}
	};

	private OnDownloadComplete mRoomAvailabilityCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			mAvailabilityResponse = (AvailabilityResponse) results;

			if (mAvailabilityResponse == null) {
				// TODO Need to figure out error handling
			}
			else if (mAvailabilityResponse.hasErrors()) {
				// TODO Need to figure out error handling	
			}
			else {
				createBedTypeToMinRateMapping();
				clusterByBedType();
				summarizeRates();
				layoutAvailabilitySummary();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////
	// LIFECYCLE EVENTS
	//////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle arguments = getArguments();
		//		mSearchParams = (SearchParams) JSONUtils.parseJSONObjectFromBundle(arguments, Codes.SEARCH_PARAMS,
		//				SearchParams.class);
		//		mProperty = (Property) JSONUtils.parseJSONObjectFromBundle(arguments, Codes.PROPERTY, Property.class);
		//		mSession = (Session) JSONUtils.parseJSONObjectFromBundle(arguments, Codes.SESSION, Session.class);
		try {
			mProperty = new Property();
			mProperty.fillWithTestData();
		}
		catch (JSONException e) {
			Log.i("Couldn't read dummy data");
		}

		mSearchParams = new SearchParams();
		mContext = getActivity().getApplicationContext();
		mInflater = getActivity().getLayoutInflater();

		mAvailabilitySummaryContainer = (ViewGroup) getView().findViewById(R.id.availability_summary_container);
		mDownloader.startDownload(KEY_AVAILABILITY, mRoomAvailabilityDownload, mRoomAvailabilityCallback);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_hotel_details, container, false);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

	private void layoutAvailabilitySummary() {
		for (int i = 0; i < MAX_SUMMARIZED_RATE_RESULTS && i < mSummarizedRates.size(); i++) {
			View summaryRow = mInflater.inflate(R.layout.snippet_availability_summary_row, null);
			TextView summaryDescription = (TextView) summaryRow.findViewById(R.id.availability_description_text_view);
			TextView priceTextView = (TextView) summaryRow.findViewById(R.id.availability_summary_price_text_view);

			Pair<BedTypeId, Rate> pair = mSummarizedRates.get(i);
			for (BedType bedType : pair.second.getBedTypes()) {
				if (bedType.bedTypeId == pair.first) {
					summaryDescription.setText(bedType.bedTypeDescription);
					break;
				}
			}
			priceTextView.setText(StrUtils.formatHotelPrice(pair.second.getDisplayRate()));

			mAvailabilitySummaryContainer.addView(summaryRow);
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
	private void createBedTypeToMinRateMapping() {
		mBedTypeToMinRateMap.clear();
		
		for (Rate rate : mAvailabilityResponse.getRates()) {
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