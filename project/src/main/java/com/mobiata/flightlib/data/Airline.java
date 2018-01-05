package com.mobiata.flightlib.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class Airline implements JSONable {

	// Version of the data, used for compatibility when changing format
	private static final int VERSION = 2;

	// Airline data
	public String mAirlineCode;
	public String mAirlineName;
	public String mAirlinePhone;
	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("version", VERSION);
			obj.putOpt("airlineCode", mAirlineCode);
			obj.putOpt("airlineName", mAirlineName);
			obj.putOpt("airlinePhone", mAirlinePhone);
			return obj;
		}
		catch (JSONException e) {
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mAirlineCode = obj.optString("airlineCode", null);
		mAirlineName = obj.optString("airlineName", null);
		mAirlinePhone = obj.optString("airlinePhone", null);
		return true;
	}
}
