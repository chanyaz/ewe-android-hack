package com.expedia.bookings.data;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

// Important note: month is 1-indexed here (to match every other field in existence).
// This means that conversion to/from Calendar requires a few +/- 1s.
public class Date implements JSONable {
	private int mYear;
	private int mMonth;
	private int mDayOfMonth;

	// Cached data
	private Calendar mCal;

	public Date() {
		// Default constructor
	}

	public Date(int year, int month, int dayOfMonth) {
		mYear = year;
		mMonth = month;
		mDayOfMonth = dayOfMonth;
	}

	public Date(Calendar cal) {
		fromCalendar(cal);
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
	
	public void setDate(int year, int month, int dayOfMonth){
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

	public Date clone() {
		return new Date(mYear, mMonth, mDayOfMonth);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Date)) {
			return false;
		}

		Date other = (Date) o;

		return this.mYear == other.mYear && this.mMonth == other.mMonth && this.mDayOfMonth == other.mDayOfMonth;
	}

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

	public boolean fromJson(JSONObject obj) {
		mDayOfMonth = obj.optInt("dayOfMonth");
		mMonth = obj.optInt("month");
		mYear = obj.optInt("year");
		mCal = null;
		return true;
	}

	@Override
	public String toString() {
		JSONObject obj = toJson();
		try {
			return obj.toString(2);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}
}
