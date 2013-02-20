package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.flightlib.data.Flight;

public class FlightStatsFlightResponse extends Response implements JSONable {

	private Flight mFlight;
	
	public FlightStatsFlightResponse() {
		// Default constructor, nothing to do
	}
	
	public void setFlight(Flight flight) {
		mFlight = flight;
	}
	
	public Flight getFlight() {
		return mFlight;
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
			JSONUtils.putJSONable(obj, "flight", mFlight);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert FlightStatsFlightResponse to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mFlight = JSONUtils.getJSONable(obj, "flight", Flight.class);

		return true;
	}

}
