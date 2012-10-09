package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FlightCheckoutResponse extends Response implements JSONable {

	private String mOrderId;

	private Money mTotalCharges;

	public void setOrderId(String orderId) {
		mOrderId = orderId;
	}

	public String getOrderId() {
		return mOrderId;
	}

	public Money getTotalCharges() {
		return mTotalCharges;
	}

	public void setTotalCharges(Money totalCharges) {
		mTotalCharges = totalCharges;
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
			obj.put("orderId", mOrderId);
			JSONUtils.putJSONable(obj, "totalCharges", mTotalCharges);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mOrderId = obj.optString("mTotalCharges", null);
		mTotalCharges = JSONUtils.getJSONable(obj, "totalCharges", Money.class);
		return true;
	}
}
