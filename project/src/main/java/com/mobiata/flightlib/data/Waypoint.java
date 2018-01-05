package com.mobiata.flightlib.data;

import java.util.Iterator;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;
import com.mobiata.flightlib.utils.DateTimeUtils;

/**
 * Represents a waypoint on a plane trip - either the origin or the destination.
 * <p/>
 * The concepts behind the date/times stored is somewhat complex.  Each date/time
 * has a POSITION and an ACCURACY.  The POSITION is where the plane is at the
 * particular date/time - be it at the gate, runway, or unknown (which is used when
 * position is not specified).  The ACCURACY is how accurate the date/time is - from
 * published to actual.
 */
public class Waypoint implements JSONable {

	public static final int F_DEPARTURE = 1;
	public static final int F_ARRIVAL = 2;

	// Accuracy times
	public static final int ACCURACY_UNKNOWN = 0;
	public static final int ACCURACY_SCHEDULED = 2;
	public static final int ACCURACY_ESTIMATED = 3;
	public static final int ACCURACY_ACTUAL = 4;

	// Position times
	public static final int POSITION_UNKNOWN = 0;
	public static final int POSITION_GATE = 1;
	public static final int POSITION_RUNWAY = 2;

	// Useful for calculating things.  Keep updated.
	private static final int NUM_POSITIONS = 3;
	private static final int NUM_ACCURACIES = 5;

	// Waypoint actions
	// Represents whether this is a departing waypoint or an arriving one
	public static final int ACTION_UNKNOWN = -1;
	public static final int ACTION_DEPARTURE = 1;
	public static final int ACTION_ARRIVAL = 2;
	public static final int ACTION_DIVERTED = 3;

	// Version of the data, used for compatibility when changing format
	private static final int VERSION = 5;

	// Waypoint data
	public String mAirportCode;
	public String mCity;
	private String mGate;
	private String mTerminal;
	public int mAction = -1;

	// Date times for the flight at this waypoint
	private final DateTime[][] mDateTimes = new DateTime[NUM_POSITIONS][NUM_ACCURACIES];
	private final String[][] mDateTimeStrs = new String[NUM_POSITIONS][NUM_ACCURACIES];

	// Alternative method for storing date times (using seconds
	// from epoch + gmt time zone offset).
	private final long[][] mMillisFromEpoch = new long[NUM_POSITIONS][NUM_ACCURACIES];
	private final int[][] mTzOffsetMillis = new int[NUM_POSITIONS][NUM_ACCURACIES];

	public Waypoint(int action) {
		mAction = action;
	}

	public Waypoint(JSONObject obj) throws JSONException {
		loadFromJson(obj);
	}

	public Airport getAirport() {
		return FlightStatsDbUtils.getAirport(mAirportCode);
	}

	public void setGate(String gate) {
		mGate = gate;
	}

	public void setTerminal(String terminal) {
		mTerminal = terminal;
	}

	public String getGate() {
		// the null check is redundant, but prevents it from errorring
		// when trying to access string methods
		if (mGate == null || mGate.equals("-") || mGate.length() <= 0) {
			return null;
		}
		else {
			return mGate;
		}
	}

	public String getTerminal() {
		// the null check is redundant, but prevents it from errorring
		// when trying to access string methods
		if (mTerminal == null || mTerminal.equals("-") || mTerminal.length() <= 0) {
			return null;
		}
		else {
			return mTerminal;
		}
	}

	public boolean hasTerminal() {
		return (getTerminal() != null);
	}

	public boolean hasGate() {
		return (getGate() != null);
	}

	/**
	 * Adds a date/time to the waypoint.
	 *
	 * @param position where the plane is
	 * @param accuracy the accuracy of the reading
	 */
	public void addDateTime(int position, int accuracy, String dateStr) {
		mDateTimes[position][accuracy] = null;

		if (dateStr != null && dateStr.length() > 9) {
			// Correct for non-zero-padded month/day here
			if (dateStr.charAt(6) == '-') {
				dateStr = dateStr.substring(0, 5) + "0" + dateStr.substring(5);
			}
			char sep = dateStr.charAt(9);
			if (sep == 'T' || sep == ' ') {
				dateStr = dateStr.substring(0, 8) + "0" + dateStr.substring(8);
			}
		}

		mDateTimeStrs[position][accuracy] = dateStr;
	}

