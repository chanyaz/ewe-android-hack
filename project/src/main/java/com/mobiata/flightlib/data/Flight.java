package com.mobiata.flightlib.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.time.util.JodaUtils;

public class Flight implements Comparable<Flight>, JSONable {

	private static final int TIME_CHANGE_THRESHHOLD = DateTimeConstants.MILLIS_PER_MINUTE * 10;


	// Flags for adding a flight code

	public boolean isSeatMapAvailable() {
		return mIsSeatMapAvailable;
	}

	public void setIsSeatMapAvailable(boolean mIsSeatMapAvailable) {
		this.mIsSeatMapAvailable = mIsSeatMapAvailable;
	}

	public static final int F_PRIMARY_AIRLINE_CODE = 1;
	public static final int F_OPERATING_AIRLINE_CODE = 2;

	// FlightStats status codes
	public static final String STATUS_SCHEDULED = "S";
	public static final String STATUS_UNKNOWN = "U";
	public static final String STATUS_REDIRECTED = "R";
	public static final String STATUS_DIVERTED = "D";
	public static final String STATUS_CANCELLED = "C";

	// Search mode used to obtain the flight result
	private static final int SEARCH_MODE_ROUTE = 1;

	// Version of the data, used for compatibility when changing format
	private static final int VERSION = 3;

	// Flight data
	private HashMap<String, FlightCode> mFlightCodes;

	// The primary airline is the code that the user input to find this
	// If the user input no code, it will be the same as the operating airline,
	// or if there is a marketing airline (wet lease or shared airline) it will
	// be that one.
	private String mPrimaryAirlineCode = null;

	// The operating airline is the one that runs the actual flight
	private String mOperatingAirlineCode = null;

	// Initialize Waypoints here as they likely throw exceptions if they are ever null.
	private Waypoint mOrigin = new Waypoint(Waypoint.ACTION_DEPARTURE);
	private Waypoint mDestination = new Waypoint(Waypoint.ACTION_ARRIVAL);
	private Waypoint mDiverted = null;

	// Takeoff and arrival times for last notification
	//  initialized to 0 here and to real values in updateFrom
	private long mLastNotifiedTakeoffTime = 0;
	private long mLastNotifiedArrivalTime = 0;

	private String mAirlineName;
	public String mStatusCode;
	private long mBearing = -1;
	public String mAircraftType;
	private String mAircraftTailNumber;
	private String mAircraftName;
	public String mBaggageClaim;
	public int mDistanceToTravel = -1;
	private int mDistanceTraveled = -1;
	private String departureTerminal;
	private String arrivalTerminal;
	private DateTime mSegmentDepartureTime;
	private DateTime mSegmentArrivalTime;

	// Parsing the ISOformat layover duration from the API response
	private String layoverDuration;

	// FlightStats data
	public int mFlightHistoryId = -1;
	private ArrayList<String> mRating;
	public float mOnTimePercentage = 0.0f;

	// For TripIt
	private int mTripId = -1;
	private String mTripName;
	private String mConfirmationNumber;
	private String tripItSeats;
	private String mCabinCode;

	private String mUniqueFlightId = null;
	//list of Seats for flight
	private List<Seat> seatList = new ArrayList<>();

	// General info for FlightTrack
	public long mLastUpdated = 0;
	private String mUserLabel;
	private String mUserNotes;

	// Determines if this is a repeating flight.
	private boolean mIsRepeating = false;

	// Determines if seats are available to choose from
	private boolean mIsSeatMapAvailable = false;
	// Time the flight was saved
	private long mTimeCreated;

	// Search mode used to obtain this flight, defaults to route
	private int mSearchMode = SEARCH_MODE_ROUTE;

	public Flight() {
		// If someone is creating a Flight from scratch, make sure
		// that its last updated time is now.
		mTimeCreated = mLastUpdated = DateTime.now().getMillis();
	}

	public String getAirlineName() {
		return mAirlineName;
	}

	public void setAirlineName(String airlineName) {
		this.mAirlineName = airlineName;
	}

