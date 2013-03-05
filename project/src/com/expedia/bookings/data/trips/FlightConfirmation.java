package com.expedia.bookings.data.trips;

import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class FlightConfirmation implements JSONable {
	private String mCarrier;
	private String mConfirmationCode;

	public FlightConfirmation() {

	}

	public String getCarrier() {
		return mCarrier;
	}

	public String getConfirmationCode() {
		return mConfirmationCode;
	}

	public void setCarrier(String carrier) {
		mCarrier = carrier;
	}

	public void setConfirmationCode(String confirmationCode) {
		mConfirmationCode = confirmationCode;
	}

	@Override
	public String toString() {
		return mConfirmationCode;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		try {
			obj.putOpt("airlineName", mCarrier);
			obj.putOpt("number", mConfirmationCode);
		}
		catch (Exception ex) {
			Log.e("Exception in toJson()", ex);
		}
		return obj;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mCarrier = obj.optString("airlineName");
		mConfirmationCode = obj.optString("number");
		return true;
	}

}
