package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Pair;

import com.expedia.bookings.text.HtmlCompat;
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
		HOTEL,
		POI,
		METROCODE
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

	// "id" denotes regionId but it denotes the hotel id if RegionType == "HOTEL"
	private static final int UNKNOWN_REGION_ID = -1;
	private int mRegionId = UNKNOWN_REGION_ID;

	// "a" denotes the airport TLA
	private String mAirportCode;

	// "amc" denotes the multicity region id associated with the region/hotel element. We don't guarantee to have a multicity value available in this field (for all the regions)
	private int mMultiCityRegionId;

	// "ad" denotes the hotel address
	// "ci" denotes the city where the hotel is located
	// "pr" denotes the province where the hotel is located
	// "ccc" denotes the country code TLA where the hotel is located
	// "ll" denotes the latitude and longitude coordinates
	private Location mLocation;

	// Populated via LaunchLocation. We stuff this into SuggestionV2 for convenience.
	private String mImageCode;

	private int mIcon;

	public SuggestionV2() {
		// Default constructor, needed for JSONable
	}

	public int getIcon() {
		return mIcon;
	}

	public void setIcon(int icon) {
		mIcon = icon;
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

	public int getRegionId() {
		return mRegionId;
	}

	public void setRegionId(int regionId) {
		mRegionId = regionId;
	}

	public String getAirportCode() {
		return mAirportCode;
	}

	public void setAirportCode(String airportCode) {
		mAirportCode = airportCode;
	}

	public int getMultiCityRegionId() {
		return mMultiCityRegionId;
	}

	public void setMultiCityRegionId(int regionId) {
		mMultiCityRegionId = regionId;
	}

	public Location getLocation() {
		return mLocation;
	}

	public void setLocation(Location location) {
		mLocation = location;
	}

	public void setImageCode(String imageCode) {
		mImageCode = imageCode;
	}

	public String getImageCode() {
		return mImageCode;
	}

	public ArrayList<String> getPossibleImageCodes() {
		ArrayList<String> codes = new ArrayList<>();
		if (!TextUtils.isEmpty(getImageCode())) {
			codes.add(getImageCode());
		}
		if (getRegionId() != UNKNOWN_REGION_ID) {
			codes.add("" + getRegionId());
		}
		if (!TextUtils.isEmpty(getAirportCode())) {
			codes.add(getAirportCode());
		}
		return codes;
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
				&& mRegionId == other.mRegionId
				&& TextUtils.equals(mAirportCode, other.mAirportCode)
				&& mMultiCityRegionId == other.mMultiCityRegionId
				&& ((mLocation == null && other.mLocation == null) || (mLocation != null && mLocation
						.equals(other.mLocation)));
	}

	@Override
	public String toString() {
		return HtmlCompat.stripHtml(mDisplayName);
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

			obj.put("hotelId", mRegionId);
			obj.putOpt("airportCode", mAirportCode);
			obj.putOpt("regionId", mMultiCityRegionId);

			JSONUtils.putJSONable(obj, "location", mLocation);

			obj.putOpt("imageCode", mImageCode);

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

		mRegionId = obj.optInt("hotelId", UNKNOWN_REGION_ID);
		mAirportCode = obj.optString("airportCode", null);
		mMultiCityRegionId = obj.optInt("regionId");

		mLocation = JSONUtils.getJSONable(obj, "location", Location.class);

		mImageCode = obj.optString("imageCode", null);

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
		mRegionId = in.readInt();
		mAirportCode = in.readString();
		mMultiCityRegionId = in.readInt();
		mLocation = in.readParcelable(getClass().getClassLoader());
		mImageCode = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		ParcelUtils.writeEnum(dest, mResultType);
		ParcelUtils.writeEnum(dest, mSearchType);
		ParcelUtils.writeEnum(dest, mRegionType);
		dest.writeString(mFullName);
		dest.writeString(mDisplayName);
		dest.writeInt(mIndex);
		dest.writeInt(mRegionId);
		dest.writeString(mAirportCode);
		dest.writeInt(mMultiCityRegionId);
		dest.writeParcelable(mLocation, 0);
		dest.writeString(mImageCode);
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
