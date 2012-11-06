package com.expedia.bookings.data;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class LaunchHotelFallbackData implements JSONable {

	private List<HotelDestination> mDestinations;

	public List<HotelDestination> getDestinations() {
		return mDestinations;
	}

	public void setDestinations(List<HotelDestination> destinations) {
		mDestinations = destinations;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		try {
			JSONUtils.putJSONableList(obj, "destinations", mDestinations);

			return obj;
		}
		catch (JSONException e) {
			Log.e("LaunchHotelFallbackData toJson fail", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mDestinations = JSONUtils.getJSONableList(obj, "destinations", HotelDestination.class);

		return true;
	}
}