	/**
	 * Adds a date/time to the waypoint using a time since the epoch and a timezone offset.
	 *
	 * @param position        where the plane is
	 * @param accuracy        the accuracy of the reading
	 * @param millisFromEpoch milliseconds from the epoch (Jan 1 1970)
	 * @param tzOffsetMillis  timezone offset in milliseconds
	 */
	public void addDateTime(int position, int accuracy, long millisFromEpoch, int tzOffsetMillis) {
		mDateTimes[position][accuracy] = null;

		mMillisFromEpoch[position][accuracy] = millisFromEpoch;
		mTzOffsetMillis[position][accuracy] = tzOffsetMillis;
	}

	/**
	 * Gets a specific date/time for a plane position and accuracy
	 *
	 * @param position where the plane is
	 * @param accuracy the accuracy of the reading
	 * @return the date/time if it exists, null otherwise
	 */
	public DateTime getDateTime(int position, int accuracy) {
		if (mDateTimes[position][accuracy] == null) {
			// First check if we have a string to parse
			if (mDateTimeStrs[position][accuracy] != null) {
				LocalDateTime local = DateTimeUtils.parseFlightStatsDateTime(mDateTimeStrs[position][accuracy]);
				Airport airport = getAirport();
				DateTime cal = airport != null && airport.mTimeZone != null
					? local.toDateTime(airport.mTimeZone)
				 	: local.toDateTime();

				mDateTimes[position][accuracy] = cal;
			}

			// Second, check if we have a seconds from epoch + offset
			if (mMillisFromEpoch[position][accuracy] != 0) {
				DateTime cal = new DateTime(mMillisFromEpoch[position][accuracy]);
				cal = cal.withZone(DateTimeZone.forOffsetMillis(mTzOffsetMillis[position][accuracy]));

				mDateTimes[position][accuracy] = cal;
			}
		}

		return mDateTimes[position][accuracy];
	}

	/**
	 * Gets the most accurate date/time for a position.
	 *
	 * @param position
	 * @return
	 */
	public DateTime getMostAccurateDateTime(int position) {
		String[] dateTimes = mDateTimeStrs[position];
		long[] dateTimesAlt = mMillisFromEpoch[position];
		for (int accuracy = ACCURACY_ACTUAL; accuracy >= ACCURACY_UNKNOWN; accuracy--) {
			if (dateTimes[accuracy] != null || dateTimesAlt[accuracy] != 0) {
				return getDateTime(position, accuracy);
			}
		}
		return null;
	}

	public DateTime getMostRelevantDateTime() {
		return getAnnotatedMostRelevantDateTime().getDateTime();
	}

