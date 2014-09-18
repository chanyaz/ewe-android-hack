package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;

public class RoutesResponse extends Response {

	private FlightRoutes mFlightRoutes;
	private boolean mLoadedFromDisk;

	public RoutesResponse() {
		// Default constructor
	}

	public RoutesResponse(FlightRoutes routes) {
		mFlightRoutes = routes;
		mLoadedFromDisk = true;
	}

	public void setFlightRoutes(FlightRoutes routes) {
		mFlightRoutes = routes;
	}

	public FlightRoutes getFlightRoutes() {
		return mFlightRoutes;
	}

	public boolean wasLoadedFromDisk() {
		return mLoadedFromDisk;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable interface

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONable(obj, "flightRoutes", mFlightRoutes);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mFlightRoutes = JSONUtils.getJSONable(obj, "flightRoutes", FlightRoutes.class);
		return true;
	}
}
