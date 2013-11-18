package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
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

import com.expedia.bookings.data.FlightTrip.CompareField;
import com.expedia.bookings.data.FlightTrip.FlightTripComparator;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.flightlib.data.Waypoint;

public class FlightSearch implements JSONable {

	private FlightSearchParams mSearchParams = new FlightSearchParams();
	private FlightSearchResponse mSearchResponse;
	private FlightSearchState mSearchState = new FlightSearchState();

	// Not to be saved - transitory states!
	private Map<String, FlightTrip> mFlightTripMap = new HashMap<String, FlightTrip>();
	private Map<String, FlightLeg> mFlightLegMap = new HashMap<String, FlightLeg>();
	private FlightTripQuery[] mFlightTripQueries;

	public void reset() {
		mSearchParams.reset();
		mSearchResponse = null;
		mSearchState.reset();
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
		mSearchState.reset();
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
		return mSearchState.getSelectedLegs(mSearchParams.getQueryLegCount());
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
		return mSearchState.getFilter(mSearchParams.getQueryLegCount(), legPosition, getTrips(legPosition));
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

	public FlightSearchState getSearchState() {
		return mSearchState;
	}

	public void setSearchState(FlightSearchState state) {
		mSearchState = state;
	}

	/**
	 * Returns all airports available from the SearchResponse for the given leg and departure/arrival pair.
	 * @param legNumber - which leg?
	 * @param departureAirport - is this the departure or arrival airport for the given leg?
	 * @return the airport codes that are available in the response for leg and location (departure or arrival).
	 */
	public Set<String> getAirports(int legNumber, boolean departureAirport) {
		return mSearchResponse.getAirports(legNumber, departureAirport);
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

		private Map<String, FlightTrip> mCheapestTripsByAirline;

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

						if (Collections.disjoint(preferredAirlines, leg.getPrimaryAirlines())) {
							iterator.remove();
						}
					}
				}

				// #1878. Filter by number of stops before sorting.
				mTrips = getTripsFilteredByStops(mLegPosition, mTrips, filter.getStops());

				// Filter the trips by airport filter
				mTrips = getTripsFilteredByAirport(mLegPosition, mTrips, filter.getDepartureAirports(), true);
				mTrips = getTripsFilteredByAirport(mLegPosition, mTrips, filter.getArrivalAirports(), false);

				// Handling case when tripsList might return empty after filtering by stops.
				if (mTrips.size() == 0) {
					return mTrips;
				}

				// Sort the results
				Comparator<FlightTrip> comparator;
				switch (filter.getSort()) {
				case DEPARTURE:
					comparator = new FlightTripComparator(mLegPosition, CompareField.DEPARTURE);
					break;
				case ARRIVAL:
					comparator = new FlightTripComparator(mLegPosition, CompareField.ARRIVAL);
					break;
				case DURATION:
					comparator = new FlightTripComparator(mLegPosition, CompareField.DURATION);
					break;
				case PRICE:
				default:
					comparator = new FlightTripComparator(mLegPosition, CompareField.PRICE);
					break;
				}
				Collections.sort(mTrips, comparator);

				// Calculate the min/max time, and lowest prices
				FlightTrip trip = mTrips.get(0);
				FlightLeg leg = trip.getLeg(mLegPosition);
				mMinTime = leg.getFirstWaypoint().getMostRelevantDateTime();
				mMaxTime = leg.getLastWaypoint().getMostRelevantDateTime();
				mCheapestTripsByAirline = new HashMap<String, FlightTrip>();

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

