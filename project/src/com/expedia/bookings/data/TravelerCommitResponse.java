package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class TravelerCommitResponse extends Response implements JSONable {

	private boolean mSucceeded;
	private String mTuid;
	private String mActivityId;

	public boolean isSucceeded() {
		return mSucceeded;
	}

	public void setSucceeded(boolean succeeded) {
		this.mSucceeded = succeeded;
	}

	public String getTuid() {
		return mTuid;
	}

	public void setTuid(String tuid) {
		this.mTuid = tuid;
	}

	public String getActivityId() {
		return mActivityId;
	}

	public void setActivityId(String activityId) {
		this.mActivityId = activityId;
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
			obj.put("succeeded", mSucceeded);
			obj.put("tuid", mTuid);
			obj.put("activityId", mActivityId);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mSucceeded = obj.optBoolean("succeeded", false);
		mTuid = obj.optString("tuid");
		mActivityId = obj.optString("activityId");

		return true;
	}
}
