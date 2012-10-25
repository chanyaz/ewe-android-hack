package com.expedia.bookings.data;

import com.mobiata.android.json.JSONable;
import org.json.JSONObject;

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

	private String mImageKey;
	private String mImageUrl;

	public Destination() {

	}

	public Destination(String destinationId, String cityName, String description) {
		mDestinationId = destinationId;
		mCity = cityName;
		mDescription = description;
	}

	public String getDestinationId() {
		return mDestinationId;
	}

	public String getCity() {
		return mCity;
	}

	public String getDescription() {
		return mDescription;
	}

	public String getImageKey() {
		return mImageKey;
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

	public void setImageMeta(String key, String url) {
		mImageKey = key;
		mImageUrl = url;
	}

	@Override
	public JSONObject toJson() {
		return null; //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		return false; //To change body of implemented methods use File | Settings | File Templates.
	}

}
