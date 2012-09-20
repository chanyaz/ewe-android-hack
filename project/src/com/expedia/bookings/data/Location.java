package com.expedia.bookings.data;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Location implements JSONable {
	private List<String> mStreetAddress;
	private String mDescription;
	private String mCity;
	private String mStateCode;
	private String mCountryCode;
	private String mPostalCode;
	private double mLatitude;
	private double mLongitude;

	// The destination id, used for disambiguation of geocoding results
	//
	// Also sometimes used for airport/metro codes
	private String mDestinationId;

	public List<String> getStreetAddress() {
		return mStreetAddress;
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

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putStringList(obj, "streetAddress", mStreetAddress);
			obj.putOpt("description", mDescription);
			obj.putOpt("city", mCity);
			obj.putOpt("stateCode", mStateCode);
			obj.putOpt("countryCode", mCountryCode);
			obj.putOpt("postalCode", mPostalCode);
			obj.putOpt("latitude", mLatitude);
			obj.putOpt("longitude", mLongitude);
			obj.putOpt("destinationId", mDestinationId);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Location object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		mStreetAddress = JSONUtils.getStringList(obj, "streetAddress");
		mDescription = obj.optString("description", null);
		mCity = obj.optString("city", null);
		mStateCode = obj.optString("stateCode", null);
		mCountryCode = LocaleUtils.convertCountryCode(obj.optString("countryCode", null));
		mPostalCode = obj.optString("postalCode", null);
		mLatitude = obj.optDouble("latitude");
		mLongitude = obj.optDouble("longitude");
		mDestinationId = obj.optString("destinationId", null);
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Location)) {
			return false;
		}

		Location other = (Location) o;

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

	public String toFormattedString() {
		String formattedString = "";

		if (mCity != null && !mCity.equals("")) {
			formattedString += mCity;
		}

		if (mStateCode != null && !mStateCode.equals("")) {
			formattedString += ", " + mStateCode;
		}

		if (mCountryCode != null && !mCountryCode.equals("") && !mCountryCode.equals("US")) {
			formattedString += ", " + mCountryCode;
		}

		return formattedString;
	}
}
