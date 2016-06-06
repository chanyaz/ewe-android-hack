package com.expedia.bookings.data.rail.responses;

import java.util.List;

//This class will go away once API team fixes duration to be consistent on search vs createTrip
public class RailLegOption extends LegOption {
	public Duration duration;
	public List<RailTripSegment> travelSegmentList;

	public int durationMinutes() {
		return duration.time;
	}

	@Override
	public List<? extends RailSegment> getTravelSegments() {
		return  travelSegmentList;
	}
}
