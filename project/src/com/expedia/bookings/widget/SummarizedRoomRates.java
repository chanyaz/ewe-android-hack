package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import android.util.Pair;

import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.BedType;
import com.expedia.bookings.data.Rate.BedTypeId;

public class SummarizedRoomRates {

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
	private BedTypeId mMinimumRateBedTypeId;

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

	public void clearOutData() {
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

	public int numSummarizedRates() {
		return mSummarizedRates.size();
	}

	public Rate getRate(int position) {
		return mSummarizedRates.get(position).second;
	}

	public Pair<BedTypeId, Rate> getBedTypeToRatePair(int position) {
		return mSummarizedRates.get(position);
	}

	public Rate getStartingRate() {
		return mMinimumRateAvailable;
	}

	public void updateSummarizedRoomRates(AvailabilityResponse response) {
		createBedTypeToMinRateMapping(response);
		clusterByBedType();
		summarizeRates();
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
				mMinimumRateBedTypeId = rate.getBedTypes().iterator().next().bedTypeId;
			}
		}

		// don't keep track of the bed type to rate mapping  
		// considered the minimum rate mapping in the map 
		// since it will be handled separately 
		if (mMinimumRateAvailable != null) {
			mBedTypeToMinRateMap.remove(mMinimumRateBedTypeId);
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

		// first, add the minimum rate to the summarized rates
		if (mMinimumRateAvailable != null) {
			mSummarizedRates.add(new Pair<Rate.BedTypeId, Rate>(mMinimumRateBedTypeId, mMinimumRateAvailable));
		}

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
		Rate rate = mBedTypeToMinRateMap.get(id);
		if (id != null) {
			mSummarizedRates.add(new Pair<Rate.BedTypeId, Rate>(id, rate));
		}
	}

}
