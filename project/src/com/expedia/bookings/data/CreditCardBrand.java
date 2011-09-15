package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class CreditCardBrand implements JSONable {

	private String mName;
	private String mCode;

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getCode() {
		return mCode;
	}

	public void setCode(String code) {
		this.mCode = code;
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("name", mName);
			obj.putOpt("code", mCode);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert CreditCardBrand object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		mName = obj.optString("name", null);
		mCode = obj.optString("code", null);
		return true;
	}

	@Override
	public String toString() {
		JSONObject obj = toJson();
		try {
			return obj.toString(2);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}
}
