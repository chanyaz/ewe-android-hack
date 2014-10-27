package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class HotelProductResponse extends Response {
	private String mOriginalProductKey;
	private Rate mRate;

	public HotelProductResponse() {
		// default empty for JSONable
	}

	public HotelProductResponse(String originalProductKey) {
		mOriginalProductKey = originalProductKey;
	}

	public String getOriginalProductKey() {
		return mOriginalProductKey;
	}

	public void setRate(Rate rate) {
		mRate = rate;
	}

	public Rate getRate() {
		return mRate;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONable(obj, "rate", mRate);
			obj.put("originalProductKey", mOriginalProductKey);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert HotelProductResponse object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mRate = JSONUtils.getJSONable(obj, "rate", Rate.class);
		mOriginalProductKey = obj.optString("originalProductKey", null);

		return true;
	}
}
