package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.utils.CurrencyUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class ValidPayment implements JSONable {

	private String mCreditCardType;
	private Money mFee;
	private String mCurrencyCode;

	public void setCreditCardType(String type) {
		mCreditCardType = type;
	}

	public CreditCardType getCreditCardType() {
		return CurrencyUtils.getType(mCreditCardType);
	}

	public Money getFee() {
		return mFee;
	}

	public void setFee(Money fee) {
		mFee = fee;
	}

	public String getCurrencyCode() {
		return mCurrencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		mCurrencyCode = currencyCode;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	//		"name": "AmericanExpress",
	//		"fee": "8.50",
	//		"feeCurrencyCode": "AUD"

	@Override
	public JSONObject toJson() {
		try {
			JSONObject json = new JSONObject();
			json.putOpt("name", mCreditCardType);
			JSONUtils.putJSONable(json, "fee", mFee);
			json.putOpt("feeCurrencyCode", mCurrencyCode);
			return json;
		}
		catch (JSONException e) {
			Log.e("Could not convert ValidPayment object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mCreditCardType = obj.optString("name", null);
		mFee = JSONUtils.getJSONable(obj, "fee", Money.class);
		mCurrencyCode = obj.optString("feeCurrencyCode", null);
		return true;
	}
}
