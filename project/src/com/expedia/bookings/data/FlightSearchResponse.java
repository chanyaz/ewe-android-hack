package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;

public class FlightSearchResponse extends Response {

	private List<FlightTrip> mTrips;

	private List<Location> mSearchCities;

	public FlightSearchResponse() {
		mTrips = new ArrayList<FlightTrip>();
	}

	public void addTrip(FlightTrip trip) {
		mTrips.add(trip);
	}

	public FlightTrip getTrip(int position) {
		return mTrips.get(position);
	}

	public List<FlightTrip> getTrips() {
		return mTrips;
	}

	public int getTripCount() {
		if (mTrips == null) {
			return 0;
		}

		return mTrips.size();
	}

	public void addSearchCity(Location location) {
		if (mSearchCities == null) {
			mSearchCities = new ArrayList<Location>();
		}
		mSearchCities.add(location);
	}

	public List<Location> getSearchCities() {
		return mSearchCities;
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
			JSONUtils.putJSONableList(obj, "searchCities", mSearchCities);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mTrips = JSONUtils.getJSONableList(obj, "trips", FlightTrip.class);
		mSearchCities = JSONUtils.getJSONableList(obj, "searchCities", Location.class);
		return true;
	}
}
