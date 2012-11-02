package com.expedia.bookings.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Pair;

import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.model.Search;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * A general Suggestion.  At the moment, can either be a city with hotels
 * or an airport location (as airport or metro code).
 * 
 * There are a few areas of improvement, should we ever need to "go there":
 * 
 * - There are a few different ways to display the results.  Right now the
 *   parser (or user) handles this problem for us, but we're not locked
 *   into the display name.
 * 
 * - There is a regionType as well as a suggestionType that might help
 *   disambiguate results in the future
 * 
 * - There is a sort index field.  The docs say you shouldn't trust that
 *   the results are sorted but for now we are.  :P  
 *
 */
public class Suggestion implements JSONable {

	public static enum Type {
		CITY,
		ATTRACTION,
		AIRPORT,
		ADDRESS,
		HOTEL,
	}

	public Suggestion() {
		// Default constructor, required for JSONable
	}

	private String mId;
	private Type mType;
	private String mDisplayName;
	private double mLatitude;
	private double mLongitude;
	private String mAirportLocationCode;

	//////////////////////////////////////////////////////////////////////////
	// Getters/setters

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public Type getType() {
		return mType;
	}

	public void setType(Type type) {
		mType = type;
	}

	public String getDisplayName() {
		return mDisplayName;
	}

	public void setDisplayName(String displayName) {
		mDisplayName = displayName;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	public String getAirportLocationCode() {
		return mAirportLocationCode;
	}

	public void setAirportLocationCode(String airportLocationCode) {
		mAirportLocationCode = airportLocationCode;
	}
	
	//////////////////////////////////////////////////////////////////////////
	// Utility
	
	private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^(.+)\\((.+)\\)$");
	
	public Pair<String, String> splitDisplayNameForFlights() {
		Matcher m = DISPLAY_NAME_PATTERN.matcher(mDisplayName);
		if (m.matches()) {
			return new Pair<String, String>(m.group(1), m.group(2));
		}
		else {
			Log.e("Could not split display name for flight: " + mDisplayName);
			return null;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Conversion to other types (Search, JSONable)

	public Search toSearch() {
		SearchParams searchParams = new SearchParams();
		searchParams.setQuery(mDisplayName);
		searchParams.setRegionId(mId);

		switch (mType) {
		case CITY:
			searchParams.setSearchType(SearchType.CITY);
			break;
		case ADDRESS:
			searchParams.setSearchType(SearchType.ADDRESS);
			break;
		case ATTRACTION:
		case AIRPORT:
		case HOTEL:
			searchParams.setSearchType(SearchType.POI);
			break;
		default:
			searchParams.setSearchType(SearchType.FREEFORM);
			break;
		}

		searchParams.setSearchLatLon(mLatitude, mLongitude);

		return new Search(searchParams);
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("id", mId);
			JSONUtils.putEnum(obj, "type", mType);
			obj.putOpt("displayName", mDisplayName);
			obj.putOpt("latitude", mLatitude);
			obj.putOpt("longitude", mLongitude);
			obj.putOpt("airportLocationCode", mAirportLocationCode);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mId = obj.optString("id", null);
		mType = JSONUtils.getEnum(obj, "type", Type.class);
		mDisplayName = obj.optString("displayName", null);
		mLatitude = obj.optDouble("latitude");
		mLongitude = obj.optDouble("longitude");
		mAirportLocationCode = obj.optString("airportLocationCode", null);
		return true;
	}
}
