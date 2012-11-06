package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class HotelDestination implements JSONable {

	// These vars are for the LaunchScreen
	private String mLaunchTileText;
	private String mImgUrl;

	// These vars are for PhoneSearchActivity (in order to perform a search without disambiguation)
	private String mRegionId;
	private double mLatitude;
	private double mLongitude;

	private String mPhoneSearchDisplayText;

	public void setPhoneSearchDisplayText(String phoneSearchDisplayText) {
		mPhoneSearchDisplayText = phoneSearchDisplayText;
	}

	public String getPhoneSearchDisplayText() {
		return mPhoneSearchDisplayText;
	}

	public HotelDestination setLaunchTileText(String displayText) {
		mLaunchTileText = displayText;
		return this;
	}

	public String getLaunchTileText() {
		return mLaunchTileText;
	}

	public HotelDestination setImgUrl(String url) {
		mImgUrl = url;
		return this;
	}

	public String getImgUrl() {
		return mImgUrl;
	}

	public HotelDestination setRegionId(String regionId) {
		mRegionId = regionId;
		return this;
	}

	public String getRegionId() {
		return mRegionId;
	}

	public HotelDestination setLatitudeLongitude(double lat, double lon) {
		mLatitude = lat;
		mLongitude = lon;
		return this;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();

		try {
			obj.put("imgUrl", mImgUrl);
			obj.put("launchTileText", mLaunchTileText);
			obj.put("regionId", mRegionId);
			obj.put("latitude", mLatitude);
			obj.put("longitude", mLongitude);
			obj.put("phoneSearchDisplayText", mPhoneSearchDisplayText);
			return obj;
		}
		catch (JSONException e) {
			Log.e("HotelDestination toJson fail", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		try {
			mImgUrl = obj.getString("imgUrl");
			mLaunchTileText = obj.getString("launchTileText");
			mRegionId = obj.getString("regionId");
			mLatitude = obj.getDouble("latitude");
			mLongitude = obj.getDouble("longitude");
			mPhoneSearchDisplayText = obj.getString("phoneSearchDisplayText");
		}
		catch (JSONException e) {
			Log.e("HotelDestination fromJson fail", e);
		}

		return true;
	}
}
