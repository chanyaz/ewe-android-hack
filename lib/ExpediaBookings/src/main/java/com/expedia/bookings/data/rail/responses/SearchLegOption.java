package com.expedia.bookings.data.rail.responses;

import java.util.List;

import com.expedia.bookings.utils.DateUtils;

//This class will go away once API team fixes duration to be consistent on search vs createTrip
public class SearchLegOption extends LegOption {

	public String duration;  //ISO duration format P[yY][mM][dD][T[hH][mM][s[.s]S]]
	public List<RailSearchSegment> travelSegmentList;

	public int durationMinutes() {
		return DateUtils.parseDurationMinutes(duration);
	}

	@Override
	public List<? extends RailSegment> getTravelSegments() {
		return travelSegmentList;
	}
}
