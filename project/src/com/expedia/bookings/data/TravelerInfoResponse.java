package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class TravelerInfoResponse extends Response implements JSONable {

	private boolean mSuccess;

	private Traveler mPassenger;

	public void setSuccess(boolean success) {
		mSuccess = success;
	}

	@Override
	public boolean isSuccess() {
		return !hasErrors() && mSuccess;
	}

	public void setTraveler(Traveler passenger) {
		mPassenger = passenger;
	}

	public Traveler getTraveler() {
		return mPassenger;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable interface

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			obj.putOpt("success", mSuccess);
			JSONUtils.putJSONable(obj, "traveler", mPassenger);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert SearchResponse to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mSuccess = obj.optBoolean("success", false);
		mPassenger = (Traveler) JSONUtils.getJSONable(obj, "traveler", Traveler.class);
		return true;
	}
}
