package com.mobiata.flightlib.data;

import java.util.ArrayList;

import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class Airport implements JSONable {

	// Version of the data, used for compatibility when changing format
	private static final int VERSION = 1;

	// Airport data
	public String mAirportCode;
	public String mName;
	public String mCity;
	public String mStateCode;
	public String mCountryCode;
	public String mCountry;
	public DateTimeZone mTimeZone;
	public int mClassification;
	public boolean mHasInternationalTerminalI;
	public ArrayList<AirportMap> mAirportMaps;
	public double mLat;
	public double mLon;
	public String mRegionId;

	public double getLatitude() {
		return mLat;
	}

	public double getLongitude() {
		return mLon;
	}

	public boolean hasAirportMaps() {
		if (mAirportMaps != null && mAirportMaps.size() > 0) {
			for (AirportMap map : mAirportMaps) {
				if (map.hasSufficientData()) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public String toString() {
		try {
			return toJson().toString(4);
		}
		catch (JSONException e) {
			return e.toString();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("version", VERSION);
			obj.putOpt("airportCode", mAirportCode);
			obj.putOpt("name", mName);
			obj.putOpt("city", mCity);
			obj.putOpt("stateCode", mStateCode);
			obj.putOpt("countryCode", mCountryCode);
			obj.putOpt("country", mCountry);
			obj.putOpt("regionId", mRegionId);
			if (mTimeZone != null) {
				obj.putOpt("timeZone", mTimeZone.getID());
			}
			obj.putOpt("classification", mClassification);
			obj.putOpt("hasInternationalTerminalI", mHasInternationalTerminalI);
			obj.putOpt("lat", mLat);
			obj.putOpt("lon", mLon);

			if (mAirportMaps != null) {
				JSONArray arr = new JSONArray();
				for (AirportMap map : mAirportMaps) {
					arr.put(map.toJson());
				}
				obj.put("maps", arr);
			}

			return obj;
		}
		catch (JSONException e) {
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		try {
			mAirportCode = obj.optString("airportCode", null);
			mName = obj.optString("name", null);
			mCity = obj.optString("city", null);
			mStateCode = obj.optString("stateCode", null);
			mCountryCode = obj.optString("countryCode", null);
			mCountry = obj.optString("country", null);
			String timeZone = obj.optString("timeZone", null);
			mRegionId = obj.optString("regionId", null);
			if (timeZone != null) {
				mTimeZone = DateTimeZone.forID(timeZone);
			}
			else {
				// We need at least SOME sort of time zone...
				mTimeZone = DateTimeZone.getDefault();
			}
			mClassification = obj.optInt("classification");
			mHasInternationalTerminalI = obj.optBoolean("hasInternationalTerminalI");

			if (obj.has("lat")) {
				mLat = obj.optDouble("lat");
			}
			else {
				mLat = ((float) obj.optInt("latE6")) / 1e6;
			}

			if (obj.has("lon")) {
				mLat = obj.optDouble("lon");
			}
			else {
				mLon = ((float) obj.optInt("lonE6")) / 1e6;
			}

			if (obj.has("maps")) {
				mAirportMaps = new ArrayList<>();
				JSONArray arr = obj.getJSONArray("maps");
				int length = arr.length();
				for (int a = 0; a < length; a++) {
					AirportMap map = new AirportMap();
					map.fromJson(arr.getJSONObject(a));
					mAirportMaps.add(map);
				}
			}
			return true;
		}
		catch (JSONException e) {
			return false;
		}
	}
}