	private AnnotatedDateTime getAnnotatedMostRelevantDateTime() {
		boolean isArrival = (mAction == ACTION_ARRIVAL);

		// Note: because diverted flights often don't ever get updated with good gate times,
		// we put the priority for them on actual runway times rather than estimated gate times
		// as is normally the case for arrivals.

		// Actual gate
		if (getDateTime(POSITION_GATE, ACCURACY_ACTUAL) != null) {
			return new AnnotatedDateTime(this, POSITION_GATE, ACCURACY_ACTUAL);
		}

		// If this is an arrival, put predilection on gate times in case plane landed
		// but hasn't arrived at gate yet
		if (isArrival) {
			// Estimated gate
			if (getDateTime(POSITION_GATE, ACCURACY_ESTIMATED) != null) {
				return new AnnotatedDateTime(this, POSITION_GATE, ACCURACY_ESTIMATED);
			}
		}

		// Actual runway
		if (getDateTime(POSITION_RUNWAY, ACCURACY_ACTUAL) != null) {
			return new AnnotatedDateTime(this, POSITION_RUNWAY, ACCURACY_ACTUAL);
		}

		// Estimated gate
		if (!isArrival) {
			if (getDateTime(POSITION_GATE, ACCURACY_ESTIMATED) != null) {
				return new AnnotatedDateTime(this, POSITION_GATE, ACCURACY_ESTIMATED);
			}
		}

		// Scheduled gate
		if (getDateTime(POSITION_GATE, ACCURACY_SCHEDULED) != null) {
			return new AnnotatedDateTime(this, POSITION_GATE, ACCURACY_SCHEDULED);
		}

		// Estimated runway departure
		if (getDateTime(POSITION_RUNWAY, ACCURACY_ESTIMATED) != null) {
			return new AnnotatedDateTime(this, POSITION_RUNWAY, ACCURACY_ESTIMATED);
		}

		// Scheduled runway departure
		if (getDateTime(POSITION_RUNWAY, ACCURACY_SCHEDULED) != null) {
			return new AnnotatedDateTime(this, POSITION_RUNWAY, ACCURACY_SCHEDULED);
		}

		// Published time
		if (getDateTime(POSITION_UNKNOWN, ACCURACY_SCHEDULED) != null) {
			return new AnnotatedDateTime(this, POSITION_UNKNOWN, ACCURACY_SCHEDULED);
		}

		// Off the wall, last guess time
		if (getDateTime(POSITION_UNKNOWN, ACCURACY_UNKNOWN) != null) {
			return new AnnotatedDateTime(this, POSITION_UNKNOWN, ACCURACY_UNKNOWN);
		}

		// If we got here, we got nothing!
		return new AnnotatedDateTime(null, -1, -1);
	}

	public DateTime getBestSearchDateTime() {

		// FlightStats preferred date
		if (getDateTime(POSITION_UNKNOWN, ACCURACY_UNKNOWN) != null) {
			return getDateTime(POSITION_UNKNOWN, ACCURACY_UNKNOWN);
		}

		// Published
		if (getDateTime(POSITION_UNKNOWN, ACCURACY_SCHEDULED) != null) {
			return getDateTime(POSITION_UNKNOWN, ACCURACY_SCHEDULED);
		}

		// Scheduled Gate
		if (getDateTime(POSITION_GATE, ACCURACY_SCHEDULED) != null) {
			return getDateTime(POSITION_GATE, ACCURACY_SCHEDULED);
		}

		// Actual Gate
		if (getDateTime(POSITION_GATE, ACCURACY_ACTUAL) != null) {
			return getDateTime(POSITION_GATE, ACCURACY_ACTUAL);
		}

		// Estimated Gate
		if (getDateTime(POSITION_GATE, ACCURACY_ESTIMATED) != null) {
			return getDateTime(POSITION_GATE, ACCURACY_ESTIMATED);
		}

		// Scheduled Runway
		if (getDateTime(POSITION_RUNWAY, ACCURACY_SCHEDULED) != null) {
			return getDateTime(POSITION_RUNWAY, ACCURACY_SCHEDULED);
		}

		// Actual Runway
		if (getDateTime(POSITION_RUNWAY, ACCURACY_ACTUAL) != null) {
			return getDateTime(POSITION_RUNWAY, ACCURACY_ACTUAL);
		}

		// Estimated Runway
		if (getDateTime(POSITION_RUNWAY, ACCURACY_ESTIMATED) != null) {
			return getDateTime(POSITION_RUNWAY, ACCURACY_ESTIMATED);
		}

		return null;
	}

