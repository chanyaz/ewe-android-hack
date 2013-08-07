package com.expedia.bookings.data;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

// Important note: month is 1-indexed here (to match every other field in existence).
// This means that conversion to/from Calendar requires a few +/- 1s.
/**
 * Use the LocalDate class if possible.
 */
public class Date implements JSONable, Comparable<Object> {
	private int mYear;
	private int mMonth;
	private int mDayOfMonth;

	// Cached data
	private Calendar mCal;

	@Deprecated
	public Date() {
		// Default constructor
	}

	@Deprecated
	public Date(int year, int month, int dayOfMonth) {
		mYear = year;
		mMonth = month;
		mDayOfMonth = dayOfMonth;
	}

	@Deprecated
	public Date(Calendar cal) {
		fromCalendar(cal);
	}

	@Deprecated
	public Date(Time time) {
		fromTime(time);
	}

	@Deprecated
	public Date(LocalDate date) {
		mYear = date.getYear();
		mMonth = date.getMonthOfYear();
		mDayOfMonth = date.getDayOfMonth();
	}

	public int getYear() {
		return mYear;
	}

	public void setYear(int year) {
		this.mYear = year;
		mCal = null;
	}

	public int getMonth() {
		return mMonth;
	}

	public void setMonth(int month) {
		this.mMonth = month;
		mCal = null;
	}

	public int getDayOfMonth() {
		return mDayOfMonth;
	}

	public void setDayOfMonth(int dayOfMonth) {
		this.mDayOfMonth = dayOfMonth;
		mCal = null;
	}

	public void setDate(int year, int month, int dayOfMonth) {
		setYear(year);
		setMonth(month);
		setDayOfMonth(dayOfMonth);
	}

	public Calendar getCalendar() {
		if (mCal == null) {
			mCal = new GregorianCalendar(mYear, mMonth - 1, mDayOfMonth);
		}
		return mCal;
	}

	public void fromCalendar(Calendar cal) {
		mYear = cal.get(Calendar.YEAR);
		mMonth = cal.get(Calendar.MONTH) + 1;
		mDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
	}

	public void fromTime(Time time) {
		mYear = time.year;
		mMonth = time.month + 1;
		mDayOfMonth = time.monthDay;
	}

	@Deprecated
	public Date clone() {
		return new Date(mYear, mMonth, mDayOfMonth);
	}

	@Override
	public int compareTo(Object another) {
		int year, month, dayOfMonth;
		if (another instanceof Date) {
			Date other = (Date) another;
			year = other.mYear;
			month = other.mMonth;
			dayOfMonth = other.mDayOfMonth;
		}
		else if (another instanceof Calendar) {
			Calendar cal = (Calendar) another;
			year = cal.get(Calendar.YEAR);
			month = cal.get(Calendar.MONTH) + 1;
			dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		}
		else {
			throw new IllegalArgumentException("Expected Date or Calendar, got " + another);
		}

		// Perf: We could probably do something fancier with bit shifting if we need the performance below
		if (mYear != year) {
			return mYear - year;
		}
		else if (mMonth != month) {
			return mMonth - month;
		}
		else if (mDayOfMonth != dayOfMonth) {
			return mDayOfMonth - dayOfMonth;
		}

		return 0;
	}

	public boolean after(Object o) {
		return compareTo(o) > 0;
	}

	public boolean before(Object o) {
		return compareTo(o) < 0;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Date) && !(o instanceof Calendar)) {
			return false;
		}

		return compareTo(o) == 0;
	}

	@Deprecated
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("dayOfMonth", mDayOfMonth);
			obj.putOpt("month", mMonth);
			obj.putOpt("year", mYear);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Date object to JSON.", e);
			return null;
		}
	}

	@Deprecated
	public boolean fromJson(JSONObject obj) {
		mDayOfMonth = obj.optInt("dayOfMonth");
		mMonth = obj.optInt("month");
		mYear = obj.optInt("year");
		mCal = null;
		return true;
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// LocalDate compatibility

	public static LocalDate toLocalDate(Date date) {
		if (date != null) {
			return new LocalDate(date.mYear, date.mMonth, date.mDayOfMonth);
		}
		return null;
	}
}
