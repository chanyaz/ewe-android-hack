package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.utils.GsonUtil;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FlightCheckoutResponse extends Response implements JSONable {

	private String mOrderId;

	private Money mTotalCharges;

	// A new offer can be returned in some error cases, like price changes
	private FlightTrip mNewOffer;

	private String mDestinationRegionId;

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

	public void setDestinationRegionId(String destinationRegionId) {
		mDestinationRegionId = destinationRegionId;
	}

	public String getDestinationRegionId() {
		return mDestinationRegionId;
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
			GsonUtil.putForJsonable(obj, "totalCharges", mTotalCharges);
			JSONUtils.putJSONable(obj, "newOffer", mNewOffer);
			obj.put("destinationRegionId", mDestinationRegionId);
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
		mTotalCharges = GsonUtil.getForJsonable(obj, "totalCharges", Money.class);
		mNewOffer = JSONUtils.getJSONable(obj, "newOffer", FlightTrip.class);
		mDestinationRegionId = obj.optString("destinationRegionId");
		return true;
	}
}
