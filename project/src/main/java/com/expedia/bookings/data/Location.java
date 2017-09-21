package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Location implements JSONable, Parcelable {

	private int mLocationId;

	private List<String> mStreetAddress;
	private String mDescription;
	private String mCity;
	private String mStateCode;
	private String mCountryCode;
	private String mCountryName;
	private String mPostalCode;
	private double mLatitude;
	private double mLongitude;

	// The destination id, used for disambiguation of geocoding results
	//
	// Also sometimes used for airport/metro codes
	private String mDestinationId;

	// Returned in the FlightSearchResponse for the given search airport codes
	private String mSearchType;

	// Returned from SuggestionResponseHandler for the region type of the given search
	private String mRegionType;

	public Location() {
		// Default constructor
	}

	public Location(Location other) {
		if (other != null) {
			fromJson(other.toJson());
		}
	}

	public void setLocationId(int id) {
		mLocationId = id;
	}

	public int getLocationId() {
		return mLocationId;
	}

	public List<String> getStreetAddress() {
		return mStreetAddress;
	}

	public String getStreetAddressLine1() {
		if (mStreetAddress.size() >= 1) {
			return mStreetAddress.get(0);
		}
		return "";
	}

	public String getStreetAddressLine2() {
		if (mStreetAddress.size() >= 2) {
			return mStreetAddress.get(1);
		}
		return "";
	}

	public String getStreetAddressString() {
		if (mStreetAddress == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (String string : mStreetAddress) {
			sb.append(string + "\n");
		}
		return sb.toString().trim();
	}

	public void setStreetAddress(List<String> streetAddress) {
		mStreetAddress = streetAddress;
	}

	// Handy shortcut
	public void addStreetAddressLine(String line) {
		if (!TextUtils.isEmpty(line)) {
			if (mStreetAddress == null) {
				mStreetAddress = new ArrayList<String>();
			}

			mStreetAddress.add(line);
		}
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

	public String getCity() {
		return mCity;
	}

	public void setCity(String city) {
		this.mCity = city;
	}

	public String getStateCode() {
		return mStateCode;
	}

	public void setStateCode(String stateCode) {
		this.mStateCode = stateCode;
	}

	public String getCountryCode() {
		return mCountryCode;
	}

	public void setCountryCode(String countryCode) {
		this.mCountryCode = LocaleUtils.convertCountryCode(countryCode);
	}

	public String getPostalCode() {
		return mPostalCode;
	}

	public void setPostalCode(String postalCode) {
		this.mPostalCode = postalCode;
	}

	public String getCountyName() {
		return mCountryName;
	}

	public void setCountryName(String countryName) {
		this.mCountryName = countryName;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		this.mLatitude = latitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		this.mLongitude = longitude;
	}

	public String getDestinationId() {
		return mDestinationId;
	}

	public void setDestinationId(String destinationId) {
		this.mDestinationId = destinationId;
	}

	public String getSearchType() {
		return mSearchType;
	}

	public void setSearchType(String searchType) {
		mSearchType = searchType;
	}

	public String getRegionType() {
		return mRegionType;
	}

	public void setRegionType(String regionType) {
		mRegionType = regionType;
	}

	// Update this Location's fields with data from another, without blowing
	// away any data currently stored here (if there's no new value)
	public void updateFrom(Location other) {
		// Skip if we're trying to update from ourself
		if (this == other) {
			return;
		}

		if (other.mStreetAddress != null && other.mStreetAddress.size() > 0) {
			mStreetAddress = other.mStreetAddress;
		}
		if (!TextUtils.isEmpty(other.mDescription)) {
			mDescription = other.mDescription;
		}
		if (!TextUtils.isEmpty(other.mCity)) {
			mCity = other.mCity;
		}
		if (!TextUtils.isEmpty(other.mStateCode)) {
			mStateCode = other.mStateCode;
		}
		if (!TextUtils.isEmpty(other.mCountryCode)) {
			mCountryCode = other.mCountryCode;
		}
		if (!TextUtils.isEmpty(other.mCountryName)) {
			mCountryName = other.mCountryName;
		}
		if (!TextUtils.isEmpty(other.mPostalCode)) {
			mPostalCode = other.mPostalCode;
		}
		if (other.mLatitude != 0) {
			mLatitude = other.mLatitude;
		}
		if (other.mLongitude != 0) {
			mLongitude = other.mLongitude;
		}
		if (!TextUtils.isEmpty(other.mDestinationId)) {
			mDestinationId = other.mDestinationId;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONAble

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putStringList(obj, "streetAddress", mStreetAddress);
			obj.putOpt("description", mDescription);
			obj.putOpt("city", mCity);
			obj.putOpt("stateCode", mStateCode);
			obj.putOpt("countryCode", mCountryCode);
			obj.putOpt("postalCode", mPostalCode);
			obj.putOpt("countryName", mCountryName);
			obj.putOpt("latitude", mLatitude);
			obj.putOpt("longitude", mLongitude);
			obj.putOpt("destinationId", mDestinationId);
			obj.putOpt("searchType", mSearchType);
			obj.putOpt("regionType", mRegionType);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Location object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mStreetAddress = JSONUtils.getStringList(obj, "streetAddress");
		mDescription = obj.optString("description", null);
		mCity = obj.optString("city", null);
		mStateCode = obj.optString("stateCode", null);
		mCountryCode = LocaleUtils.convertCountryCode(obj.optString("countryCode", null));
		mPostalCode = obj.optString("postalCode", null);
		mCountryName = obj.optString("countryName", null);
		mLatitude = obj.optDouble("latitude", 0);
		mLongitude = obj.optDouble("longitude", 0);
		mDestinationId = obj.optString("destinationId", null);
		mSearchType = obj.optString("searchType", null);
		mRegionType = obj.optString("regionType", null);
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Location)) {
			return false;
		}

		Location other = (Location) o;

		if (this == other) {
			return true;
		}

		return (mStreetAddress == null) == (other.mStreetAddress == null)
				&& (mStreetAddress == null || mStreetAddress.equals(other.mStreetAddress))
				&& ((mDescription == null) == (other.mDescription == null))
				&& (mDescription == null || mDescription.equals(other.mDescription))
				&& ((mCity == null) == (other.mCity == null))
				&& (mCity == null || mCity.equals(other.mCity))
				&& ((mStateCode == null) == (other.mStateCode == null))
				&& (mStateCode == null || mStateCode.equals(other.mStateCode))
				&& ((mCountryCode == null) == (other.mCountryCode == null))
				&& (mCountryCode == null || mCountryCode.equals(other.mCountryCode))
				&& ((mPostalCode == null) == (other.mPostalCode == null))
				&& (mPostalCode == null || mPostalCode.equals(other.mPostalCode))
				&& ((mDestinationId == null) == (other.mDestinationId == null))
				&& (mDestinationId == null || mDestinationId.equals(other.mDestinationId))
				&& mLatitude == other.mLatitude && mLongitude == other.mLongitude;
	}

	@Override
	public String toString() {
		JSONObject obj = toJson();
		try {
			return obj.toString(2);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}

	public String toShortFormattedString() {
		ArrayList<String> locationParts = new ArrayList<String>();
		locationParts.add(mCity);
		locationParts.add(mStateCode);
		if (!TextUtils.equals(mCountryCode, "US")) {
			locationParts.add(mCountryCode);
		}

		return Strings.joinWithoutEmpties(", ", locationParts);
	}

	public String toLongFormattedString() {
		String streetAddress = getStreetAddressString();
		if (streetAddress != null && TextUtils.isDigitsOnly(streetAddress)
				&& TextUtils.isEmpty(toShortFormattedString())) {
			return null;
		}

		ArrayList<String> locationParts = new ArrayList<String>();
		locationParts.add(getStreetAddressString());
		locationParts.add(toShortFormattedString());
		locationParts.add(mPostalCode);
		return Strings.joinWithoutEmpties(", ", locationParts);
	}

	public String toTwoLineAddressFormattedString() {
		ArrayList<String> locationParts = new ArrayList<String>();
		locationParts.add(mCity);
		locationParts.add(mStateCode);
		locationParts.add(mCountryName);
		locationParts.add(mPostalCode);

		return Strings.joinWithoutEmpties(", ", locationParts);
	}

	public String toCityStateCountryAddressFormattedString() {
		ArrayList<String> locationParts = new ArrayList<String>();
		locationParts.add(mCity);
		locationParts.add(mStateCode);
		locationParts.add(mCountryName);

		return Strings.joinWithoutEmpties(", ", locationParts);
	}

	//////////////////////////////////////////////////////////////////////////
	// Parcelable

	private Location(Parcel in) {
		mStreetAddress = new ArrayList<String>();
		in.readList(mStreetAddress, getClass().getClassLoader());
		mDescription = in.readString();
		mCity = in.readString();
		mStateCode = in.readString();
		mCountryCode = in.readString();
		mCountryName = in.readString();
		mPostalCode = in.readString();
		mLatitude = in.readDouble();
		mLongitude = in.readDouble();
		mDestinationId = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeList(mStreetAddress);
		dest.writeString(mDescription);
		dest.writeString(mCity);
		dest.writeString(mStateCode);
		dest.writeString(mCountryCode);
		dest.writeString(mCountryName);
		dest.writeString(mPostalCode);
		dest.writeDouble(mLatitude);
		dest.writeDouble(mLongitude);
		dest.writeString(mDestinationId);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
		public Location createFromParcel(Parcel in) {
			return new Location(in);
		}

		public Location[] newArray(int size) {
			return new Location[size];
		}
	};
}
