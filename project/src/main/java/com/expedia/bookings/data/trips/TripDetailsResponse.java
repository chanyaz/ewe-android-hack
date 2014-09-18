package com.expedia.bookings.data.trips;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Response;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class TripDetailsResponse extends Response {

	private Trip mTrip;

	public void setTrip(Trip trip) {
		mTrip = trip;
	}

	public Trip getTrip() {
		return mTrip;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONable(obj, "trip", mTrip);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert TripResponse object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mTrip = JSONUtils.getJSONable(obj, "trip", Trip.class);
		return true;
	}
}
