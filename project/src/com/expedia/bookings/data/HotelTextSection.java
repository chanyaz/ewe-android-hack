package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class HotelTextSection implements JSONable {

	private String mName;

	private String mContent;

	public HotelTextSection() {
		// Default constructor
	}

	public HotelTextSection(String name, String content) {
		mName = name;
		mContent = content;
	}

	public String getName() {
		return mName;
	}

	public String getContent() {
		return mContent;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("name", mName);
			obj.putOpt("content", mContent);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean fromJson(JSONObject obj) {
		mName = obj.optString("name", null);
		mContent = obj.optString("content", null);
		return true;
	}
}
