package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Pair;

import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.SuggestionStrUtils;
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

	public enum Type {
		CITY,
		ATTRACTION,
		AIRPORT,
		ADDRESS,
		HOTEL,
		METROSTATION,
		TRAINSTATION,
		MULTICITY,
		NEIGHBORHOOD,
		POI,
		METROCODE,
		MULTIREGION
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
	private String mCountryCode;

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

	public String getCountryCode() {
		return mCountryCode;
	}

	public void setCountryCode(String countryCode) {
		mCountryCode = countryCode;
	}

	//////////////////////////////////////////////////////////////////////////
	// Utility

	public Pair<String, String> splitDisplayNameForFlights() {
		String formattedDisplayName = HtmlCompat.stripHtml(mDisplayName);
		return new Pair<String, String>(StrUtils.formatCityStateCountryName(formattedDisplayName),
			SuggestionStrUtils.formatAirportName(formattedDisplayName));
	}

	public HotelSearchParams toHotelSearchParams() {
		HotelSearchParams hotelSearchParams = new HotelSearchParams();
		hotelSearchParams.setQuery(mDisplayName);
		hotelSearchParams.setRegionId(mId);
		switch (mType) {
		case CITY:
		case MULTICITY:
		case NEIGHBORHOOD:
		case MULTIREGION:
			hotelSearchParams.setSearchType(SearchType.CITY);
			break;
		case ADDRESS:
			hotelSearchParams.setSearchType(SearchType.ADDRESS);
			break;
		case ATTRACTION:
		case POI:
		case METROCODE:
		case AIRPORT:
			hotelSearchParams.setSearchType(SearchType.POI);
			break;
		case HOTEL:
			hotelSearchParams.setSearchType(SearchType.HOTEL);
			break;
		default:
			hotelSearchParams.setSearchType(SearchType.FREEFORM);
			break;
		}

		hotelSearchParams.setSearchLatLon(mLatitude, mLongitude);

		return hotelSearchParams;
	}

	public Location toLocation() {
		Pair<String, String> displayName = splitDisplayNameForFlights();
		if (displayName != null) {
			Location location = new Location();

			location.setDestinationId(mAirportLocationCode);
			location.setCity(displayName.first);
			location.setDescription(displayName.second);
			location.setCountryCode(mCountryCode);

			return location;
		}

		return null;
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
