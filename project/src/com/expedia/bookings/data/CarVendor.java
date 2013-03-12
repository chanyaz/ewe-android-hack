package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class CarVendor implements JSONable {

	private String mCode;

	private String mShortName;
	private String mLongName;
	private String mPhoneNumberPrimary;
	private String mPhoneNumberAlternate;

	private Media mLogo;

	public String getCode() {
		return mCode;
	}

	public void setCode(String code) {
		mCode = code;
	}

	public String getShortName() {
		return mShortName;
	}

	public void setShortName(String shortName) {
		mShortName = shortName;
	}

	public String getLongName() {
		return mLongName;
	}

	public void setLongName(String longName) {
		mLongName = longName;
	}

	public Media getLogo() {
		return mLogo;
	}

	public void setLogo(Media logo) {
		mLogo = logo;
	}

	public String getPhoneNumberPrimary() {
		return mPhoneNumberPrimary;
	}

	public void setPhoneNumberPrimary(String argName) {
		mPhoneNumberPrimary = argName;
	}

	public String getPhoneNumberAlternate() {
		return mPhoneNumberAlternate;
	}

	public void setPhoneNumberAlternate(String argName) {
		mPhoneNumberAlternate = argName;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();

			obj.putOpt("code", mCode);
			obj.putOpt("shortName", mShortName);
			obj.putOpt("longName", mLongName);
			obj.putOpt("phoneNumberPrimary", mPhoneNumberPrimary);
			obj.putOpt("phoneNumberAlternate", mPhoneNumberAlternate);

			JSONUtils.putJSONable(obj, "logo", mLogo);

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mCode = obj.optString("code", null);
		mShortName = obj.optString("shortName", null);
		mLongName = obj.optString("longName", null);
		mPhoneNumberPrimary = obj.optString("phoneNumberPrimary", null);
		mPhoneNumberAlternate = obj.optString("phoneNumberAlternate", null);

		mLogo = JSONUtils.getJSONable(obj, "logo", Media.class);

		return true;
	}
}
