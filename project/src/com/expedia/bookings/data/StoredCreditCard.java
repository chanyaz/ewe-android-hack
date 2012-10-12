package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

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
