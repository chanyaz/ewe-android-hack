package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class BookingResponse extends Response implements JSONable {
	private boolean mSuccess;
	private String mConfNumber;
	private String mHotelConfNumber;

	// For Expedia
	private String mItineraryId;

	public boolean isSuccess() {
		return mSuccess;
	}

	public void setSuccess(boolean success) {
		this.mSuccess = success;
	}

	public String getConfNumber() {
		return mConfNumber;
	}

	public void setConfNumber(String confNumber) {
		this.mConfNumber = confNumber;
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

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("success", mSuccess);
			obj.putOpt("confNumber", mConfNumber);
			obj.putOpt("hotelConfNumber", mHotelConfNumber);
			obj.putOpt("itineraryId", mItineraryId);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert BookingResponse object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mSuccess = obj.optBoolean("success", false);
		mConfNumber = obj.optString("confNumber", null);
		mHotelConfNumber = obj.optString("hotelConfNumber", null);
		mItineraryId = obj.optString("itineraryId", null);
		return true;
	}

	// **WARNING: USE FOR TESTING PURPOSES ONLY**
	public void fillWithTestData() throws JSONException {
		String data = "{\"itineraryId\":\"69144795\",\"success\":false,\"confNumber\":\"1234\"}";
		JSONObject obj = new JSONObject(data);
		fromJson(obj);
	}
}
