package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.flightlib.data.Flight;

public class FlightStatsFlightResponse extends Response implements JSONable {

	private List<Flight> mFlights;

	public FlightStatsFlightResponse() {
		mFlights = new ArrayList<Flight>();
	}

	public void addFlight(Flight flight) {
		mFlights.add(flight);
	}

	public List<Flight> getFlights() {
		return mFlights;
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
			JSONUtils.putJSONableList(obj, "flights", mFlights);
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

		mFlights = JSONUtils.getJSONableList(obj, "flights", Flight.class);

		return true;
	}

}
