package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.trips.ItinShareInfo;
import com.expedia.bookings.data.trips.ItinShareInfo.ItinSharable;
import com.expedia.bookings.utils.FlightUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.maps.MapUtils;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;

public class FlightLeg implements JSONable, ItinSharable {

	private String mLegId;

	private ItinShareInfo mShareInfo = new ItinShareInfo();

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

	public Airport getAirport(boolean departureAirport) {
		if (departureAirport) {
			return getFirstWaypoint().getAirport();
		}
		else {
			return getLastWaypoint().getAirport();
		}
	}

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
		DateTime start = new DateTime(getFirstWaypoint().getMostRelevantDateTime());
		DateTime end = new DateTime(getLastWaypoint().getMostRelevantDateTime());
		Duration duration = new Duration(start, end);
		return duration.getMillis();
	}

	public int getDistanceInMiles() {
		int totalDistance = 0;
		if (mSegments != null) {
			Airport origin;
			Airport destination;

			for (Flight flight : mSegments) {
				if (flight.mDistanceToTravel > 0) {
					totalDistance += flight.mDistanceToTravel;
				}
				else if (flight.mOrigin != null && flight.mDestination != null) {
					origin = flight.mOrigin.getAirport();
					destination = flight.mDestination.getAirport();

					// Airports shouldn't be null here, but we'll check anyway since this
					// else if block should be relatively uncommon
					if (origin != null && destination != null) {
						totalDistance += MapUtils.getDistance(origin.getLatitude(), origin.getLongitude(),
							destination.getLatitude(), destination.getLongitude());
					}
				}
			}
		}
		return totalDistance;
	}

	// Returns the *span* of the days involved with this trip.  If all the segments
	// are on the same day, then the span is 0.  Otherwise, it's 1+.  (This is used
	// for detecting multi-day flights.)
	public int getDaySpan() {
		DateTime start = new DateTime(getFirstWaypoint().getMostRelevantDateTime());
		DateTime end = new DateTime(getLastWaypoint().getMostRelevantDateTime());
		return JodaUtils.daysBetween(start, end);
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

	/**
	 * Returns the airline code for the *first* segment of this flight leg.
	 *
	 * @return string, or null if there are no segments.
	 */
	public String getFirstAirlineCode() {
		if (mSegments == null || mSegments.size() == 0) {
			return null;
		}

		return mSegments.get(0).getPrimaryFlightCode().mAirlineCode;
	}

	public String getAirlinesFormatted() {
		return FlightUtils.getFormattedAirlinesList(getPrimaryAirlines());
	}

	public boolean isSpirit() {
		for (String airline : getPrimaryAirlines()) {
			if (airline.equalsIgnoreCase("NK")) {
				return true;
			}
		}
		return false;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("legId", mLegId);
			JSONUtils.putJSONableList(obj, "segments", mSegments);
			JSONUtils.putJSONable(obj, "shareInfo", mShareInfo);
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
		mShareInfo = JSONUtils.getJSONable(obj, "shareInfo", ItinShareInfo.class);
		mShareInfo = mShareInfo == null ? new ItinShareInfo() : mShareInfo;
		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	// ItinSharable

	@Override
	public ItinShareInfo getShareInfo() {
		return mShareInfo;
	}

	@Override
	public boolean getSharingEnabled() {
		return true;
	}
}
