package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FlightCheckoutResponse extends Response implements JSONable {

	private String mOrderId;

	private Money mTotalCharges;

	// A new offer can be returned in some error cases, like price changes
	private FlightTrip mNewOffer;

	public void setOrderId(String orderId) {
		mOrderId = orderId;
	}

	public String getOrderId() {
		return mOrderId;
	}

	public void setTotalCharges(Money totalCharges) {
		mTotalCharges = totalCharges;
	}

	public Money getTotalCharges() {
		return mTotalCharges;
	}

	public void setNewOffer(FlightTrip newOffer) {
		mNewOffer = newOffer;
	}

	public FlightTrip getNewOffer() {
		return mNewOffer;
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
			JSONUtils.putJSONable(obj, "newOffer", mNewOffer);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mOrderId = obj.optString("orderId", null);
		mTotalCharges = JSONUtils.getJSONable(obj, "totalCharges", Money.class);
		mNewOffer = JSONUtils.getJSONable(obj, "newOffer", FlightTrip.class);

		return true;
	}
}
