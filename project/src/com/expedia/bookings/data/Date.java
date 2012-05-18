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
	}

	public int getMonth() {
		return mMonth;
	}

	public void setMonth(int month) {
		this.mMonth = month;
	}

	public int getDayOfMonth() {
		return mDayOfMonth;
	}

	public void setDayOfMonth(int dayOfMonth) {
		this.mDayOfMonth = dayOfMonth;
	}

	public Calendar getCalendar() {
		return new GregorianCalendar(mYear, mMonth - 1, mDayOfMonth);
	}

	public void fromCalendar(Calendar cal) {
		mYear = cal.get(Calendar.YEAR);
		mMonth = cal.get(Calendar.MONTH) + 1;
		mDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
	}

	public Date clone() {
		return new Date(mYear, mMonth, mDayOfMonth);
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
