package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.user.UserPreference;
import com.expedia.bookings.data.user.UserPreference.Category;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class Phone implements JSONable {
	private String mNumber;
	private UserPreference.Category mCategory;
	private String mCountryCode;
	private String mCountryName;
	private String mExtensionNumber;

	public Phone() {
		// Default constructor
	}

	public Phone(String number, String countryCode) {
		mNumber = number;
		mCountryCode = countryCode;
		mCategory = Category.PRIMARY;
	}

	public Phone(JSONObject obj) {
		this.fromJson(obj);
	}

	public void setNumber(String number) {
		mNumber = number;
	}

	public String getNumber() {
		return mNumber;
	}

	public void setCategory(Category category) {
		mCategory = category;
	}

	public UserPreference.Category getCategory() {
		return mCategory;
	}

	public void setCountryCode(String countryCode) {
		mCountryCode = countryCode;
	}

	public String getCountryCode() {
		return mCountryCode;
	}

	public void setCountryName(String countryName) {
		mCountryName = countryName;
	}

	public String getCountryName() {
		return mCountryName;
	}

	public void setExtensionNumber(String extensionNumber) {
		mExtensionNumber = extensionNumber;
	}

	public String getExtensionNumber() {
		return mExtensionNumber;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.putOpt("number", mNumber);
			obj.putOpt("category", mCategory.toString());
			obj.putOpt("countryCode", mCountryCode);
			obj.putOpt("countryName", mCountryName);
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
		mCategory = UserPreference.parseCategoryString(obj.optString("category", null));
		mCountryCode = obj.optString("countryCode", null);
		mCountryName = obj.optString("countryName");
		mExtensionNumber = obj.optString("extensionNumber", null);
		return true;
	}
}
