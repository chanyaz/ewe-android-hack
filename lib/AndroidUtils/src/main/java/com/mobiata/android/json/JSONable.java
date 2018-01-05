package com.mobiata.android.json;

import org.json.JSONObject;

public interface JSONable {
	/**
	 * Convert this object into a JSONObject.
	 * @return the JSONObject if successful, null if not.
	 */
	public JSONObject toJson();

	/**
	 * Parse a JSONObject and use it to fuel this object.
	 * @param obj the JSONObject to parse.
	 * @return true if successfully parsed
	 */
	public boolean fromJson(JSONObject obj);
}
