package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class StoredCreditCard implements JSONable {

	private String mType;
	private String mDescription;
	private String mRemoteId;

	public StoredCreditCard() {
		// Default constructor
	}

	public StoredCreditCard(JSONObject obj) {
		this.fromJson(obj);
	}

	public String getType() {
		return mType;
	}

	public String getId() {
		return mRemoteId;
	}

	public String getDescription() {
		return mDescription;
	}

	public CreditCardType getCardType() {
		if (TextUtils.isEmpty(mType)) {
			return null;
		}

		// Code lovingly stolen from iOS, where they note that these
		// values are not yet verified from the API folks.
		if (mType.equals("AmericanExpress")) {
			return CreditCardType.AMERICAN_EXPRESS;
		}
		else if (mType.equals("CarteBlanche")) {
			return CreditCardType.CARTE_BLANCHE;
		}
		else if (mType.equals("ChinaUnionPay")) {
			return CreditCardType.CHINA_UNION_PAY;
		}
		else if (mType.equals("DinersClub")) {
			return CreditCardType.DINERS_CLUB;
		}
		else if (mType.equals("Discover")) {
			return CreditCardType.DISCOVER;
		}
		else if (mType.equals("JCB")) {
			return CreditCardType.JAPAN_CREDIT_BUREAU;
		}
		else if (mType.equals("Maestro")) {
			return CreditCardType.MAESTRO;
		}
		else if (mType.equals("MasterCard")) {
			return CreditCardType.MASTERCARD;
		}
		else if (mType.equals("Visa")) {
			return CreditCardType.VISA;
		}

		return null;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.putOpt("creditCardType", mType);
			obj.putOpt("description", mDescription);
			obj.putOpt("paymentsInstrumentsId", mRemoteId);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert StoredCreditCard to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mType = obj.optString("creditCardType", null);
		mDescription = obj.optString("description", null);
		mRemoteId = obj.optString("paymentsInstrumentsId", null);
		return true;
	}
}