					String legAirlineCode = leg.getFirstAirlineCode();
					if (!mCheapestTripsByAirline.containsKey(legAirlineCode)
							|| trip.getTotalFare().compareTo(
									mCheapestTripsByAirline.get(legAirlineCode).getTotalFare()) < 0) {
						mCheapestTripsByAirline.put(legAirlineCode, trip);
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

		/**
		 * @return Map of airline code --> cheapest trip
		 */
		public Map<String, FlightTrip> getCheapestTripsByAirline() {
			if (mCheapestTripsByAirline == null) {
				getTrips();
			}

			return mCheapestTripsByAirline;
		}

		public void notifyFilterChanged() {
			mTrips = null;
			mMinTime = null;
			mMaxTime = null;
			mCheapestTripsByAirline = null;
			mDataSetObservable.notifyChanged();
		}

		public void registerDataSetObserver(DataSetObserver observer) {
			mDataSetObservable.registerObserver(observer);
		}

		public void unregisterDataSetObserver(DataSetObserver observer) {
			mDataSetObservable.unregisterObserver(observer);
		}

	}

	// Static filtering methods

	public static List<FlightTrip> getTripsFilteredByStops(int legPosition, List<FlightTrip> trips, int stops) {
		// For STOPS_ANY, just return the list.
		if (stops < 0) {
			return trips;
		}

		List<FlightTrip> result = new ArrayList<FlightTrip>();
		for (FlightTrip trip : trips) {
			FlightLeg flightLeg = trip.getLeg(legPosition);
			// Two segments = 1 stop, so subtract.
			if (((flightLeg.getSegmentCount() - 1) <= stops)) {
				result.add(trip);
			}
		}
		return result;
	}

	public static List<FlightTrip> getTripsFilteredByAirport(int legPosition, List<FlightTrip> trips,
			Set<String> airports, boolean departureAirport) {

		if (airports.isEmpty()) {
			return trips;
		}

		List<FlightTrip> filteredTrips = new ArrayList<FlightTrip>();
		for (FlightTrip trip : trips) {
			FlightLeg flightLeg = trip.getLeg(legPosition);
			Waypoint waypoint;
			if (departureAirport) {
				waypoint = flightLeg.getFirstWaypoint();
			}
			else {
				waypoint = flightLeg.getLastWaypoint();
			}

			if (airports.contains(waypoint.getAirport().mAirportCode)) {
				filteredTrips.add(trip);
			}
		}
		return filteredTrips;
	}

	public static Map<String, FlightTrip> getCheapestTripEachAirlineMap(int legPosition, List<FlightTrip> trips) {
		Map<String, FlightTrip> lowestPriceMap = new HashMap<String, FlightTrip>();

		Iterator<FlightTrip> iterator = trips.iterator();
		while (iterator.hasNext()) {
			FlightTrip trip = iterator.next();
			FlightLeg leg = trip.getLeg(legPosition);
			String legAirlineCode = leg.getFirstAirlineCode();

			if (lowestPriceMap.containsKey(legAirlineCode)) {
				Money inListMoney = lowestPriceMap.get(legAirlineCode).getTotalFare();
				if (trip.getTotalFare().compareTo(inListMoney) < 0) {
					lowestPriceMap.put(legAirlineCode, trip);
				}
			}
			else {
				lowestPriceMap.put(legAirlineCode, trip);
			}
		}

		return lowestPriceMap;
	}

	/**
	 * For a legNumber and departure/airport pair, return all airport codes that we have flights for.
	 * @param trips - the FlightTrips
	 * @param legNumber - which leg, 0 or 1?
	 * @param departureAirport - the first airport of the leg or the last airport, e.g. departure or arrival?
	 */
	public static Set<String> generateAirports(Collection<FlightTrip> trips, int legNumber, boolean departureAirport) {
		Set<String> codes = new HashSet<String>();

		Waypoint waypoint;
		FlightLeg leg;
		for (FlightTrip trip : trips) {
			leg = trip.getLeg(legNumber);
			waypoint = departureAirport ? leg.getFirstWaypoint() : leg.getLastWaypoint();
			codes.add(waypoint.getAirport().mAirportCode);
		}

		return codes;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONable(obj, "searchParams", mSearchParams);
			JSONUtils.putJSONable(obj, "searchResponse", mSearchResponse);
			JSONUtils.putJSONable(obj, "searchState", mSearchState);
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
		mSearchState = JSONUtils.getJSONable(obj, "searchState", FlightSearchState.class);
		return true;
	}
}
