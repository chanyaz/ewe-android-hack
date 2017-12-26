package com.expedia.bookings.data;

import com.expedia.bookings.utils.CalendarUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.flightlib.data.Airport;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a series of predefined flight routes.
 * 
 * This is used in some POSes because there are so few flight routes
 * available that we just want to present the user with all options.
 */
public class FlightRoutes implements JSONable {

	private static final long STALE_TIMEOUT = TimeUnit.DAYS.toMillis(1);

	private Map<String, Airport> mAirports = new HashMap<String, Airport>();

	private Map<String, List<String>> mRoutes = new HashMap<String, List<String>>();

	// Cached in memory, don't save
	private List<String> mAllDestinations = new ArrayList<String>();

	private long mTimestamp;

	public void addAirport(Airport airport) {
		mAirports.put(airport.mAirportCode, airport);
	}

	public void addRoutes(String origin, List<String> destinations) {
		mRoutes.put(origin, destinations);
	}

	public Airport getAirport(String airportCode) {
		return mAirports.get(airportCode);
	}

	public Collection<Airport> getOrigins() {
		return getAirports(mRoutes.keySet());
	}

	public Collection<Airport> getDestinations(String origin) {
		return getAirports(mRoutes.get(origin));
	}

	public Collection<Airport> getAllDestinations() {
		Collection<Airport> destinations = new HashSet<Airport>();
		for (String origin : mRoutes.keySet()) {
			destinations.addAll(getDestinations(origin));
		}
		return destinations;
	}

	public Collection<Airport> getAirports(Collection<String> airportCodes) {
		Collection<Airport> airports = new ArrayList<Airport>();
		for (String airportCode : airportCodes) {
			airports.add(mAirports.get(airportCode));
		}
		return airports;
	}

	public void markCreationTime() {
		mTimestamp = DateTime.now().getMillis();
	}

	public boolean isExpired() {
		return CalendarUtils.isExpired(mTimestamp, STALE_TIMEOUT);
	}

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
			obj.put("timestamp", mTimestamp);
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

		mTimestamp = obj.optLong("timestamp");

		return true;
	}
}
