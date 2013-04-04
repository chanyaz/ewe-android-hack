package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

/**
 *
 * Class that backs a Flight destination that is used on the Launch screen. A lot of the attributes are cribbed from the
 * Location class (which has many more fields), this just contains the ones needed to for launch screen. In addition, it
 * stores the image SHA which is used to cache the image and the image URL.
 *
 */

public class Destination implements JSONable {

	private String mDestinationId;
	private String mCity;
	private String mDescription;
	private String mImageUrl;

	public Destination() {

	}

	public Destination(String destinationId, String cityName, String description, String url) {
		mDestinationId = destinationId;
		mCity = cityName;
		mDescription = description;
		mImageUrl = url;
	}

	public String getDestinationId() {
		return mDestinationId;
	}

	public String getCity() {
		return mCity;
	}

	public String getCityFormatted() {
		return mCity.split(",")[0];
	}

	public String getDescription() {
		return mDescription;
	}

	public String getImageUrl() {
		return mImageUrl;
	}

	public void setDestinationId(String destinationId) {
		mDestinationId = destinationId;
	}

	public void setCity(String city) {
		mCity = city;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("description", mDescription);
			obj.putOpt("city", mCity);
			obj.putOpt("destinationId", mDestinationId);
			obj.putOpt("imageUrl", mImageUrl);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Location object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mDescription = obj.optString("description", null);
		mCity = obj.optString("city", null);
		mDestinationId = obj.optString("destinationId", null);
		mImageUrl = obj.optString("imageUrl", null);
		return true;
	}

}
