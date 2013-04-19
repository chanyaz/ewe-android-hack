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

	private boolean mIsGoogleWallet;

	public StoredCreditCard() {
		// Default constructor
	}

	public StoredCreditCard(JSONObject obj) {
		this.fromJson(obj);
	}

	public void setType(String type) {
		mType = type;
	}

	public String getType() {
		return mType;
	}

	public void setId(String id) {
		mRemoteId = id;
	}

	public String getId() {
		return mRemoteId;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setIsGoogleWallet(boolean isGoogleWallet) {
		mIsGoogleWallet = isGoogleWallet;
	}

	public boolean isGoogleWallet() {
		return mIsGoogleWallet;
	}

	public CreditCardType getCardType() {
		if (isGoogleWallet()) {
			return CreditCardType.GOOGLE_WALLET;
		}

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
			obj.putOpt("isGoogleWallet", mIsGoogleWallet);
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
		mIsGoogleWallet = obj.optBoolean("isGoogleWallet");
		return true;
	}
}