	public void addFlightCode(FlightCode flightCode, int flags) {
		if (mFlightCodes == null) {
			mFlightCodes = new HashMap<>();
		}

		String airlineCode;
		if (flightCode.mAirlineCode == null) {
			airlineCode = FlightCode.NO_AIRLINE_CODE;
		}
		else {
			airlineCode = flightCode.mAirlineCode;
		}

		mFlightCodes.put(airlineCode, flightCode);
		if ((flags & F_PRIMARY_AIRLINE_CODE) != 0 || mPrimaryAirlineCode == null) {
			mPrimaryAirlineCode = airlineCode;
		}
		if ((flags & F_OPERATING_AIRLINE_CODE) != 0 || mOperatingAirlineCode == null) {
			mOperatingAirlineCode = airlineCode;
		}
	}

	@Nullable
	public FlightCode getPrimaryFlightCode() {
		if (mFlightCodes != null && mPrimaryAirlineCode != null) {
			return mFlightCodes.get(mPrimaryAirlineCode);
		}

		return null;
	}

	public FlightCode getOperatingFlightCode() {
		if (mFlightCodes != null && mOperatingAirlineCode != null) {
			return mFlightCodes.get(mOperatingAirlineCode);
		}
		return null;
	}

	@Nullable
	public String getDepartureTerminal() {
		return departureTerminal;
	}

	public void setDepartureTerminal(String terminal) {
		departureTerminal = terminal;
	}

	@Nullable
	public String getArrivalTerminal() {
		return arrivalTerminal;
	}

	public void setArrivalTerminal(String terminal) {
		arrivalTerminal = terminal;
	}

	public void setSegmentDepartureTime(String segmentDepartureTime) {
		DateTime parsedDate = DateTime.parse(segmentDepartureTime);
		mSegmentDepartureTime = parsedDate;
	}

	public DateTime getSegmentDepartureTime() {
		return mSegmentDepartureTime;
	}


	public void setSegmentArrivalTime(String segmentArrivalTime) {
		DateTime parsedDate = DateTime.parse(segmentArrivalTime);
		mSegmentArrivalTime = parsedDate;
	}

	public DateTime getSegmentArrivalTime() {
		return mSegmentArrivalTime;
	}

	public String getAssignedSeats() {
		if (seatList != null) {
			List<String> assignedSeats = new ArrayList<>();
			for (Seat seat : seatList) {
				assignedSeats.add(seat.getAssigned());
			}
			return TextUtils.join(", ", assignedSeats);
		}
		else {
			return null;
		}
	}

	public boolean hasSeats() {
		return !TextUtils.isEmpty(getAssignedSeats());
	}

	public boolean hasCabinCode() {
		return !TextUtils.isEmpty(mCabinCode);
	}

	public List<Seat> getSeats() {
		return seatList;
	}

