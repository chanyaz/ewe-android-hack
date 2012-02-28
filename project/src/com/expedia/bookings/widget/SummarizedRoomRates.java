package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

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
	 * These priority queues each keep track of the relative priority of bed types available within
	 * each defined bucket/category. The reason for this is so that we can display the relevant results
	 * to the user in the summary and give them a good distribution of the different kind of rooms available
	 */
	private Map<BedTypeGrouping, PriorityQueue<BedTypeId>> mAvailableBedTypes = new HashMap<SummarizedRoomRates.BedTypeGrouping, PriorityQueue<BedTypeId>>();

	private static enum BedTypeGrouping {
		KING_BEDS,
		QUEEN_BEDS,
		DOUBLE_BEDS,
		TWIN_BEDS,
		FULL_BEDS,
		SINGLE_BEDS,
		OTHER,
		UNKNOWN;
	}

	private static final Map<BedTypeGrouping, Set<BedTypeId>> BED_TYPE_GROUPINGS;

	/*
	 * Creating the pre-defined groupings of bed types
	 */
	static {
		BED_TYPE_GROUPINGS = new HashMap<SummarizedRoomRates.BedTypeGrouping, Set<BedTypeId>>();

		BED_TYPE_GROUPINGS.put(BedTypeGrouping.KING_BEDS,
				createBedTypeGrouping(BedTypeId.ONE_KING_BED, BedTypeId.TWO_KING_BEDS, BedTypeId.THREE_KING_BEDS,
						BedTypeId.FOUR_KING_BEDS, BedTypeId.ONE_KING_ONE_SOFA));

		BED_TYPE_GROUPINGS.put(BedTypeGrouping.QUEEN_BEDS,
				createBedTypeGrouping(BedTypeId.ONE_QUEEN_BED, BedTypeId.TWO_QUEEN_BEDS, BedTypeId.THREE_QUEEN_BEDS,
						BedTypeId.FOUR_QUEEN_BEDS, BedTypeId.ONE_QUEEN_ONE_SOFA));

		BED_TYPE_GROUPINGS.put(BedTypeGrouping.DOUBLE_BEDS,
				createBedTypeGrouping(BedTypeId.ONE_DOUBLE_BED, BedTypeId.TWO_DOUBLE_BEDS,
						BedTypeId.ONE_DOUBLE_ONE_SINGLE, BedTypeId.ONE_DOUBLE_TWO_SINGLES));

		BED_TYPE_GROUPINGS.put(BedTypeGrouping.TWIN_BEDS,
				createBedTypeGrouping(BedTypeId.ONE_TWIN_BED, BedTypeId.TWO_TWIN_BEDS, BedTypeId.THREE_TWIN_BEDS,
						BedTypeId.FOUR_TWIN_BEDS));

		BED_TYPE_GROUPINGS.put(BedTypeGrouping.FULL_BEDS,
				createBedTypeGrouping(BedTypeId.ONE_FULL_BED, BedTypeId.TWO_FULL_BEDS));

		BED_TYPE_GROUPINGS.put(BedTypeGrouping.SINGLE_BEDS,
				createBedTypeGrouping(BedTypeId.ONE_SINGLE_BED, BedTypeId.TWO_SINGLE_BEDS, BedTypeId.THREE_SINGLE_BEDS,
						BedTypeId.FOUR_SINGLE_BEDS));

		BED_TYPE_GROUPINGS.put(BedTypeGrouping.OTHER,
				createBedTypeGrouping(BedTypeId.ONE_BED, BedTypeId.TWO_BEDS, BedTypeId.THREE_BEDS, BedTypeId.FOUR_BEDS,
						BedTypeId.ONE_TRUNDLE_BED, BedTypeId.ONE_MURPHY_BED, BedTypeId.ONE_BUNK_BED,
						BedTypeId.ONE_SLEEPER_SOFA, BedTypeId.TWO_SLEEPER_SOFAS, BedTypeId.THREE_SLEEPER_SOFAS,
						BedTypeId.JAPENESE_FUTON));

		BED_TYPE_GROUPINGS.put(BedTypeGrouping.UNKNOWN, createBedTypeGrouping(BedTypeId.UNKNOWN));
	}

	private static Set<BedTypeId> createBedTypeGrouping(BedTypeId... bedTypeIds) {
		Set<BedTypeId> grouping = new HashSet<Rate.BedTypeId>();
		for (BedTypeId bedTypeId : bedTypeIds) {
			grouping.add(bedTypeId);
		}
		return grouping;
	}

	public void clearOutData() {
		mBedTypeToMinRateMap.clear();
		mSummarizedRates.clear();
		mAvailableBedTypes.clear();
		mMinimumRateAvailable = null;
	}

	public int numSummarizedRates() {
		return mSummarizedRates.size();
	}

	public BedTypeId getBedTypeId(int position) {
		return mSummarizedRates.get(position).first;
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
			for (BedTypeGrouping grouping : BedTypeGrouping.values()) {
				if (BED_TYPE_GROUPINGS.get(grouping).contains(id)) {
					PriorityQueue<BedTypeId> queue = mAvailableBedTypes.get(grouping);

					if (queue == null) {
						queue = new PriorityQueue<Rate.BedTypeId>(2);
						mAvailableBedTypes.put(grouping, queue);
					}

					queue.add(id);
					break;
				}
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

		boolean hasMore;
		do {
			hasMore = false;
			for (BedTypeGrouping grouping : BedTypeGrouping.values()) {
				hasMore = hasMore || addRateFromQueue(mAvailableBedTypes.get(grouping));
			}
		}
		while (hasMore);
	}

	private boolean addRateFromQueue(PriorityQueue<BedTypeId> queue) {
		if (queue != null) {
			BedTypeId id = queue.poll();
			Rate rate = mBedTypeToMinRateMap.get(id);
			if (id != null) {
				mSummarizedRates.add(new Pair<Rate.BedTypeId, Rate>(id, rate));
			}

			return !queue.isEmpty();
		}

		return false;
	}

}
