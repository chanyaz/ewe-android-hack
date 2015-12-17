package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class CarVendor implements JSONable {

	private String mCode;

	private String mShortName;
	private String mLongName;
	private String mTollFreePhone;
	private String mLocalPhone;

	private HotelMedia mLogo;

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

	public HotelMedia getLogo() {
		return mLogo;
	}

	public void setLogo(HotelMedia logo) {
		mLogo = logo;
	}

	public String getTollFreePhone() {
		return mTollFreePhone;
	}

	public void setTollFreePhone(String argName) {
		mTollFreePhone = argName;
	}

	public String getLocalPhone() {
		return mLocalPhone;
	}

	public void setLocalPhone(String argName) {
		mLocalPhone = argName;
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
			obj.putOpt("tollFreePhone", mTollFreePhone);
			obj.putOpt("localPhone", mLocalPhone);

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
		mTollFreePhone = obj.optString("tollFreePhone", null);
		mLocalPhone = obj.optString("localPhone", null);

		mLogo = JSONUtils.getJSONable(obj, "logo", HotelMedia.class);

		return true;
	}
}
