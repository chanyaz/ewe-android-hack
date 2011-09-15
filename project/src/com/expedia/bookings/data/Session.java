package com.expedia.bookings.data;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class Session implements JSONable {

	/**
	 * The default expiration time for a session - 15 minutes.
	 */
	public static final long DEFAULT_EXPIRATION_TIME = 1000 * 60 * 15; // 15 minutes

	private String mSessionId;
	private long mTimeout;

	public Session() {
		// Default constructor
		mTimeout = 0;
	}

	public Session(String sessionId) {
		mSessionId = sessionId;
		resetTimeout();
	}

	public String getSessionId() {
		return mSessionId;
	}

	public void resetTimeout() {
		mTimeout = Calendar.getInstance().getTimeInMillis();
	}

	/**
	 * @return true if the session has expired, based on DEFAULT_EXPIRATION_TIME
	 */
	public boolean hasExpired() {
		return hasExpired(DEFAULT_EXPIRATION_TIME);
	}

	public boolean hasExpired(long expirationTime) {
		return mTimeout + expirationTime < Calendar.getInstance().getTimeInMillis();
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("sessionId", mSessionId);
			obj.put("timeout", mTimeout);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Session to JSON");
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mSessionId = obj.optString("sessionId", null);
		mTimeout = obj.optLong("timeout");
		return true;
	}
}
