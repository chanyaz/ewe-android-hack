package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class SignInResponse extends Response implements JSONable {

	private boolean mSuccess;

	// Information on the user
	private User mUser;

	public void setSuccess(boolean success) {
		mSuccess = success;
	}

	public boolean isSuccess() {
		return mSuccess;
	}

	public void setUser(User user) {
		mUser = user;
	}

	public User getUser() {
		return mUser;
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
			obj.putOpt("success", mSuccess);
			JSONUtils.putJSONable(obj, "user", mUser);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert SearchResponse to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mSuccess = obj.optBoolean("success", false);
		mUser = (User) JSONUtils.getJSONable(obj, "user", User.class);
		return true;
	}
}
