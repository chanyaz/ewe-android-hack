package com.expedia.bookings.data;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class CreateTripResponse extends Response implements JSONable {

	private String mTripId;
	private String mUserId;
	private Rate mNewRate;
	private Rate mAirAttachRate;
	private String mTealeafId;
	private List<ValidPayment> mValidPayments;

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

	public void setAirAttachRate(Rate rate) {
		mAirAttachRate = rate;
	}

	public Rate getAirAttachRate() {
		return mAirAttachRate;
	}

	public void setTealeafId(String id) {
		mTealeafId = id;
	}

	public String getTealeafId() {
		return mTealeafId;
	}

	public void setValidPayments(List<ValidPayment> validPayments) {
		mValidPayments = validPayments;
	}

	public List<ValidPayment> getValidPayments() {
		return mValidPayments;
	}

	public CreateTripResponse clone() {
		CreateTripResponse response = new CreateTripResponse();
		JSONObject json = toJson();
		response.fromJson(json);
		return response;
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
			JSONUtils.putJSONableList(obj, "validPayments", mValidPayments);
			JSONUtils.putJSONable(obj, "airAttachRate", mAirAttachRate);
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
		mValidPayments = JSONUtils.getJSONableList(obj, "validPayments", ValidPayment.class);
		mAirAttachRate = JSONUtils.getJSONable(obj, "airAttachRate", Rate.class);
		return true;
	}
}
