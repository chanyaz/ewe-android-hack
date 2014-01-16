package com.expedia.bookings.data;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * Represents the different parts of the results flow.
 *
 * We keep this separate so that we can quickly save it
 * to disk, without having to save the rest of the flight
 * search data.
 *
 */
public class FlightSearchState implements JSONable {

	private FlightTripLeg[] mSelectedLegs;
	private boolean isFlightAdded; // Check to ensure that flight has actually be added by the user (tablet) as opposed to just selecting it.

	// We don't save Filters across executions because who cares?
	private FlightFilter[] mFilters;

	public void reset() {
		mSelectedLegs = null;
		mFilters = null;
	}

	public FlightTripLeg[] getSelectedLegs(int expectedLength) {
		if (mSelectedLegs == null || mSelectedLegs.length != expectedLength) {
			mSelectedLegs = new FlightTripLeg[expectedLength];
		}

		return mSelectedLegs;
	}

	public FlightFilter getFilter(int expectedLength, int legPosition, List<FlightTrip> trips,
			FlightSearch.FlightTripQuery query) {
		if (mFilters == null || mFilters.length != expectedLength) {
			mFilters = new FlightFilter[expectedLength];
		}

		if (mFilters[legPosition] == null) {
			mFilters[legPosition] = new FlightFilter();

			// A new filter should be initialized with a list of FlightTrips
			mFilters[legPosition].initAirports(query);
		}

		return mFilters[legPosition];
	}

	public boolean isFlightAdded() {
		return isFlightAdded;
	}

	public void setFlightAdded(boolean isFlightAdded) {
		this.isFlightAdded = isFlightAdded;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			if (mSelectedLegs != null) {
				JSONUtils.putJSONableList(obj, "selectedLegs", Arrays.asList(mSelectedLegs));
			}
			obj.putOpt("isFlightAdded", isFlightAdded);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		List<FlightTripLeg> selectedLegs = JSONUtils.getJSONableList(obj, "selectedLegs", FlightTripLeg.class);
		if (selectedLegs != null) {
			mSelectedLegs = selectedLegs.toArray(new FlightTripLeg[0]);
		}
		isFlightAdded = obj.optBoolean("isFlightAdded");
		return true;
	}
}
