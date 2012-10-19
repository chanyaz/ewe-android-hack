package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.utils.CalendarUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class FlightLeg implements JSONable {

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

	public Waypoint getFirstWaypoint() {
		if (mSegments != null && mSegments.size() > 0) {
			return mSegments.get(0).mOrigin;
		}
		return null;
	}

	public Waypoint getLastWaypoint() {
		if (mSegments != null && mSegments.size() > 0) {
			return mSegments.get(mSegments.size() - 1).mDestination;
		}
		return null;
	}

	// Returns the duration in milliseconds
	public long getDuration() {
		Calendar start = mSegments.get(0).mOrigin.getMostRelevantDateTime();
		Calendar end = mSegments.get(mSegments.size() - 1).mDestination.getMostRelevantDateTime();
		return end.getTimeInMillis() - start.getTimeInMillis();
	}

	public int getDistanceInMiles() {
		int totalDistance = 0;
		if (mSegments != null) {
			for (Flight flight : mSegments) {
				totalDistance += flight.mDistanceToTravel;
			}
		}
		return totalDistance;
	}

	// Returns the *span* of the days involved with this trip.  If all the segments
	// are on the same day, then the span is 0.  Otherwise, it's 1+.  (This is used
	// for detecting multi-day flights.)
	public int getDaySpan() {
		Calendar start = mSegments.get(0).mOrigin.getMostRelevantDateTime();
		Calendar end = mSegments.get(mSegments.size() - 1).mDestination.getMostRelevantDateTime();
		return (int) CalendarUtils.getDaysBetween(start, end);
	}

	// Returns all operating airlines for the flights in this leg
	//
	// F1060: Returned as a LinkedHashSet, in order of flights
	public LinkedHashSet<String> getPrimaryAirlines() {
		LinkedHashSet<String> airlines = new LinkedHashSet<String>();

		if (mSegments != null) {
			for (Flight flight : mSegments) {
				airlines.add(flight.getPrimaryFlightCode().mAirlineCode);
			}
		}

		return airlines;
	}

	public String getAirlinesFormatted() {
		StringBuilder sb = new StringBuilder();
		for (String airlineCode : getPrimaryAirlines()) {
			if (sb.length() != 0) {
				sb.append(", ");
			}

			sb.append(FlightStatsDbUtils.getAirline(airlineCode).mAirlineName);
		}
		return sb.toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("legId", mLegId);
			JSONUtils.putJSONableList(obj, "segments", mSegments);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mLegId = obj.optString("legId");
		mSegments = JSONUtils.getJSONableList(obj, "segments", Flight.class);
		return true;
	}
}
