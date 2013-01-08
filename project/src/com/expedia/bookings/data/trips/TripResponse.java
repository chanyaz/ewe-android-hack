package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Response;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class TripResponse extends Response {

	private List<Trip> mTrips = new ArrayList<Trip>();

	public void addTrip(Trip trip) {
		mTrips.add(trip);
	}

	public List<Trip> getTrips() {
		return mTrips;
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
			JSONUtils.putJSONableList(obj, "trips", mTrips);
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
		mTrips = JSONUtils.getJSONableList(obj, "trips", Trip.class);
		return true;
	}
}