	/**
	 * Calculates the delay for this waypoint.  Positive numbers means that it's late; negative numbers early.
	 * 0 means it's on time.
	 *
	 * @return the delay for this waypoint
	 */
	public Delay getDelay() {
		boolean isArrival = (mAction == ACTION_ARRIVAL);

		// Note: because diverted flights often don't ever get updated with good gate times,
		// we put the priority for them on actual runway times rather than estimated gate times
		// as is normally the case for arrivals.

		// Actual gate vs. schedule gate
		DateTime cal1 = getDateTime(POSITION_GATE, ACCURACY_SCHEDULED);
		DateTime cal2 = getDateTime(POSITION_GATE, ACCURACY_ACTUAL);
		if (cal1 != null && cal2 != null) {
			int delay = DateTimeUtils.getMinutesBetween(cal1, cal2);
			return new Delay(delay, Delay.DELAY_GATE_ACTUAL);
		}

		// If we're at an arrival airport, try to calculate all gate times first
		// to avoid giving data on a flight that has landed but is not in gate yet.
		// Estimated gate vs. scheduled gate
		if (isArrival) {
			cal1 = getDateTime(POSITION_GATE, ACCURACY_SCHEDULED);
			cal2 = getDateTime(POSITION_GATE, ACCURACY_ESTIMATED);
			if (cal1 != null && cal2 != null) {
				int delay = DateTimeUtils.getMinutesBetween(cal1, cal2);
				return new Delay(delay, Delay.DELAY_GATE_ESTIMATED);
			}
		}

		// Actual runway vs. scheduled runway
		cal1 = getDateTime(POSITION_RUNWAY, ACCURACY_SCHEDULED);
		cal2 = getDateTime(POSITION_RUNWAY, ACCURACY_ACTUAL);
		if (cal1 != null && cal2 != null) {
			int delay = DateTimeUtils.getMinutesBetween(cal1, cal2);
			return new Delay(delay, Delay.DELAY_RUNWAY_ACTUAL);
		}

		// If this we skipped this earlier, do it now
		// Estimated gate vs. scheduled gate
		if (!isArrival) {
			cal1 = getDateTime(POSITION_GATE, ACCURACY_SCHEDULED);
			cal2 = getDateTime(POSITION_GATE, ACCURACY_ESTIMATED);
			if (cal1 != null && cal2 != null) {
				int delay = DateTimeUtils.getMinutesBetween(cal1, cal2);
				return new Delay(delay, Delay.DELAY_GATE_ESTIMATED);
			}
		}

		// Estimated runway vs. scheduled runway
		cal1 = getDateTime(POSITION_RUNWAY, ACCURACY_SCHEDULED);
		cal2 = getDateTime(POSITION_RUNWAY, ACCURACY_ESTIMATED);
		if (cal1 != null && cal2 != null) {
			int delay = DateTimeUtils.getMinutesBetween(cal1, cal2);
			return new Delay(delay, Delay.DELAY_RUNWAY_ESTIMATED);
		}

		// If we got here, we couldn't calculate delay; just return on time
		return new Delay(0, Delay.DELAY_NONE);
	}

	public boolean isInternationalTerminal() {
		return getAirport() != null
			&& getAirport().mHasInternationalTerminalI
			&& "I".equals(getTerminal());
	}

