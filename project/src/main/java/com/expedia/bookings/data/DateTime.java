package com.expedia.bookings.data;

import java.util.Calendar;
import java.util.SimpleTimeZone;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.format.DateUtils;

import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.json.JSONable;

/**
 * A simple way to store a milliseconds-from-epoch + static timezone offset.
 *
 * Not mutable; it wouldn't make sense to allow someone to change one
 * field without the other.
 */
public class DateTime implements JSONable, Comparable<DateTime> {

	private long mMillisFromEpoch;
	private int mTzOffsetMillis;

	// Cached for speed
	private Calendar mCal;

	@Deprecated
	public static DateTime newInstance(Calendar cal) {
		return new DateTime(cal.getTimeInMillis(), cal.getTimeZone().getOffset(cal.getTimeInMillis()));
	}

	public static DateTime fromLocalDate(LocalDate date) {
		return new DateTime(date.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis(), 0);
	}

	public static DateTime fromJodaDateTime(org.joda.time.DateTime dateTime) {
		if (dateTime != null) {
			return new DateTime(dateTime.getMillis(), dateTime.getZone().getStandardOffset(0));
		}
		return null;
	}

	@Deprecated
	public DateTime() {
		// Empty constructor for JSONable
	}

	@Deprecated
	public DateTime(long millisFromEpoch, int tzOffsetMillis) {
		mMillisFromEpoch = millisFromEpoch;
		mTzOffsetMillis = tzOffsetMillis;
	}

	public long getMillisFromEpoch() {
		return mMillisFromEpoch;
	}

	public int getTzOffsetMillis() {
		return mTzOffsetMillis;
	}

	public Calendar getCalendar() {
		if (mCal == null) {
			mCal = Calendar.getInstance();
			mCal.setTimeInMillis(mMillisFromEpoch);
			mCal.setTimeZone(new SimpleTimeZone(mTzOffsetMillis, "GMT"));
		}

		return mCal;
	}

	/**
	 * Quick way of formatting this DateTime for consumption, using the
	 * device's locale settings.  Flags should be the same as what is used
	 * in Android's ApiDateUtils.
	 */
	public String formatTime(Context context, int flags) {
		return DateUtils.formatDateTime(context, mMillisFromEpoch + mTzOffsetMillis, flags | DateUtils.FORMAT_UTC);
	}

	/**
	 * This method is used to fetch the formatted timeZone string based on only timeZoneOffset.
	 * Example: If timeZoneOffset = -14400, it returns GMT-4
	 */
	public String formatTimeZone() {
		int offsetSeconds = mTzOffsetMillis / 1000;
		int offsetMinutes = Math.abs(offsetSeconds / 60);
		int offsetHours = offsetMinutes / 60;
		offsetMinutes -= (offsetHours * 60);
		String timeZoneString = "GMT";
		if (offsetHours > 0 || offsetMinutes > 0) {
			timeZoneString += ((offsetSeconds > 0) ? "+" : "-") + offsetHours;
			if (offsetMinutes > 0) {
				timeZoneString += ":" + ((offsetMinutes < 10) ? "0" : "") + offsetMinutes;
			}
		}

		return timeZoneString;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("millisFromEpoch", mMillisFromEpoch);
			obj.putOpt("tzOffsetMillis", mTzOffsetMillis);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mMillisFromEpoch = obj.optLong("millisFromEpoch");
		mTzOffsetMillis = obj.optInt("tzOffsetMillis");
		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Comparable

	@Override
	public int compareTo(DateTime another) {
		if (mMillisFromEpoch < another.mMillisFromEpoch) {
			return -1;
		}
		else if (mMillisFromEpoch > another.mMillisFromEpoch) {
			return 1;
		}

		if (mTzOffsetMillis < another.mTzOffsetMillis) {
			return -1;
		}
		else if (mTzOffsetMillis > another.mTzOffsetMillis) {
			return 1;
		}

		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DateTime)) {
			return false;
		}

		DateTime other = (DateTime) o;

		return mMillisFromEpoch == other.mMillisFromEpoch
				&& mTzOffsetMillis == other.mTzOffsetMillis;
	}

	//////////////////////////////////////////////////////////////////////////
	// DateTime compatibility

	public static org.joda.time.DateTime toJodaDateTime(DateTime dateTime) {
		if (dateTime != null) {
			return JodaUtils.fromMillisAndOffset(dateTime.mMillisFromEpoch, dateTime.mTzOffsetMillis);
		}

		return null;
	}
}
