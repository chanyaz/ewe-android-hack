package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class StoredCreditCard implements JSONable {

	private CreditCardType mType;
	private String mDescription;
	private String mRemoteId;
	private String mCardNumber;

	private boolean mIsGoogleWallet;

	public void setType(CreditCardType type) {
		mType = type;
	}

	public CreditCardType getType() {
		if (isGoogleWallet()) {
			return CreditCardType.GOOGLE_WALLET;
		}

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

	public void setCardNumber(String number) {
		mCardNumber = number;
	}

	/**
	 * TODO: this will always return null until the API gives us this information.
	 * @return
	 */
	public String getCardNumber() {
		return mCardNumber;
	}

	public void setIsGoogleWallet(boolean isGoogleWallet) {
		mIsGoogleWallet = isGoogleWallet;
	}

	public boolean isGoogleWallet() {
		return mIsGoogleWallet;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			JSONUtils.putEnum(obj, "creditCardType", mType);
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
		mType = JSONUtils.getEnum(obj, "creditCardType", CreditCardType.class);
		mDescription = obj.optString("description", null);
		mRemoteId = obj.optString("paymentsInstrumentsId", null);
		mIsGoogleWallet = obj.optBoolean("isGoogleWallet");
		return true;
	}
}
