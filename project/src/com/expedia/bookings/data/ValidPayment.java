package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class ValidPayment implements JSONable {

	private CreditCardType mCreditCardType;
	private Money mFee;

	public void setCreditCardType(CreditCardType type) {
		mCreditCardType = type;
	}

	public CreditCardType getCreditCardType() {
		return mCreditCardType;
	}

	public Money getFee() {
		return mFee;
	}

	public void setFee(Money fee) {
		mFee = fee;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject json = new JSONObject();
			JSONUtils.putEnum(json, "name", mCreditCardType);
			JSONUtils.putJSONable(json, "fee", mFee);
			return json;
		}
		catch (JSONException e) {
			Log.e("Could not convert ValidPayment object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mCreditCardType = JSONUtils.getEnum(obj, "name", CreditCardType.class);
		mFee = JSONUtils.getJSONable(obj, "fee", Money.class);
		return true;
	}
}
