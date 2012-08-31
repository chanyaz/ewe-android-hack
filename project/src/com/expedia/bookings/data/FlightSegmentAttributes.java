package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class FlightSegmentAttributes implements JSONable {

	private String mBookingCode;
	private String mCabinCode;

	public String getBookingCode() {
		return mBookingCode;
	}

	public String getCabinCode() {
		return mCabinCode;
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("bookingCode", mBookingCode);
			obj.putOpt("cabinCode", mCabinCode);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mBookingCode = obj.optString("bookingCode");
		mCabinCode = obj.optString("cabinCode");
		return true;
	}

}
