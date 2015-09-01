package com.mobiata.flightlib.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class AirportMap implements Comparable<AirportMap>, JSONable {

	// Version of the data, used for compatibility when changing format
	private static final int VERSION = 1;

	// Airport map data
	public int mId;
	public int mType;
	public String mName;
	public String mUrl;

	public boolean hasSufficientData() {
		return (mUrl != null && mUrl.length() > 0);
	}

	@Override
	public String toString() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("version", VERSION);
			obj.put("id", mId);
			obj.put("type", mType);
			obj.putOpt("name", mName);
			obj.putOpt("url", mUrl);

			return obj.toString(4);
		}
		catch (JSONException e) {
			return e.toString();
		}
	}

	@Override
	public int compareTo(AirportMap other) {
		if (this.mType == other.mType) {
			if (this.mName == null || other.mName == null) {
				if (this.mName == null && other.mName == null) {
					return 0;
				}
				else if (this.mName == null) {
					return -1;
				}
				else {
					return 1;
				}
			}
			else {
				return this.mName.compareTo(other.mName);
			}
		}
		else {
			return this.mType - other.mType;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AirportMap) {
			AirportMap other = (AirportMap) o;
			return (this.mType == other.mType)
					&& (this.mName != null && this.mName.equals(other.mName))
					&& (this.mUrl != null && this.mUrl.equals(other.mUrl));
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 31 * result + mType;
		result = 31 * result + ((mName == null) ? 0 : mName.hashCode());
		result = 31 * result + ((mUrl == null) ? 0 : mUrl.hashCode());

		return result;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("version", VERSION);
			obj.put("id", mId);
			obj.put("type", mType);
			obj.putOpt("name", mName);
			obj.putOpt("url", mUrl);

			return obj;
		}
		catch (JSONException e) {
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mId = obj.optInt("id");
		mType = obj.optInt("type");
		mName = obj.optString("name", null);
		mUrl = obj.optString("url", null);
		return true;
	}

}
