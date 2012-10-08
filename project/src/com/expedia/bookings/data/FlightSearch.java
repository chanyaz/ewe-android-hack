package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FlightSearch implements JSONable {

	private FlightSearchParams mSearchParams = new FlightSearchParams();
	private FlightSearchResponse mSearchResponse;
	private FlightTripLeg[] mSelectedLegs;
	private FlightFilter[] mFilters;

	// Not to be saved - transitory states!
	private Map<String, FlightTrip> mFlightTripMap = new HashMap<String, FlightTrip>();
	private Map<String, FlightLeg> mFlightLegMap = new HashMap<String, FlightLeg>();
	private FlightTripQuery[] mFlightTripQueries;

	public void reset() {
		mSearchParams.reset();
		mSearchResponse = null;
		mSelectedLegs = null;
		mFilters = null;
	}

	public void setSearchParams(FlightSearchParams params) {
		mSearchParams = params;
	}

	public FlightSearchParams getSearchParams() {
		return mSearchParams;
	}

	public void setSearchResponse(FlightSearchResponse searchResponse) {
		mSearchResponse = searchResponse;

		// If we got search locations from this, set them here
		if (searchResponse != null) {
			List<Location> searchCities = searchResponse.getSearchCities();
			if (searchCities != null && searchCities.size() == 2) {
				// We can assume (for now) that searchCity[0] == origin and searchCity[1] == destination
				mSearchParams.getDepartureLocation().updateFrom(searchCities.get(0));
				mSearchParams.getArrivalLocation().updateFrom(searchCities.get(1));
			}
		}

		// Reset the FlightTrip and FlightLeg maps
		mFlightTripMap.clear();
		if (mSearchResponse != null && mSearchResponse.getTripCount() > 0) {
			for (FlightTrip flightTrip : mSearchResponse.getTrips()) {
				mFlightTripMap.put(flightTrip.getProductKey(), flightTrip);

				for (FlightLeg leg : flightTrip.getLegs()) {
					mFlightLegMap.put(leg.getLegId(), leg);
				}
			}
		}

		// Clear the selected legs and filters, as we've got new results
		mFlightTripQueries = null;
		mSelectedLegs = null;
		mFilters = null;
	}

	public FlightTrip getFlightTrip(String productKey) {
		return mFlightTripMap.get(productKey);
	}

	public FlightLeg getFlightLeg(String legId) {
		return mFlightLegMap.get(legId);
	}

	/**
	 * Returns a list of valid FlightTrips for a particular leg.  If there
	 * are no legs selected on any other positions, then this will return
	 * *every* trip.  But if there are legs selected, then the list of 
	 * trips you can select will be limited.
	 * 
	 * It also returns the trips sorted by price, and gets rid of more expensive
	 * trips using the same leg
	 */
	public List<FlightTrip> getTrips(int legPosition) {
		List<FlightTrip> validTrips = new ArrayList<FlightTrip>();

		final List<FlightTrip> trips = mSearchResponse.getTrips();
		final int tripCount = trips.size();
		final FlightTripLeg[] selectedLegs = getSelectedLegs();
		final int legCount = selectedLegs.length;

		// Filter out invalid trips
		for (int a = 0; a < tripCount; a++) {
			FlightTrip trip = trips.get(a);

			boolean addLeg = true;
			for (int b = 0; b < legCount; b++) {
				if (b != legPosition && selectedLegs[b] != null
						&& !selectedLegs[b].getFlightLeg().equals(trip.getLeg(b))) {
					addLeg = false;
					break;
				}
			}

			if (addLeg) {
				validTrips.add(trip);
			}
		}

		// Sort the trips by price
		Collections.sort(validTrips, FlightTrip.PRICE_COMPARATOR);

		// Remove any duplicated legs between multiple trips
		//
		// Because it's sorted by price, we can just keep the first
		// entry for each leg
		Set<String> legIds = new HashSet<String>();
		Iterator<FlightTrip> it = validTrips.iterator();
		while (it.hasNext()) {
			String legId = it.next().getLeg(legPosition).getLegId();
			if (legIds.contains(legId)) {
				it.remove();
			}
			else {
				legIds.add(legId);
			}
		}

		return validTrips;
	}

	public FlightSearchResponse getSearchResponse() {
		return mSearchResponse;
	}

	public FlightTripLeg[] getSelectedLegs() {
		if (mSelectedLegs == null || mSelectedLegs.length != mSearchParams.getQueryLegCount()) {
			mSelectedLegs = new FlightTripLeg[mSearchParams.getQueryLegCount()];
		}

		return mSelectedLegs;
	}

	public void setSelectedLeg(int position, FlightTripLeg leg) {
		getSelectedLegs()[position] = leg;
	}

	// Returns the selected FlightTrip.
	//
	// Only valid if all legs are selected.  Otherwise, it returns null.
	public FlightTrip getSelectedFlightTrip() {
		FlightTripLeg[] legs = getSelectedLegs();
		for (int a = 0; a < legs.length; a++) {
			if (legs[a] == null) {
				return null;
			}
		}

		final List<FlightTrip> trips = mSearchResponse.getTrips();
		final int tripCount = trips.size();
		for (int a = 0; a < tripCount; a++) {
			FlightTrip candidate = trips.get(a);

			boolean matches = true;
			for (int b = 0; b < legs.length; b++) {
				if (!candidate.getLeg(b).equals(legs[b].getFlightLeg())) {
					matches = false;
				}
			}

			if (matches) {
				return candidate;
			}
		}

		// It shouldn't be possible to get here; if you do, something
		// logically went wrong with the app (as you were able to
		// select a flightleg combo that isn't valid).  So throw
		// a hissy fit.
		throw new RuntimeException("Somehow setup a series of flight legs that do not match any flight trips.");
	}

	public FlightFilter getFilter(int legPosition) {
		if (mFilters == null || mFilters.length != mSearchParams.getQueryLegCount()) {
			mFilters = new FlightFilter[mSearchParams.getQueryLegCount()];
		}

		if (mFilters[legPosition] == null) {
			mFilters[legPosition] = new FlightFilter();
		}

		return mFilters[legPosition];
	}

	public FlightTripQuery queryTrips(final int legPosition) {
		ensureFlightTripQueriesIntegrity();

		if (mFlightTripQueries[legPosition] == null) {
			mFlightTripQueries[legPosition] = new FlightTripQuery(legPosition);
			getFilter(legPosition).registerDataSetObserver(new DataSetObserver() {
				@Override
				public void onChanged() {
					mFlightTripQueries[legPosition].notifyFilterChanged();
				}
			});
		}

		return mFlightTripQueries[legPosition];
	}

	public void clearQuery(int legPosition) {
		ensureFlightTripQueriesIntegrity();

		mFlightTripQueries[legPosition] = null;
	}

	private void ensureFlightTripQueriesIntegrity() {
		if (mFlightTripQueries == null || mFlightTripQueries.length != mSearchParams.getQueryLegCount()) {
			mFlightTripQueries = new FlightTripQuery[mSearchParams.getQueryLegCount()];
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Trips query
	//
	// This allows one to query a set of flights and also be notified of
	// updates to the query

	public class FlightTripQuery {
		private DataSetObservable mDataSetObservable = new DataSetObservable();

		private int mLegPosition;

		private List<FlightTrip> mTrips;

		private Calendar mMinTime;
		private Calendar mMaxTime;

		public FlightTripQuery(int legPosition) {
			mLegPosition = legPosition;
		}

		public List<FlightTrip> getTrips() {
			if (mTrips == null) {
				mTrips = FlightSearch.this.getTrips(mLegPosition);

				// Filter results (if user called for it)
				FlightFilter filter = getFilter(mLegPosition);

				// TODO: Filter based on departure/arrival specs

				// Filter out preferred airlines
				// TODO: Is the preferred airline operating?  Marketing?  Currently assumes operating.
				if (filter.hasPreferredAirlines()) {
					Set<String> preferredAirlines = filter.getPreferredAirlines();

					Iterator<FlightTrip> iterator = mTrips.iterator();
					while (iterator.hasNext()) {
						FlightTrip trip = iterator.next();
						FlightLeg leg = trip.getLeg(mLegPosition);

						if (Collections.disjoint(preferredAirlines, leg.getOperatingAirlines())) {
							iterator.remove();
						}
					}
				}

				// Sort the results
				Comparator<FlightTrip> comparator;
				switch (filter.getSort()) {
				case DEPARTURE:
					comparator = new FlightTrip.DepartureComparator(mLegPosition);
					break;
				case ARRIVAL:
					comparator = new FlightTrip.ArrivalComparator(mLegPosition);
					break;
				case DURATION:
					comparator = new FlightTrip.DurationComparator(mLegPosition);
					break;
				case PRICE:
				default:
					comparator = FlightTrip.PRICE_COMPARATOR;
					break;
				}
				Collections.sort(mTrips, comparator);

				// Calculate the min/max time
				FlightTrip trip = mTrips.get(0);
				FlightLeg leg = trip.getLeg(mLegPosition);
				mMinTime = leg.getFirstWaypoint().getMostRelevantDateTime();
				mMaxTime = leg.getLastWaypoint().getMostRelevantDateTime();

				for (int a = 1; a < mTrips.size(); a++) {
					trip = mTrips.get(a);
					leg = trip.getLeg(mLegPosition);

					Calendar minTime = leg.getFirstWaypoint().getMostRelevantDateTime();
					Calendar maxTime = leg.getLastWaypoint().getMostRelevantDateTime();

					if (minTime.before(mMinTime)) {
						mMinTime = minTime;
					}
					if (maxTime.after(mMaxTime)) {
						mMaxTime = maxTime;
					}
				}
			}

			return mTrips;
		}

		public int getCount() {
			return getTrips().size();
		}

		public Calendar getMinTime() {
			if (mMinTime == null) {
				getTrips();
			}

			return mMinTime;
		}

		public Calendar getMaxTime() {
			if (mMinTime == null) {
				getTrips();
			}

			return mMaxTime;
		}

		public void notifyFilterChanged() {
			mTrips = null;
			mDataSetObservable.notifyChanged();
		}

		public void registerDataSetObserver(DataSetObserver observer) {
			mDataSetObservable.registerObserver(observer);
		}

		public void unregisterDataSetObserver(DataSetObserver observer) {
			mDataSetObservable.unregisterObserver(observer);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONable(obj, "searchParams", mSearchParams);
			JSONUtils.putJSONable(obj, "searchResponse", mSearchResponse);

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
		mSearchParams = JSONUtils.getJSONable(obj, "searchParams", FlightSearchParams.class);
		setSearchResponse(JSONUtils.getJSONable(obj, "searchResponse", FlightSearchResponse.class));

		List<FlightTripLeg> selectedLegs = JSONUtils.getJSONableList(obj, "selectedLegs", FlightTripLeg.class);
		if (selectedLegs != null) {
			mSelectedLegs = selectedLegs.toArray(new FlightTripLeg[0]);
		}

		return true;
	}
}
