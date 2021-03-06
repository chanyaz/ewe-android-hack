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

	public FlightFilter getFilter(int expectedLength, int legPosition, FlightSearch.FlightTripQuery query) {
		ensureFilterIntegrity(expectedLength);

		if (mFilters[legPosition] == null) {
			mFilters[legPosition] = new FlightFilter();
			mFilters[legPosition].initFromFlightSearch(query);
		}

		return mFilters[legPosition];
	}

	public FlightFilter[] getFilters(int expectedLength) {
		ensureFilterIntegrity(expectedLength);
		return mFilters;
	}

	private void ensureFilterIntegrity(int expectedLength) {
		if (mFilters == null || mFilters.length != expectedLength) {
			mFilters = new FlightFilter[expectedLength];
		}
	}

	public FlightSearchState clone() {
		FlightSearchState state = new FlightSearchState();
		state.mSelectedLegs = new FlightTripLeg[mSelectedLegs.length];

		for (int i = 0; i < mSelectedLegs.length; i++) {
			state.mSelectedLegs[i] = mSelectedLegs[i];
		}

		return state;
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
		return true;
	}
}
