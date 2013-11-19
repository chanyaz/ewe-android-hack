package com.expedia.bookings.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Pair;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.ParcelUtils;

/**
 * Don't want to break old suggestion code, so this takes its place now
 */
public class SuggestionV2 implements JSONable, Parcelable, Comparable<SuggestionV2> {

	/**
	 * The type of result
	 */
	public enum ResultType {
		REGION,
		HOTEL,

		// Non-ESS types - special types for our usage!
		CURRENT_LOCATION
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
	private int mRegionId;

	// "ad" denotes the hotel address
	// "ci" denotes the city where the hotel is located
	// "pr" denotes the province where the hotel is located
	// "ccc" denotes the country code TLA where the hotel is located
	// "ll" denotes the latitude and longitude coordinates
	private Location mLocation;

	public SuggestionV2() {
		// Default constructor, needed for JSONable
	}

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

	public int getRegionId() {
		return mRegionId;
	}

	public void setRegionId(int regionId) {
		mRegionId = regionId;
	}

	public Location getLocation() {
		return mLocation;
	}

	public void setLocation(Location location) {
		mLocation = location;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SuggestionV2)) {
			return false;
		}

		SuggestionV2 other = (SuggestionV2) o;

		// We purposefully do not compare display name, as its HTML can vary
		// based on how the query is constructed.  To put it another way, two
		// suggestions can be suggesting the same thing with different
		// display names.
		return mResultType == other.mResultType
				&& mSearchType == other.mSearchType
				&& mRegionType == other.mRegionType
				&& TextUtils.equals(mFullName, other.mFullName)
				&& mIndex == other.mIndex
				&& mHotelId == other.mHotelId
				&& TextUtils.equals(mAirportCode, other.mAirportCode)
				&& mRegionId == other.mRegionId
				&& ((mLocation == null && other.mLocation == null) || (mLocation != null && mLocation
						.equals(other.mLocation)));
	}

	//////////////////////////////////////////////////////////////////////////
	// Utility

	private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^(.+)\\((.+)\\)$");

	/**
	 * @return the full name, split intelligently into two lines; for display purposes
	 */
	public Pair<String, String> splitFullName() {
		Matcher m = DISPLAY_NAME_PATTERN.matcher(mFullName);
		if (m.matches()) {
			return Pair.create(m.group(1), m.group(2));
		}
		else {
			return null;
		}
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
			obj.putOpt("regionId", mRegionId);

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
		mRegionId = obj.optInt("regionId");

		mLocation = JSONUtils.getJSONable(obj, "location", Location.class);

		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Parcelable

	private SuggestionV2(Parcel in) {
		mResultType = ParcelUtils.readEnum(in, ResultType.class);
		mSearchType = ParcelUtils.readEnum(in, SearchType.class);
		mRegionType = ParcelUtils.readEnum(in, RegionType.class);
		mFullName = in.readString();
		mDisplayName = in.readString();
		mIndex = in.readInt();
		mHotelId = in.readInt();
		mAirportCode = in.readString();
		mRegionId = in.readInt();
		mLocation = in.readParcelable(getClass().getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		ParcelUtils.writeEnum(dest, mResultType);
		ParcelUtils.writeEnum(dest, mSearchType);
		ParcelUtils.writeEnum(dest, mRegionType);
		dest.writeString(mFullName);
		dest.writeString(mDisplayName);
		dest.writeInt(mIndex);
		dest.writeInt(mHotelId);
		dest.writeString(mAirportCode);
		dest.writeInt(mRegionId);
		dest.writeParcelable(mLocation, 0);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<SuggestionV2> CREATOR = new Parcelable.Creator<SuggestionV2>() {
		public SuggestionV2 createFromParcel(Parcel in) {
			return new SuggestionV2(in);
		}

		public SuggestionV2[] newArray(int size) {
			return new SuggestionV2[size];
		}
	};
}
