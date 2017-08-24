package com.expedia.bookings.data.rail.responses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.utils.CollectionUtils;

public class RailProduct {
	public final List<Integer> legOptionIndexList;
	public final List<PassengerSegmentFare> segmentFareDetailList = new ArrayList();
	public final List<RailCard> fareQualifierList = new ArrayList();
	public boolean refundable;
	public List<String> refundableRules;
	public List<String> fareNotes;
	public String aggregatedCarrierServiceClassDisplayName;
	public final String aggregatedCarrierFareClassDisplayName;
	public String aggregatedFareDescription;
	public final boolean openReturn;

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
