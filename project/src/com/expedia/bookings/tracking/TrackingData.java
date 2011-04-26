package com.expedia.bookings.tracking;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.mobiata.android.Log;
import com.mobiata.android.util.IoUtils;
import com.mobiata.hotellib.data.JSONable;

public class TrackingData implements JSONable {

	private static final String FILENAME = "1984.dat";

	private String mVersion;

	public void setVersion(String version) {
		mVersion = version;
	}

	public String getVersion() {
		return mVersion;
	}

	public boolean save(Context context) {
		try {
			IoUtils.writeStringToFile(FILENAME, toJson().toString(), context);
			return true;
		}
		catch (IOException e) {
			Log.e("Could not write tracking data file.", e);
			return false;
		}
	}

	public boolean load(Context context) {
		try {
			fromJson(new JSONObject(IoUtils.readStringFromFile(FILENAME, context)));
			return true;
		}
		catch (Exception e) {
			Log.e("Could not read tracking data file.", e);
			return false;
		}
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		try {
			obj.putOpt("version", mVersion);
		}
		catch (JSONException e) {
			Log.e("Could not convert TrackingData to JSON", e);
			return null;
		}

		return obj;
	}

	@Override
	public void fromJson(JSONObject obj) {
		mVersion = obj.optString("version", null);
	}
}
