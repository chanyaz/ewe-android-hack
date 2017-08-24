package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class FlightSearchResponse extends Response {

	private final List<FlightTrip> mTrips;

	private List<Location> mSearchCities;

	private Map<String, String> mAirlineNames;

	private String mObFeesDetails;

	public FlightSearchResponse() {
		mTrips = new ArrayList<FlightTrip>();
		mAirlineNames = new HashMap<String, String>();
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

	public void setAirlineNames(Map<String, String> airlineNames) {
		mAirlineNames = airlineNames;
	}

	public Map<String, String> getAirlineNames() {
		return mAirlineNames;
	}

	public void setObFeesDetails(String obFeesDetails) {
		mObFeesDetails = obFeesDetails;
	}

	public String getObFeesDetails() {
		return mObFeesDetails;
	}

	/**
	 * Compacts the memory taken up by de-duplicating String usage.
	 *
	 * Should be safe since Strings are immutable.  Seems dumb, but actually
	 * saves a TON of memory (if you have a ton of FlightTrips)
	 */
	public void compact() {
		long start = System.nanoTime();

		Map<String, String> usedStringMap = new HashMap<String, String>();

		for (FlightTrip trip : mTrips) {
			String currency = trip.getBaseFare().getCurrency();
			if (usedStringMap.containsKey(currency)) {
				currency = usedStringMap.get(currency);
				trip.getBaseFare().setCurrency(currency);
				trip.getTotalPrice().setCurrency(currency);
				trip.getTaxes().setCurrency(currency);
				trip.getFees().setCurrency(currency);
			}
			else {
				usedStringMap.put(currency, currency);
			}
		}

		Log.d("Flight search results compaction time: " + ((System.nanoTime() - start) / 1000000) + " ms");
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable
	//
	// We save the trip data in the a "matrix" format, wherein most leg data
	// can be stored in a map since it's the same.  This greatly reduces the
	// memory requirements and speeds up parsing time.

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			// Save trips while mapping out legs
			Map<String, FlightLeg> legMap = new HashMap<String, FlightLeg>();
			JSONArray trips = new JSONArray();
			for (FlightTrip trip : mTrips) {
				trips.put(trip.toJson(false));

				for (FlightLeg leg : trip.getLegs()) {
					if (!legMap.containsKey(leg.getLegId())) {
						legMap.put(leg.getLegId(), leg);
					}
				}
			}
			obj.put("trips", trips);

			// Put in legs as a single array (we can unmap them later via legId)
			JSONUtils.putJSONableList(obj, "legs", new ArrayList<FlightLeg>(legMap.values()));

			JSONUtils.putJSONableList(obj, "searchCities", mSearchCities);
			obj.putOpt("obFeesDetails", mObFeesDetails);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		// Unroll the legs
		List<FlightLeg> legs = JSONUtils.getJSONableList(obj, "legs", FlightLeg.class);
		Map<String, FlightLeg> legMap = new HashMap<String, FlightLeg>();
		for (FlightLeg leg : legs) {
			legMap.put(leg.getLegId(), leg);
		}

		// Get all flight trips and add the legs to it
		JSONArray tripArr = obj.optJSONArray("trips");
		int len = tripArr.length();
		for (int a = 0; a < len; a++) {
			FlightTrip trip = new FlightTrip();
			trip.fromJson(tripArr.optJSONObject(a), legMap);
			mTrips.add(trip);
		}

		mSearchCities = JSONUtils.getJSONableList(obj, "searchCities", Location.class);
		mObFeesDetails = obj.optString("obFeesDetails", null);

		compact();

		return true;
	}
}
