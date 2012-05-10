package com.expedia.bookings.data;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class FlightSearchParams implements JSONable {

	private Calendar mDepartureDate;
	private Calendar mReturnDate;
	private String mDepartureAirportCode;
	private String mArrivalAirportCode;

	public FlightSearchParams() {
		reset();
	}

	public void reset() {
		mDepartureDate = Calendar.getInstance();
		mReturnDate = Calendar.getInstance();
		mReturnDate.add(Calendar.DAY_OF_YEAR, 5);
	}

	public Calendar getDepartureDate() {
		return mDepartureDate;
	}

	public void setDepartureDate(Calendar departureDate) {
		mDepartureDate = departureDate;
	}

	public Calendar getReturnDate() {
		return mReturnDate;
	}

	public void setReturnDate(Calendar returnDate) {
		mReturnDate = returnDate;
	}

	public String getDepartureAirportCode() {
		return mDepartureAirportCode;
	}

	public void setDepartureAirportCode(String departureAirportCode) {
		mDepartureAirportCode = departureAirportCode;
	}

	public String getArrivalAirportCode() {
		return mArrivalAirportCode;
	}

	public void setArrivalAirportCode(String arrivalAirportCode) {
		mArrivalAirportCode = arrivalAirportCode;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("departureDate", mDepartureDate.getTimeInMillis());
			obj.putOpt("returnDate", mReturnDate.getTimeInMillis());
			obj.putOpt("departureAirportCode", mDepartureAirportCode);
			obj.putOpt("arrivalAirportCode", mArrivalAirportCode);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mDepartureDate = Calendar.getInstance();
		mDepartureDate.setTimeInMillis(obj.optLong("departureDate"));

		mReturnDate = Calendar.getInstance();
		mReturnDate.setTimeInMillis(obj.optLong("returnDate"));

		mDepartureAirportCode = obj.optString("departureAirportCode");
		mArrivalAirportCode = obj.optString("arrivalAirportCode");

		return true;
	}

}
