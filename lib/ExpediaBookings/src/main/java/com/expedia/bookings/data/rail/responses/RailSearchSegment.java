package com.expedia.bookings.data.rail.responses;

import com.expedia.bookings.utils.DateUtils;

//This class will go away once API team fixes duration to be consistent on search vs createTrip
public class RailSearchSegment extends RailSegment {
	public String duration;  //ISO duration format P[yY][mM][dD][T[hH][mM][s[.s]S]]

	public int durationMinutes() {
		return DateUtils.parseDurationMinutes(duration);
	}
}
