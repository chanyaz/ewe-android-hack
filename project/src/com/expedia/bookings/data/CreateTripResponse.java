package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class CreateTripResponse extends Response implements JSONable {

	private String mTripId;
	private String mUserId;
	private Rate mNewRate;

	@Override
	public boolean isSuccess() {
		return !hasErrors();
	}

	public void setTripId(String tripId) {
		mTripId = tripId;
	}

	public String getTripId() {
		return mTripId;
	}

	public void setUserId(String userId) {
		mUserId = userId;
	}

	public String getUserId() {
		return mUserId;
	}

	public void setNewRate(Rate rate) {
		mNewRate = rate;
	}

	public Rate getNewRate() {
		return mNewRate;
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
			//obj.putOpt("success", mSuccess);
			//JSONUtils.putJSONable(obj, "user", mUser);
			obj.put("tripId", mTripId);
			obj.put("userId", mUserId);
			JSONUtils.putJSONable(obj, "newRate", mNewRate);
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

		mTripId = obj.optString("tripId", null);
		mUserId = obj.optString("userId", null);
		mNewRate = (Rate) JSONUtils.getJSONable(obj, "newRate", Rate.class);
		return true;
	}
}