	@Override
	public String toString() {
		try {
			return convertToJson().toString(4);
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
			return convertToJson();
		}
		catch (JSONException e) {
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		try {
			loadFromJson(obj);
			return true;
		}
		catch (JSONException e) {
			return false;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Legacy JSON code
	//
	// Should someday be phased out completely

	/**
	 * @deprecated Use JSONable.toJson() instead
	 */
	@Deprecated
	public JSONObject convertToJson() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("version", VERSION);
		obj.putOpt("airportCode", mAirportCode);

		obj.putOpt("gate", mGate);
		obj.putOpt("terminal", mTerminal);
		obj.putOpt("city", mCity);

		obj.put("action", mAction);

		obj.putOpt("unknownTimes", toJson(mDateTimeStrs[POSITION_UNKNOWN]));
		obj.putOpt("gateTimes", toJson(mDateTimeStrs[POSITION_GATE]));
		obj.putOpt("runwayTimes", toJson(mDateTimeStrs[POSITION_RUNWAY]));

		// Add alternate date time
		JSONArray millisFromEpoch = new JSONArray();
		JSONArray tzOffsetMillis = new JSONArray();
		boolean add = false;
		for (int position = 0; position < NUM_POSITIONS; position++) {
			for (int accuracy = 0; accuracy < NUM_ACCURACIES; accuracy++) {
				if (mMillisFromEpoch[position][accuracy] != 0) {
					add = true;
				}
				millisFromEpoch.put(mMillisFromEpoch[position][accuracy]);
				tzOffsetMillis.put(mTzOffsetMillis[position][accuracy]);
			}
		}
		if (add) {
			// We don't need to store this data if this method of storing data wasn't used
			obj.putOpt("millisFromEpoch", millisFromEpoch);
			obj.putOpt("tzOffsetMillis", tzOffsetMillis);
		}

		return obj;
	}

	private JSONObject toJson(String[] dateTimes) throws JSONException {
		JSONObject times = new JSONObject();
		for (int accuracy = 0; accuracy < dateTimes.length; accuracy++) {
			if (dateTimes[accuracy] != null) {
				times.put(accuracy + "", dateTimes[accuracy]);
			}
		}
		if (times.length() > 0) {
			return times;
		}
		return null;
	}

	/**
	 * @deprecated Use JSONable.fromJson() instead
	 */
	@Deprecated
	private void loadFromJson(JSONObject obj) throws JSONException {
		int version = obj.getInt("version");

		if (obj.has("airport")) {
			Airport airport = new Airport();
			airport.fromJson(obj.getJSONObject("airport"));
			mAirportCode = airport.mAirportCode;
		}
		else if (obj.has("airportCode")) {
			mAirportCode = obj.getString("airportCode");
		}

		setGate(obj.optString("gate", null));

		setTerminal(obj.optString("terminal", null));

		if (obj.has("action")) {
			mAction = obj.getInt("action");
		}

		if (obj.has("city")) {
			mCity = obj.getString("city");
		}

		if (version < 4) {
			if (obj.has("unknownTimes")) {
				loadFromJsonBackCompat(POSITION_UNKNOWN, obj.getJSONObject("unknownTimes"));
			}

			if (obj.has("gateTimes")) {
				loadFromJsonBackCompat(POSITION_GATE, obj.getJSONObject("gateTimes"));
			}

			if (obj.has("runwayTimes")) {
				loadFromJsonBackCompat(POSITION_RUNWAY, obj.getJSONObject("runwayTimes"));
			}
		}
		else {
			if (obj.has("unknownTimes")) {
				loadFromJson(POSITION_UNKNOWN, obj.getJSONObject("unknownTimes"));
			}

			if (obj.has("gateTimes")) {
				loadFromJson(POSITION_GATE, obj.getJSONObject("gateTimes"));
			}

			if (obj.has("runwayTimes")) {
				loadFromJson(POSITION_RUNWAY, obj.getJSONObject("runwayTimes"));
			}
		}

		if (obj.has("millisFromEpoch") && obj.has("tzOffsetMillis")) {
			JSONArray millisFromEpoch = obj.optJSONArray("millisFromEpoch");
			JSONArray tzOffsetMillis = obj.optJSONArray("tzOffsetMillis");

			int len = millisFromEpoch.length();
			for (int a = 0; a < len; a++) {
				int position = a / NUM_ACCURACIES;
				int accuracy = a % NUM_ACCURACIES;

				mMillisFromEpoch[position][accuracy] = millisFromEpoch.getLong(a);
				mTzOffsetMillis[position][accuracy] = tzOffsetMillis.getInt(a);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadFromJson(int position, JSONObject times) throws JSONException {
		Iterator<String> it = times.keys();
		while (it.hasNext()) {
			String accStr = it.next();
			int accuracy = Integer.parseInt(accStr);
			addDateTime(position, accuracy, times.getString(accStr));
		}
	}

	@SuppressWarnings("unchecked")
	private void loadFromJsonBackCompat(int position, JSONObject times) throws JSONException {
		Iterator<String> it = times.keys();
		Airport airport = getAirport();
		DateTimeZone tz = (airport != null) ? airport.mTimeZone : DateTimeZone.getDefault();
		while (it.hasNext()) {
			String accStr = it.next();
			int accuracy = Integer.parseInt(accStr);
			DateTime cal = new DateTime(times.getLong(accStr), tz);
			addDateTime(position, accuracy, cal.toString(DateTimeUtils.FLIGHT_STATS_FORMAT));
		}
	}

	public static class AnnotatedDateTime {
		private final DateTime dateTime;

		AnnotatedDateTime(Waypoint wp, int position, int accuracy) {
			if (position > -1 && accuracy > -1) {
				dateTime = wp.getDateTime(position, accuracy);
			}
			else {
				dateTime = null;
			}
		}

		public DateTime getDateTime() {
			return dateTime;
		}

	}
}
