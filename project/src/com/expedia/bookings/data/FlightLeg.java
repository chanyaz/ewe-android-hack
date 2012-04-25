package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

public class FlightLeg {

	private String mLegId;

	private List<FlightSegment> mSegments = new ArrayList<FlightSegment>();

	public String getLegId() {
		return mLegId;
	}

	public void setLegId(String legId) {
		mLegId = legId;
	}

	public void addSegment(FlightSegment segment) {
		mSegments.add(segment);
	}

	public int getSegmentCount() {
		return mSegments.size();
	}

	public FlightSegment getSegment(int position) {
		return mSegments.get(position);
	}

	public List<FlightSegment> getSegments() {
		return mSegments;
	}

	////////////////////////////////////////////////////////////////////////
	// More meta retrieval methods
	
	public boolean hasMultipleAirlines() {
		String airlineCode = null;
		for (FlightSegment segment : mSegments) {
			if (airlineCode == null) {
				airlineCode = segment.getAirlineCode();
			}
			else if (!airlineCode.equals(segment.getAirlineCode())) {
				return true;
			}
		}
		return false;
	}

	public String getAirlineCode() {
		if (hasMultipleAirlines()) {
			return null;
		}
		else {
			return mSegments.get(0).getAirlineCode();
		}
	}
}
