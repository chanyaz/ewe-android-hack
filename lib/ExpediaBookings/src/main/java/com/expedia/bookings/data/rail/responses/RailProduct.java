package com.expedia.bookings.data.rail.responses;

import com.expedia.bookings.utils.CollectionUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RailProduct {
	public List<Integer> legOptionIndexList;
	public List<PassengerSegmentFare> segmentFareDetailList = new ArrayList<>();
	public List<RailCard> fareQualifierList = new ArrayList<>();
	public boolean refundable;
	public List<String> refundableRules;
	public List<String> fareNotes;
	public String aggregatedCarrierServiceClassDisplayName;
	public String aggregatedCarrierFareClassDisplayName;
	public String aggregatedFareDescription;
	public boolean openReturn;

	@NotNull
	public Map<Integer, PassengerSegmentFare> getSegmentToFareMapping() {
		Map<Integer, PassengerSegmentFare> mapping = new HashMap<>();
		for (PassengerSegmentFare segmentFare : segmentFareDetailList) {
			mapping.put(segmentFare.travelSegmentIndex, segmentFare);
		}
		return Collections.unmodifiableMap(mapping);
	}

	public boolean hasRailCardApplied() {
		return CollectionUtils.isNotEmpty(fareQualifierList);
	}
}
