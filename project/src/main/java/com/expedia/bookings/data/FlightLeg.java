package com.expedia.bookings.data;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.trips.ItinShareInfo;
import com.expedia.bookings.data.trips.ItinShareInfo.ItinSharable;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.maps.MapUtils;
import com.mobiata.flightlib.data.Airline;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.data.Waypoint;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FlightLeg implements JSONable, ItinSharable {

	private String mLegId;

	private boolean mUserCheckedIn = false;

	private ItinShareInfo mShareInfo = new ItinShareInfo();

	private List<Flight> mSegments = new ArrayList<>();

	private String mBaggageFeesUrl;

	private String mDuration;

	private String airlineLogoURL;

	private boolean mHasBagFee = false;

	private String mFareType;

	private String mAirLineCode;

	private boolean mIsFreeCancellable;

	// split ticket fare details
	private Money mTotalFare;

	public Money getTotalFare() {
		return mTotalFare;
	}

	public void setTotalFare(Money totalFare) {
		this.mTotalFare = totalFare;
	}

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

	public String getFareType() {
		return mFareType;
	}

	public void setFareType(String fareType) {
		mFareType = fareType;
	}

	public String getBaggageFeesUrl() {
		return mBaggageFeesUrl;
	}

	public void setBaggageFeesUrl(String baggageFeesUrl) {
		mBaggageFeesUrl = baggageFeesUrl;
	}

	public void setLegDuration(String duration) {
		mDuration = duration;
	}

	public String getLegDuration() {
		return mDuration;
	}

	public int durationMinutes() {
		try {
			if (Strings.isNotEmpty(mDuration)) {
				return DateUtils.parseDurationMinutesFromISOFormat(mDuration);
			}
		}
		catch (IllegalArgumentException e) {
			Log.e("unsupported parsing format", e);
		}
		return 0;
	}

	public boolean hasBagFee() {
		return mHasBagFee;
	}

	public void setHasBagFee(boolean hasBagFee) {
		mHasBagFee = hasBagFee;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof FlightLeg)) {
			return false;
		}

		return ((FlightLeg) o).getLegId().equals(mLegId);
	}

	public void setUserCheckedIn(boolean mUserCheckedIn) {
		this.mUserCheckedIn = mUserCheckedIn;
	}

	public boolean isUserCheckedIn() {
		return mUserCheckedIn;
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
			return mSegments.get(0).getOriginWaypoint();
		}
		return null;
	}

	public Waypoint getLastWaypoint() {
		if (mSegments != null && mSegments.size() > 0) {
			return mSegments.get(mSegments.size() - 1).getDestinationWaypoint();
		}
		return null;
	}

	// Returns the duration in milliseconds
	public long getDurationFromWaypoints() {
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
				else if (flight.getOriginWaypoint() != null && flight.getDestinationWaypoint() != null) {
					origin = flight.getOriginWaypoint().getAirport();
					destination = flight.getDestinationWaypoint().getAirport();

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
		LinkedHashSet<String> airlines = new LinkedHashSet<>();

		if (mSegments == null) {
			Log.w("FlightLeg", "Attempting to retrieve primaryAirlines with null mSegments");
			return airlines;
		}

		for (Flight flight : mSegments) {
			FlightCode code = flight.getPrimaryFlightCode();
			if (code == null) {
				Log.w("FlightLeg", "Attempting to retrieve primaryAirlines with null code");
			}
			else {
				airlines.add(code.mAirlineCode);
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

		FlightCode code = mSegments.get(0).getPrimaryFlightCode();
		if (code == null) {
			return null;
		}

		return code.mAirlineCode;
	}

	public String getPrimaryAirlineNamesFormatted() {
		if (mSegments == null) {
			Log.w("FlightLeg", "Attempting to retrieve primaryAirlineNamesFormatted with null mSegments");
			return "";
		}

		Set<String> airlineNames = new LinkedHashSet<>();
		for (int i = 0; i < mSegments.size(); i++) {
			// 1. FlightCode.mAirlineCode has precedence as this is information given from API
			// 2. Fallback to use FS.db if we don't have information from the API.
			FlightCode code = mSegments.get(i).getPrimaryFlightCode();
			if (code != null) {
				Airline airline = Db.getAirline(code.mAirlineCode);
				if (Strings.isNotEmpty(code.mAirlineName)) {
					airlineNames.add(code.mAirlineName);
				}
				else if (Strings.isNotEmpty(airline.mAirlineName)) {
					airlineNames.add(airline.mAirlineName);
				}
				else {
					Log.w("FlightLeg",
						"FlightCode airlineName empty and attempted to retrieve airlineName from DB with bad (likely null) airline code");
				}
			}
			else {
				Log.w("FlightLeg", "Attempted to retrieve primaryAirlineNamesFormatted with null primaryFlightCode");
			}
		}
		return Strings.joinWithoutEmpties(", ", airlineNames);
	}

	public boolean isSpirit() {
		for (String airline : getPrimaryAirlines()) {
			if (airline.equalsIgnoreCase("NK")) {
				return true;
			}
		}
		return false;
	}

	public boolean isFreeCancellable() {
		return mIsFreeCancellable;
	}

	public void setIsFreeCancellable(boolean isFreeCancellable) {
		this.mIsFreeCancellable = isFreeCancellable;
	}

	public String getAirlineLogoURL() {
		return airlineLogoURL;
	}

	public void setAirlineLogoURL(String airlineLogoURL) {
		this.airlineLogoURL = airlineLogoURL;
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
			obj.putOpt("baggageFeesUrl", mBaggageFeesUrl);
			obj.putOpt("duration", mDuration);
			obj.putOpt("hasBagFee", mHasBagFee);
			obj.putOpt("fareType", mFareType);
			obj.putOpt("isFreeCancellable", mIsFreeCancellable);
			obj.put("userCheckedIn", mUserCheckedIn);
			obj.putOpt("airlineLogoURL", airlineLogoURL);
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
		mBaggageFeesUrl = obj.optString("baggageFeesUrl");
		mDuration = obj.optString("duration");
		mHasBagFee = obj.optBoolean("hasBagFee", false);
		mFareType = obj.optString("fareType", "");
		mIsFreeCancellable = obj.optBoolean("isFreeCancellable");
		mUserCheckedIn = obj.optBoolean("userCheckedIn");
		airlineLogoURL = obj.optString("airlineLogoURL");
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
