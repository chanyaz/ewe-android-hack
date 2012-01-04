package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class User implements JSONable {

	private String mEmail;
	private String mFirstName;
	private String mLastName;

	public void setEmail(String email) {
		mEmail = email;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setFirstName(String firstName) {
		mFirstName = firstName;
	}

	public String getFirstName() {
		return mFirstName;
	}

	public void setLastName(String lastName) {
		mLastName = lastName;
	}

	public String getLastName() {
		return mLastName;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable interface

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.putOpt("email", mEmail);
			obj.putOpt("firstName", mFirstName);
			obj.putOpt("lastName", mLastName);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert User to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mEmail = obj.optString("email", null);
		mFirstName = obj.optString("firstName", null);
		mLastName = obj.optString("lastName", null);
		return true;
	}
}
