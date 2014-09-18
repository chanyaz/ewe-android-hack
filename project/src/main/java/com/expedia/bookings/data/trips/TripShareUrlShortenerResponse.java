package com.expedia.bookings.data.trips;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Response;
import com.mobiata.android.Log;

public class TripShareUrlShortenerResponse extends Response {

	private String mShortUrl;
	private String mLongUrl;

	public void setShortUrl(String shortUrl) {
		mShortUrl = shortUrl;
	}

	public String getShortUrl() {
		return mShortUrl;
	}

	public void setLongUrl(String longUrl) {
		mLongUrl = longUrl;
	}

	public String getLongUrl() {
		return mLongUrl;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	private static final String JSON_KEY_LONG_URL = "JSON_KEY_LONG_URL";
	private static final String JSON_KEY_SHORT_URL = "JSON_KEY_SHORT_URL";

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			obj.putOpt(JSON_KEY_LONG_URL, mLongUrl);
			obj.putOpt(JSON_KEY_SHORT_URL, mShortUrl);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Error in toJson()", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mLongUrl = obj.optString(JSON_KEY_LONG_URL);
		mShortUrl = obj.optString(JSON_KEY_SHORT_URL);
		return true;
	}
}
