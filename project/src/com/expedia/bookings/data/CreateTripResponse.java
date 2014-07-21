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
	private String mTealeafId;

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

	public void setTealeafId(String id) {
		mTealeafId = id;
	}

	public String getTealeafId() {
		return mTealeafId;
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
			obj.put("tripId", mTripId);
			obj.put("userId", mUserId);
			obj.put("tealeafId", mTealeafId);
			JSONUtils.putJSONable(obj, "newRate", mNewRate);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert HotelSearchResponse to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mTripId = obj.optString("tripId", null);
		mUserId = obj.optString("userId", null);
		mTealeafId = obj.optString("tealeafId", null);
		mNewRate = JSONUtils.getJSONable(obj, "newRate", Rate.class);
		return true;
	}
}
