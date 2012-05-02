package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import com.mobiata.flightlib.data.Airline;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class FlightLeg {

	private String mLegId;

	private List<Flight> mSegments = new ArrayList<Flight>();

	public String getLegId() {
		return mLegId;
	}

	public void setLegId(String legId) {
		mLegId = legId;
	}

	public void addSegment(Flight segment) {
		mSegments.add(segment);
	}

	public int getSegmentCount() {
		return mSegments.size();
	}

	public Flight getSegment(int position) {
		return mSegments.get(position);
	}

	public List<Flight> getSegments() {
		return mSegments;
	}

	////////////////////////////////////////////////////////////////////////
	// More meta retrieval methods

	public boolean hasMultipleAirlines() {
		String airlineCode = null;
		for (Flight segment : mSegments) {
			if (airlineCode == null) {
				airlineCode = segment.getPrimaryFlightCode().mAirlineCode;
			}
			else if (!airlineCode.equals(segment.getPrimaryFlightCode().mAirlineCode)) {
				return true;
			}
		}
		return false;
	}

	public String getAirlineName() {
		if (hasMultipleAirlines()) {
			return null;
		}
		else {
			Airline airline = FlightStatsDbUtils.getAirline(mSegments.get(0).getPrimaryFlightCode().mAirlineCode);
			return airline.mAirlineName;
		}
	}
}
