package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class Phone implements JSONable {
	private String mNumber;
	private String mAreaCode;
	private UserPreference.Category mCategory;
	private String mCountryCode;
	private String mExtensionNumber;

	public Phone(JSONObject obj) {
		this.fromJson(obj);
	}

	public String getNumber() {
		return mNumber;
	}

	public String getAreaCode() {
		return mAreaCode;
	}

	public UserPreference.Category getCategory() {
		return mCategory;
	}

	public String getCountryCode() {
		return mCountryCode;
	}

	public String getExtensionNumber() {
		return mExtensionNumber;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.putOpt("number", mNumber);
			obj.putOpt("areaCode", mAreaCode);
			obj.putOpt("category", mCategory.toString());
			obj.putOpt("countryCode", mCountryCode);
			obj.putOpt("extensionNumber", mExtensionNumber);

			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Phone to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mNumber = obj.optString("number", null);
		mAreaCode = obj.optString("areaCode", null);
		mCategory = UserPreference.parseCategoryString(obj.optString("category", null));
		mCountryCode = obj.optString("countryCode", null);
		mExtensionNumber = obj.optString("extensionNumber", null);
		return true;
	}
}

