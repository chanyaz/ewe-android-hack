package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.user.User;
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

	@Override
	public boolean isSuccess() {
		return !hasErrors() && mSuccess;
	}

	public void setUser(User user) {
		mUser = user;
	}

	public User getUser() {
		return mUser;
	}

	// Shortcut if all we care about is the traveler
	//
	// This is the case when we're retrieving profile details
	// about an associated traveler
	public Traveler getTraveler() {
		if (mUser != null) {
			return mUser.getPrimaryTraveler();
		}
		return null;
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
			Log.e("Could not convert HotelSearchResponse to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mSuccess = obj.optBoolean("success", false);
		mUser = JSONUtils.getJSONable(obj, "user", User.class);
		return true;
	}
}
