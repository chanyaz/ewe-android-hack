package com.expedia.bookings.data;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class LaunchHotelData implements JSONable {

	private List<Property> mProperties;
	private Distance.DistanceUnit mDistanceUnit;

	public LaunchHotelData() {

	}

	public LaunchHotelData(List<Property> properties, Distance.DistanceUnit distanceUnit) {
		mProperties = properties;
		mDistanceUnit = distanceUnit;
	}

	public Distance.DistanceUnit getDistanceUnit() {
		return mDistanceUnit;
	}

	public void setDistanceUnit(Distance.DistanceUnit distanceUnit) {
		mDistanceUnit = distanceUnit;
	}

	public List<Property> getProperties() {
		return mProperties;
	}

	public void setProperties(List<Property> properties) {
		mProperties = properties;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		try {
			JSONUtils.putJSONableList(obj, "properties", mProperties);
			obj.put("distanceUnit", mDistanceUnit.toString());

			return obj;
		}
		catch (JSONException e) {
			Log.e("LaunchHotelData toJson fail", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mProperties = JSONUtils.getJSONableList(obj, "properties", Property.class);
		mDistanceUnit = Distance.DistanceUnit.valueOf(obj.optString("distanceUnit",
				Distance.DistanceUnit.MILES.toString()));
		return true;
	}
}
