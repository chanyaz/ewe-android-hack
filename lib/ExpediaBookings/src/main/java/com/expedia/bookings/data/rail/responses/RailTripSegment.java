package com.expedia.bookings.data.rail.responses;

public class RailTripSegment extends RailSegment {
	public Duration duration;

	public int durationMinutes() {
		return duration.time;
	}
}
