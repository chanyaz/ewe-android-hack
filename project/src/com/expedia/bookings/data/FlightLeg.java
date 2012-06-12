package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof FlightLeg)) {
			return false;
		}

		return ((FlightLeg) o).getLegId().equals(mLegId);
	}

	////////////////////////////////////////////////////////////////////////
	// More meta retrieval methods

	// Returns the duration in milliseconds
	public long getDuration() {
		Calendar start = mSegments.get(0).mOrigin.getMostRelevantDateTime();
		Calendar end = mSegments.get(mSegments.size() - 1).mDestination.getMostRelevantDateTime();
		return end.getTimeInMillis() - start.getTimeInMillis();
	}

	// Returns all operating airlines for the flights in this leg
	public Set<String> getOperatingAirlines() {
		Set<String> airlines = new HashSet<String>();

		if (mSegments != null) {
			for (Flight flight : mSegments) {
				airlines.add(flight.getOperatingFlightCode().mAirlineCode);
			}
		}

		return airlines;
	}

	public String getAirlinesFormatted() {
		StringBuilder sb = new StringBuilder();
		for (String airlineCode : getOperatingAirlines()) {
			if (sb.length() != 0) {
				sb.append(", ");
			}

			sb.append(FlightStatsDbUtils.getAirline(airlineCode).mAirlineName);
		}
		return sb.toString();
	}
}
