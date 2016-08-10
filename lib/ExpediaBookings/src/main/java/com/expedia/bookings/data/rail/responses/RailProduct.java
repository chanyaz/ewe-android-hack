package com.expedia.bookings.data.rail.responses;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class RailProduct {
	public List<Integer> legOptionIndexList;
	public List<FareBreakdown> fareBreakdownList;
	public boolean refundable;
	public List<String> refundableRules;
	public List<String> fareNotes;
	public String aggregatedCarrierServiceClassDisplayName;
	public String aggregatedCarrierFareClassDisplayName;
	public String aggregatedFareDescription;

	public static class FareBreakdown {
		public List<PassengerFare> passengerFareList;
	}

	public static class PassengerFare {
		public Integer passengerIndex;
		public List<PassengerSegmentFare> passengerSegmentFareList;

		@NotNull
		public Map<Integer, PassengerSegmentFare> getSegmentToFareMapping() {
			Map<Integer, PassengerSegmentFare> mapping = new HashMap<>();
			for (PassengerSegmentFare segmentFare : passengerSegmentFareList) {
				mapping.put(segmentFare.travelSegmentIndex, segmentFare);
			}
			return Collections.unmodifiableMap(mapping);
		}
	}
}
