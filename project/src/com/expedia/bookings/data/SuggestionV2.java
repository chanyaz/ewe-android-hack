package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * Don't want to break old suggestion code, so this takes its place now
 */
public class SuggestionV2 implements JSONable, Comparable<SuggestionV2> {

	/**
	 * The type of result
	 */
	public enum ResultType {
		REGION,
		HOTEL
	}

	/**
	 * The type of search that happens if you use this region id
	 */
	public enum SearchType {
		CITY,
		ATTRACTION,
		AIRPORT,
		HOTEL
	}

	/**
	 * The type of region that this result denotes
	 */
	public enum RegionType {
		CITY,
		MULTICITY,
		NEIGHBORHOOD,
		POI,
		AIRPORT,
		METROCODE,
		HOTEL
	}

	// "@type" "regionResult" or "hotelResult" - indicates what type of response it is (region or hotel)
	private ResultType mResultType;

	// "t"denotes the type, indicates the search type.
	private SearchType mSearchType;

	// "rt"denotes the type, indicates the region type.
	private RegionType mRegionType;

	// "f" denotes the region full/long name, indicates the region name exactly as in the Atlas database.
	private String mFullName;

	// "d" denotes the display name, indicates the region name with the query highlighted in Bold markers for display use.
	private String mDisplayName;

	// "i" denotes the index, indicating the position of the result in the set of results. The results are not to be sorted by index. They may appear to be sorted, but the client should short them by index before rendering to user.
	private int mIndex;

	// "id" denotes the hotel id
	private int mHotelId;

	// "a" denotes the airport TLA
	private String mAirportCode;

	// "amc" denotes the multicity region id associated with the region/hotel element. We don't guarantee to have a multicity value available in this field (for all the regions)
	// "ad" denotes the hotel address
	// "ci" denotes the city where the hotel is located
	// "pr" denotes the province where the hotel is located
	// "ccc" denotes the country code TLA where the hotel is located
	// "ll" denotes the latitude and longitude coordinates
	private Location mLocation;

	public ResultType getResultType() {
		return mResultType;
	}

	public void setResultType(ResultType resultType) {
		mResultType = resultType;
	}

	public SearchType getSearchType() {
		return mSearchType;
	}

	public void setSearchType(SearchType searchType) {
		mSearchType = searchType;
	}

	public RegionType getRegionType() {
		return mRegionType;
	}

	public void setRegionType(RegionType regionType) {
		mRegionType = regionType;
	}

	public String getFullName() {
		return mFullName;
	}

	public void setFullName(String fullName) {
		mFullName = fullName;
	}

	public String getDisplayName() {
		return mDisplayName;
	}

	public void setDisplayName(String displayName) {
		mDisplayName = displayName;
	}

	public int getIndex() {
		return mIndex;
	}

	public void setIndex(int index) {
		mIndex = index;
	}

	public int getHotelId() {
		return mHotelId;
	}

	public void setHotelId(int hotelId) {
		mHotelId = hotelId;
	}

	public String getAirportCode() {
		return mAirportCode;
	}

	public void setAirportCode(String airportCode) {
		mAirportCode = airportCode;
	}

	public Location getLocation() {
		return mLocation;
	}

	public void setLocation(Location location) {
		mLocation = location;
	}

	//////////////////////////////////////////////////////////////////////////
	// Comparable

	@Override
	public int compareTo(SuggestionV2 another) {
		return mIndex - another.mIndex;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putEnum(obj, "resultType", mResultType);
			JSONUtils.putEnum(obj, "searchType", mSearchType);
			JSONUtils.putEnum(obj, "regionType", mRegionType);

			obj.putOpt("fullName", mFullName);
			obj.putOpt("displayName", mDisplayName);

			obj.put("index", mIndex);

			obj.put("hotelId", mHotelId);
			obj.putOpt("airportCode", mAirportCode);

			JSONUtils.putJSONable(obj, "location", mLocation);

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mResultType = JSONUtils.getEnum(obj, "resultType", ResultType.class);
		mSearchType = JSONUtils.getEnum(obj, "searchType", SearchType.class);
		mRegionType = JSONUtils.getEnum(obj, "regionType", RegionType.class);

		mFullName = obj.optString("fullName", null);
		mDisplayName = obj.optString("displayName", null);

		mIndex = obj.optInt("index");

		mHotelId = obj.optInt("hotelId");
		mAirportCode = obj.optString("airportCode", null);

		mLocation = JSONUtils.getJSONable(obj, "location", Location.class);

		return true;
	}

}
