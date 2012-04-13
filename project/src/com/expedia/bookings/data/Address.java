package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class Address implements JSONable {

	private String mCity;
	private String mProvince;
	private String mPostalCode;
	private String mCountryCode;
	private String mFirstAddressLine;
	private String mSecondAddressLine;

	public Address(JSONObject obj) {
		this.fromJson(obj);
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.putOpt("city", mCity);
			obj.putOpt("province", mProvince);
			obj.putOpt("postalCode", mPostalCode);
			obj.putOpt("countryAlpha3Code", mCountryCode);
			obj.putOpt("firstAddressLine", mFirstAddressLine);
			obj.putOpt("secondAddressLine", mSecondAddressLine);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert User to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mCity = obj.optString("city", null);
		mProvince = obj.optString("province", null);
		mPostalCode = obj.optString("postalCode", null);
		mCountryCode = obj.optString("countryAlpha3Code", null);
		mFirstAddressLine = obj.optString("firstAddressLine", null);
		mSecondAddressLine = obj.optString("secondAddressLine", null);
		return true;
	}
}

