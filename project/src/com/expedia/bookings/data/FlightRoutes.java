package com.expedia.bookings.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.flightlib.data.Airport;

/**
 * This class represents a series of predefined flight routes.
 * 
 * This is used in some POSes because there are so few flight routes
 * available that we just want to present the user with all options.
 */
public class FlightRoutes implements JSONable {

	private Map<String, Airport> mAirports = new HashMap<String, Airport>();

	private Map<String, List<String>> mRoutes = new HashMap<String, List<String>>();

	public void addAirport(Airport airport) {
		mAirports.put(airport.mAirportCode, airport);
	}

	public void addRoutes(String origin, List<String> destinations) {
		mRoutes.put(origin, destinations);
	}

	// TODO: Write getters that make sense

	//////////////////////////////////////////////////////////////////////////
	// JSONable interface

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONableList(obj, "airports", mAirports.values());
			JSONObject routes = new JSONObject();
			for (String origin : mRoutes.keySet()) {
				JSONUtils.putStringList(routes, origin, mRoutes.get(origin));
			}
			obj.put("routes", routes);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean fromJson(JSONObject obj) {
		List<Airport> airports = JSONUtils.getJSONableList(obj, "airports", Airport.class);
		for (Airport airport : airports) {
			addAirport(airport);
		}

		JSONObject routes = obj.optJSONObject("routes");

		Iterator<String> it = routes.keys();
		while (it.hasNext()) {
			String origin = it.next();
			addRoutes(origin, JSONUtils.getStringList(routes, origin));
		}

		return true;
	}
}
