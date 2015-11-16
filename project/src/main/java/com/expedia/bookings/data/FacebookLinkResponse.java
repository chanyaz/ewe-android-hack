package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FacebookLinkResponse extends Response implements JSONable {

	public enum FacebookLinkResponseCode {
		none, //this is not a valid code, but a default value
		success,
		notLinked,
		existing,
		loginFailed,
		error,
		nofbdatafound,
	}

	private FacebookLinkResponseCode mResponseCode = FacebookLinkResponseCode.none;

	public void setFacebookLinkResponseCode(FacebookLinkResponseCode code) {
		if (code == null) {
			mResponseCode = FacebookLinkResponseCode.none;
		}
		else {
			mResponseCode = code;
		}
	}

	public FacebookLinkResponseCode getFacebookLinkResponseCode() {
		return mResponseCode;
	}

	@Override
	public boolean isSuccess() {
		return mResponseCode.compareTo(FacebookLinkResponseCode.success) == 0;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable interface

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putEnum(obj, "responsecode", mResponseCode);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert HotelSearchResponse to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mResponseCode = JSONUtils.getEnum(obj, "responsecode", FacebookLinkResponseCode.class);
		return true;
	}
}
