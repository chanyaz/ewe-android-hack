package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class BookingResponse extends Response implements JSONable {

	private String mHotelConfNumber;

	// For Expedia
	private String mItineraryId;

	private String mPhoneNumber;

	public boolean succeededWithErrors() {
		if (!hasErrors()) {
			return false;
		}

		for (ServerError error : getErrors()) {
			if (error.succeededWithErrors()) {
				return true;
			}
		}

		return false;
	}

	public String getHotelConfNumber() {
		return mHotelConfNumber;
	}

	public void setHotelConfNumber(String hotelConfNumber) {
		this.mHotelConfNumber = hotelConfNumber;
	}

	public String getItineraryId() {
		return mItineraryId;
	}

	public void setItineraryId(String itineraryId) {
		this.mItineraryId = itineraryId;
	}

	public void setPhoneNumber(String number) {
		mPhoneNumber = number;
	}

	public String getPhoneNumber() {
		return mPhoneNumber;
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = super.toJson();
			obj.putOpt("hotelConfNumber", mHotelConfNumber);
			obj.putOpt("itineraryId", mItineraryId);
			obj.putOpt("phoneNumber", mPhoneNumber);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert BookingResponse object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mHotelConfNumber = obj.optString("hotelConfNumber", null);
		mItineraryId = obj.optString("itineraryId", null);
		mPhoneNumber = obj.optString("phoneNumber", null);
		return true;
	}
}
