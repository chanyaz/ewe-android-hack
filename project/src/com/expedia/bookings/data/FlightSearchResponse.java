package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

public class FlightSearchResponse extends SearchResponse {

	private List<FlightTrip> mTrips;

	public FlightSearchResponse() {
		mTrips = new ArrayList<FlightTrip>();
	}

	public void addTrip(FlightTrip trip) {
		mTrips.add(trip);
	}

	public FlightTrip getTrip(int position) {
		return mTrips.get(position);
	}

	public List<FlightTrip> getTrips() {
		return mTrips;
	}

	public int getTripCount() {
		return mTrips.size();
	}
}
