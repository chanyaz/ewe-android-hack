package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class Policy implements JSONable {

	public static final int TYPE_GUARANTEE = 1;
	public static final int TYPE_CANCEL = 2;
	public static final int TYPE_CHECK_IN = 3;
	public static final int TYPE_CHECK_OUT = 4;
	public static final int TYPE_DEPOSIT = 5;
	public static final int TYPE_EXTRA_PERSON = 6;
	public static final int TYPE_PREPAYMENT = 7;
	public static final int TYPE_OTHER_INFO = 8;
	public static final int TYPE_TAX = 9; // This is really more of a boolean for HP
	public static final int TYPE_NONREFUNDABLE = 10;
	public static final int TYPE_IMMEDIATE_CHARGE = 11;

	private int mType;
	private String mDescription;

	public int getType() {
		return mType;
	}

	public void setType(int type) {
		this.mType = type;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("type", mType);
			obj.putOpt("description", mDescription);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Policy object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		mType = obj.optInt("type");
		mDescription = obj.optString("description", null);

		return true;
	}

	@Override
	public String toString() {
		JSONObject obj = toJson();
		try {
			return obj.toString(2);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}
}
