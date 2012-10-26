package com.expedia.bookings.data;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class LaunchFlightData implements JSONable {

	private List<Destination> mDestinations;

	public LaunchFlightData() {

	}

	public void setDestinations(List<Destination> destinations) {
		mDestinations = destinations;
	}

	public List<Destination> getDestinations() {
		return mDestinations;
	}

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONableList(obj, "destinations", mDestinations);
			return obj;
		}
		catch (JSONException e) {
			Log.e("LaunchFlightData toJson fail", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mDestinations = JSONUtils.getJSONableList(obj, "destinations", Destination.class);
		return true;
	}
}
