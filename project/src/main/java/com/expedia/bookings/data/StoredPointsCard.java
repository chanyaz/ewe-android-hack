package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class StoredPointsCard implements JSONable {
	private PaymentType paymentType;
	private String description;
	private String paymentsInstrumentId;

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPaymentsInstrumentId() {
		return paymentsInstrumentId;
	}

	public void setPaymentsInstrumentId(String paymentsInstrumentId) {
		this.paymentsInstrumentId = paymentsInstrumentId;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			JSONUtils.putEnum(obj, "creditCardType", paymentType);
			obj.putOpt("description", description);
			obj.putOpt("paymentsInstrumentsId", paymentsInstrumentId);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert StoredPointsCard to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		paymentType = JSONUtils.getEnum(obj, "creditCardType", PaymentType.class);
		description = obj.optString("description", null);
		paymentsInstrumentId = obj.optString("paymentsInstrumentsId", null);
		return true;
	}
}