	public String getFirstSixSeats(String seats) {
		String mSeats = seats;
		String[] seatArray = mSeats.split("\\s+");
		int count = seatArray.length;
		if (count < 7) {
			return mSeats;
		}
		else {
			int extraSeats = count - 6;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 6; i++) {
				sb.append(seatArray[i]);
				sb.append(" ");

			}
			sb.deleteCharAt(sb.length() - 2);
			sb.append("+");
			sb.append(extraSeats);
			return sb.toString();
		}
	}

	public void addSeat(Seat mSeat) {
		seatList.add(mSeat);
	}

	public void removeSeat(int index) {
		seatList.remove(index);
	}

	@Nullable
	public String getLayoverDuration() {
		return layoverDuration;
	}

	public void setLayoverDuration(String layover) {
		layoverDuration = layover;
	}

	/**
	 * Determines if we should be in "red alert" mode over this flight -
	 * for example, if it's cancelled, diverted or redirected.
	 *
	 * @return true if we should call red alert attention to this flight
	 */
	public boolean isRedAlert() {
		return (mStatusCode.equals(Flight.STATUS_CANCELLED) || mStatusCode.equals(Flight.STATUS_DIVERTED) || mStatusCode
			.equals(Flight.STATUS_REDIRECTED));
	}

	private boolean isTripItFlight() {
		return mTripId != -1;
	}

	public int compareTo(Flight another) {
		// Need to handle missing data cases
		if (mOrigin == null) {
			if (another.mOrigin == null) {
				return 0;
			}
			return 1;
		}
		else if (another.mOrigin == null) {
			return -1;
		}

		DateTime c1 = mOrigin.getMostRelevantDateTime();
		DateTime c2 = another.mOrigin.getMostRelevantDateTime();

		// Again, need to handle missing data cases
		if (c1 == null) {
			if (c2 == null) {
				return 0;
			}
			return 1;
		}
		else if (c2 == null) {
			return -1;
		}

		return c1.compareTo(c2);
	}

	public String getCabinCode() {
		return mCabinCode;
	}

	public void setCabinCode(String mCabinCode) {
		this.mCabinCode = mCabinCode;
	}

	public void setOriginWaypoint(Waypoint origin) {
		mOrigin = origin;
	}

	/**
	 * Returns the origin waypoint
	 *
	 * @return the origin waypoint
	 */
	public Waypoint getOriginWaypoint() {
		return mOrigin;
	}

	/**
	 * Specifically returns the destination airport, which may not be the arrival airport
	 * if the flight is redirected or diverted
	 */
	public Waypoint getDestinationWaypoint() {
		return mDestination;
	}

	/**
	 *
	 */
	public void setDestinationWaypoint(Waypoint destination) {
		mDestination = destination;
	}

	/**
	 * Returns the diverted waypoint
	 */
	public Waypoint getDivertedWaypoint() {
		return mDiverted;
	}

	/**
	 *
	 */
	public void setDivertedWaypoint(Waypoint diverted) {
		mDiverted = diverted;
	}

	/**
	 * Returns the arrival airport, which could either be
	 * the original destination or the diverted airport.
	 *
	 * @return the arrival airport (either destination or diverted)
	 */
	public Waypoint getArrivalWaypoint() {
		if (isDiverted()) {
			return mDiverted;
		}
		return mDestination;
	}

	/**
	 * Returns true if the flight is in DIVERTED status AND we know the diverted airport.
	 */
	private boolean isDiverted() {
		return Flight.STATUS_DIVERTED.equals(mStatusCode) && mDiverted != null;
	}

	/**
	 * @return the length of the trip in minutes, or 0 if unknown/invalid
	 */
	public int getTripTime() {
		int tripTime = 0;
		DateTime departureDateTime = mOrigin.getMostAccurateDateTime(Waypoint.POSITION_GATE);
		DateTime arrivalDateTime = getArrivalWaypoint().getMostAccurateDateTime(Waypoint.POSITION_GATE);

		if (departureDateTime == null && arrivalDateTime == null) {
			// If we don't even have that, compare unknown positions
			departureDateTime = mOrigin.getMostAccurateDateTime(Waypoint.POSITION_UNKNOWN);
			arrivalDateTime = getArrivalWaypoint().getMostAccurateDateTime(Waypoint.POSITION_UNKNOWN);
		}

		if (departureDateTime != null && arrivalDateTime != null) {
			tripTime = Minutes.minutesBetween(departureDateTime, arrivalDateTime).getMinutes();
		}

		return (tripTime >= 0) ? tripTime : 0;
	}

	/**
	 * Provides a safer, more intelligent manner to updating flights
	 * from other data.
	 *
	 * @param another
	 */
	public void updateFrom(Flight another) {
		if (another == null) {
			return;
		}

		//Initialize the last notified times if necessary

		if (mLastNotifiedTakeoffTime == 0) {
			mLastNotifiedTakeoffTime = mOrigin.getMostRelevantDateTime().getMillis();
		}
		if (mLastNotifiedArrivalTime == 0) {
			mLastNotifiedArrivalTime = getArrivalWaypoint().getMostRelevantDateTime().getMillis();
		}

		// If the change in times were large enough to set off a notification, update notification times
		if (Math.abs(mLastNotifiedTakeoffTime - another.mOrigin.getMostRelevantDateTime().getMillis())
			> TIME_CHANGE_THRESHHOLD) {
			this.mLastNotifiedTakeoffTime = another.mOrigin.getMostRelevantDateTime().getMillis();
		}
		if (Math.abs(mLastNotifiedArrivalTime
			- another.getArrivalWaypoint().getMostRelevantDateTime().getMillis()) > TIME_CHANGE_THRESHHOLD) {
			this.mLastNotifiedArrivalTime = another.getArrivalWaypoint().getMostRelevantDateTime().getMillis();
		}

		this.mFlightCodes = another.mFlightCodes;
		this.mOperatingAirlineCode = another.mOperatingAirlineCode;
		this.mOrigin = another.mOrigin;
		this.mDestination = another.mDestination;
		this.mDiverted = another.mDiverted;
		this.mStatusCode = another.mStatusCode;
		this.mBearing = another.mBearing;
		this.mAircraftTailNumber = another.mAircraftTailNumber;
		this.mRating = another.mRating;
		this.mOnTimePercentage = another.mOnTimePercentage;
		this.mBaggageClaim = another.mBaggageClaim;
		this.mDistanceToTravel = another.mDistanceToTravel;
		this.mDistanceTraveled = another.mDistanceTraveled;
		// Only update these fields if the updater has
		// info - otherwise, we want to preserve what
		// data we have here.  (For example, if a TripIt
		// flight is being updated via FareCompare data.)
		if (another.mFlightHistoryId != -1) {
			this.mFlightHistoryId = another.mFlightHistoryId;
		}
		if (another.mAircraftType != null) {
			this.mAircraftType = another.mAircraftType;
		}
		if (another.mAircraftName != null) {
			this.mAircraftName = another.mAircraftName;
		}

		updateTripItDataFrom(another);

		// Set the last updated time
		mLastUpdated = DateTime.now().getMillis();

	}
	public boolean hasRedEye() {
		DateTime originDay = getOriginWaypoint().getBestSearchDateTime();
		DateTime destinationDay = getDestinationWaypoint().getBestSearchDateTime();
		return JodaUtils.daysBetween(originDay,destinationDay) != 0;
	}

	public int daySpan() {
		DateTime originDay = getOriginWaypoint().getBestSearchDateTime();
		DateTime destinationDay = getDestinationWaypoint().getBestSearchDateTime();
		return JodaUtils.daysBetween(originDay,destinationDay);
	}

	private void updateTripItDataFrom(Flight another) {
		if (another.isTripItFlight()) {
			// This denotes the update flight as a TripIt flight - thus, update all TripIt items
			this.mTripId = another.mTripId;
			this.mConfirmationNumber = another.mConfirmationNumber;
			this.tripItSeats = another.tripItSeats;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (o.getClass() != this.getClass()) {
			return false;
		}

		// short-circuit if this is literally the same instance
		if (this == o) {
			return true;
		}

		Flight other = (Flight) o;

		if (mFlightHistoryId != -1 && mFlightHistoryId == other.mFlightHistoryId) {
			return true;
		}

		if (mUniqueFlightId != null && mUniqueFlightId.equals(other.mUniqueFlightId)) {
			return true;
		}

		if (getPrimaryFlightCode().equals(other.getPrimaryFlightCode())
			&& mOrigin.mAirportCode.equals(other.mOrigin.mAirportCode)
			&& mDestination.mAirportCode.equals(other.getDestinationWaypoint().mAirportCode)) {

			if (mIsRepeating && other.mIsRepeating) {
				// If these are both repeating flights, we don't need to verify that the date is correct.
				return true;
			}

			// Check the absolute time difference between time1 and time2 is < 3 hours
			DateTime time1 = mOrigin.getMostAccurateDateTime(Waypoint.POSITION_UNKNOWN);
			DateTime time2 = other.mOrigin.getMostAccurateDateTime(Waypoint.POSITION_UNKNOWN);
			if (Math.abs(Hours.hoursBetween(time1, time2).getHours()) < 3) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = 17;

		FlightCode primaryFlightCode = getPrimaryFlightCode();
		result = 31 * result + ((primaryFlightCode == null) ? 0 : primaryFlightCode.hashCode());
		result = 31 * result + ((mOrigin.mAirportCode == null) ? 0 : mOrigin.mAirportCode.hashCode());
		result = 31 * result + ((mDestination.mAirportCode == null) ? 0 : mDestination.mAirportCode.hashCode());
		result = 31 * result + (mIsRepeating ? 0 : 1);

		return result;
	}

	/**
	 * Custom toString, solely for debugging purposes
	 */
	@Override
	public String toString() {
		try {
			return toJson().toString(4);
		}
		catch (JSONException e) {
			return e.toString();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("version", VERSION);
			if (mFlightCodes != null && mFlightCodes.size() > 0) {
				JSONArray flightCodes = new JSONArray();
				for (FlightCode flightCode : mFlightCodes.values()) {
					flightCodes.put(flightCode.toJson());
				}
				obj.put("flightCodes", flightCodes);
			}
			obj.putOpt("primaryAirlineCode", mPrimaryAirlineCode);
			obj.putOpt("operatingAirlineCode", mOperatingAirlineCode);
			if (mOrigin != null) {
				obj.put("origin", mOrigin.convertToJson());
			}
			if (mDestination != null) {
				obj.put("destination", mDestination.convertToJson());
			}
			if (mDiverted != null) {
				obj.put("diverted", mDiverted.convertToJson());
			}
			if (mLastNotifiedTakeoffTime != 0) {
				obj.put("lastNotifiedTakeofftime", mLastNotifiedTakeoffTime);
			}
			if (mLastNotifiedArrivalTime != 0) {
				obj.put("lastNotifiedArrivalTime", mLastNotifiedArrivalTime);
			}
			obj.put("statusCode", mStatusCode);
			obj.put("bearing", mBearing);
			obj.put("flightHistoryId", mFlightHistoryId);
			obj.putOpt("tripId", mTripId);
			obj.putOpt("tripName", mTripName);
			obj.putOpt("confirmationNumber", mConfirmationNumber);
			obj.putOpt("seats", tripItSeats);

			if (mRating != null) {
				JSONArray rating = new JSONArray();
				int len = mRating.size();
				for (int a = 0; a < len; a++) {
					rating.put(mRating.get(a));
				}
				obj.put("rating", rating);
			}
			obj.putOpt("onTimePercentage", mOnTimePercentage);

			obj.putOpt("lastUpdated", mLastUpdated);
			obj.putOpt("aircraftType", mAircraftType);
			obj.putOpt("tailNumber", mAircraftTailNumber);
			obj.putOpt("aircraftName", mAircraftName);
			obj.putOpt("baggageClaim", mBaggageClaim);
			obj.putOpt("userLabel", mUserLabel);
			obj.putOpt("userNotes", mUserNotes);
			obj.putOpt("cabinCodeLocalized", mCabinCode);

			obj.putOpt("distanceToTravel", mDistanceToTravel);
			obj.putOpt("distanceTraveled", mDistanceTraveled);

			obj.putOpt("isRepeating", mIsRepeating);
			obj.putOpt("isSeatMapAvailable", mIsSeatMapAvailable);

			obj.putOpt("timeCreated", mTimeCreated);
			obj.putOpt("searchMode", mSearchMode);

			obj.putOpt("uniqueFlightId", mUniqueFlightId);

			obj.putOpt("departureTerminal", departureTerminal);
			obj.putOpt("arrivalTerminal", arrivalTerminal);

			JSONUtils.putJSONableList(obj, "seatList", seatList);
			obj.putOpt("layoverDuration", layoverDuration);
			obj.putOpt("airlineName", mAirlineName);
			return obj;
		}
		catch (JSONException e) {
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		try {
			int version = obj.getInt("version");

			if (version == 1) {
				FlightCode flightCode = new FlightCode();
				if (obj.has("airline")) {
					Airline airline = new Airline();
					airline.fromJson(obj.getJSONObject("airline"));
					flightCode.mAirlineCode = airline.mAirlineCode;
				}
				if (obj.has("flightNumber")) {
					flightCode.mNumber = obj.getString("flightNumber");
				}
				addFlightCode(flightCode, F_OPERATING_AIRLINE_CODE + F_PRIMARY_AIRLINE_CODE);
			}
			else {
				if (obj.has("flightCodes")) {
					JSONArray flightCodes = obj.getJSONArray("flightCodes");
					for (int a = 0; a < flightCodes.length(); a++) {
						FlightCode flightCode = new FlightCode();
						flightCode.fromJson(flightCodes.getJSONObject(a));
						addFlightCode(flightCode, 0);
					}
				}

				// 4539: Here we have to explicitly check if the primary/operating airline
				// codes are provided.  Otherwise we could accidentally setup a Flight with
				// no primary/operating airline code!
				if (obj.has("primaryAirlineCode")) {
					mPrimaryAirlineCode = obj.optString("primaryAirlineCode", null);
				}
				if (obj.has("operatingAirlineCode")) {
					mOperatingAirlineCode = obj.optString("operatingAirlineCode", null);
				}
			}

			if (obj.has("origin")) {
				mOrigin = new Waypoint(obj.getJSONObject("origin"));
				if (mOrigin.mAction == Waypoint.ACTION_UNKNOWN) {
					mOrigin.mAction = Waypoint.ACTION_DEPARTURE;
				}
			}
			if (obj.has("destination")) {
				mDestination = new Waypoint(obj.getJSONObject("destination"));
				if (mDestination.mAction == Waypoint.ACTION_UNKNOWN) {
					mDestination.mAction = Waypoint.ACTION_ARRIVAL;
				}
			}
			if (obj.has("diverted")) {
				mDiverted = new Waypoint(obj.getJSONObject("diverted"));
			}
			if (obj.has("lastNotifiedArrivalTime")) {
				mLastNotifiedArrivalTime = obj.getLong("lastNotifiedArrivalTime");
			}
			if (obj.has("lastNotifiedTakeoffTime")) {
				mLastNotifiedTakeoffTime = obj.getLong("lastNotifiedTakeoffTime");
			}
			mStatusCode = obj.getString("statusCode");
			mBearing = obj.getLong("bearing");
			mFlightHistoryId = obj.getInt("flightHistoryId");
			mTripId = obj.optInt("tripId", -1);
			mTripName = obj.optString("tripName", null);
			mConfirmationNumber = obj.optString("confirmationNumber", null);
			tripItSeats = obj.optString("seats", null);

			if (obj.has("rating")) {
				ArrayList<String> rating = mRating = new ArrayList<>();
				JSONArray ratingArr = obj.getJSONArray("rating");
				int len = ratingArr.length();
				for (int a = 0; a < len; a++) {
					rating.add(ratingArr.getString(a));
				}
			}
			mOnTimePercentage = (float) obj.optDouble("onTimePercentage", 0.0f);

			mLastUpdated = obj.optLong("lastUpdated", 0);
			mAircraftType = obj.optString("aircraftType", null);
			mAircraftTailNumber = obj.optString("tailNumber", null);
			mAircraftName = obj.optString("aircraftName", null);
			mBaggageClaim = obj.optString("baggageClaim", null);
			mUserLabel = obj.optString("userLabel", null);
			mUserNotes = obj.optString("userNotes", null);
			mCabinCode = obj.optString("cabinCodeLocalized", null);

			seatList = JSONUtils.getJSONableList(obj, "seatList", Seat.class);

			mDistanceToTravel = obj.optInt("distanceToTravel", -1);
			mDistanceTraveled = obj.optInt("distanceTraveled", -1);

			mIsRepeating = obj.optBoolean("isRepeating", false);
			mIsSeatMapAvailable = obj.optBoolean("isSeatMapAvailable", false);

			//If we lost the time created, we can at least hope to get something from the last updated time
			// Otherwise it will just default with that to 0.
			mTimeCreated = obj.optLong("timeCreated", mLastUpdated);

			// if we lost track of the search mode, default to by route
			mSearchMode = obj.optInt("searchMode", SEARCH_MODE_ROUTE);

			mUniqueFlightId = obj.optString("uniqueFlightId", null);

			departureTerminal = obj.optString("departureTerminal", null);
			arrivalTerminal = obj.optString("arrivalTerminal", null);

			layoverDuration = obj.optString("layoverDuration", null);
			mAirlineName = obj.optString("airlineName", null);

			return true;
		}
		catch (JSONException e) {
			return false;
		}
	}
}
