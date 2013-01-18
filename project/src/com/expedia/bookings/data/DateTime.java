package com.expedia.bookings.data;

import java.util.Calendar;
import java.util.SimpleTimeZone;

import org.json.JSONException;
import org.json.JSONObject;

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

	public DateTime(long millisFromEpoch, int tzOffsetMillis) {
		mMillisFromEpoch = millisFromEpoch;
		mTzOffsetMillis = tzOffsetMillis;
	}

	public Calendar getCalendar() {
		if (mCal == null) {
			mCal = Calendar.getInstance();
			mCal.setTimeInMillis(mMillisFromEpoch);
			mCal.setTimeZone(new SimpleTimeZone(mTzOffsetMillis, "GMT"));
		}

		return mCal;
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
}
