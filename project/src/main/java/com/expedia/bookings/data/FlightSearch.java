package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.text.TextUtils;

import com.expedia.bookings.data.FlightTrip.CompareField;
import com.expedia.bookings.data.FlightTrip.FlightTripComparator;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;

/**
 * Contains both the parameters (see {@link com.expedia.bookings.data.FlightSearchParams}) and search
 * results (see {@link com.expedia.bookings.data.FlightSearchResponse}) for a flight search.
 */
public class FlightSearch implements JSONable {

	private FlightSearchParams mSearchParams = new FlightSearchParams();
	private FlightSearchResponse mSearchResponse;
	private FlightSearchState mSearchState = new FlightSearchState();

	private final Map<Flight, FlightStatsRating> mFlightStatsRatings = new HashMap<Flight, FlightStatsRating>();

	// Not to be saved - transitory states!
	private final Map<String, FlightTrip> mFlightTripMap = new HashMap<String, FlightTrip>();
	private final Map<String, FlightLeg> mFlightLegMap = new HashMap<String, FlightLeg>();
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
	 * <p/>
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
		FlightTripLeg[] legs = getSelectedLegs();
		legs[position] = leg;
	}

	public void clearSelectedLegs() {
		for (int i = 0; i < getSelectedLegs().length; i++) {
			setSelectedLeg(i, null);
		}
	}

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
		for (int i = 0; i < legs.length; i++) {
			Log.e("leg=" + i + " content: " + legs[i]);
		}
		throw new RuntimeException("Somehow setup a series of flight legs that do not match any flight trips.");
	}

	public FlightFilter getFilter(int legPosition) {
		return mSearchState.getFilter(mSearchParams.getQueryLegCount(), legPosition, queryTrips(legPosition));
	}

	public FlightTripQuery queryTrips(final int legPosition) {
		ensureFlightTripQueriesIntegrity();

		if (mFlightTripQueries[legPosition] == null) {
			mFlightTripQueries[legPosition] = new FlightTripQuery(legPosition);
			getFilter(legPosition).registerDataSetObserver(new DataSetObserver() {
				@Override
				public void onChanged() {
					FlightTripQuery query = mFlightTripQueries[legPosition];
					if (query != null) {
						query.notifyFilterChanged();
					}
				}
			});
		}

		return mFlightTripQueries[legPosition];
	}

	public void clearQuery(int legPosition) {
		ensureFlightTripQueriesIntegrity();

		// We must null out both the Query and the Filter in order
		// to ensure the FlightTripQuery.getTrips() call will retrieve
		// the FlightTrips from an unadultered state. The airport filter
		// depends upon dynamic data from the FlightTripQuery so it must
		// be nulled out and regenerated when the query changes.
		mFlightTripQueries[legPosition] = null;
		mSearchState.getFilters(mSearchParams.getQueryLegCount())[legPosition] = null;
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

	//////////////////////////////////////////////////////////////////////////
	// Trips query
	//
	// This allows one to query a set of flights and also be notified of
	// updates to the query

	public class FlightTripQuery {
		private DataSetObservable mDataSetObservable = new DataSetObservable();

		private int mLegPosition;

		private List<FlightTrip> mTrips;

		private Map<String, FlightTrip> mCheapestTripsByAirline;

		private Map<String, FlightTrip> mCheapestTripsByDepartureAirport;
		private Map<String, FlightTrip> mCheapestTripsByArrivalAirport;

		private Set<String> mDepartureAirportCodes;
		private Set<String> mArrivalAirportCodes;

		private Set<String> mAirlinesFilteredByStopsAndAirports;

		private ArrayList<Integer> mNumberOfStops;

		private DateTime mMinTime;
		private DateTime mMaxTime;

		public FlightTripQuery(int legPosition) {
			mLegPosition = legPosition;
		}

		public List<FlightTrip> getTrips() {
			if (mTrips == null) {
				// Retrieve all the trips
				mTrips = FlightSearch.this.getTrips(mLegPosition);

				mDepartureAirportCodes = new HashSet<String>();
				mArrivalAirportCodes = new HashSet<String>();
				mAirlinesFilteredByStopsAndAirports = new HashSet<String>();

				mCheapestTripsByDepartureAirport = new HashMap<String, FlightTrip>();
				mCheapestTripsByArrivalAirport = new HashMap<String, FlightTrip>();
				mCheapestTripsByAirline = new TreeMap<String, FlightTrip>(new Comparator<String>() {
					@Override
					public int compare(String lhs, String rhs) {
						String lhsAirlineName = Db.getAirline(lhs).mAirlineName;
						String rhsAirlineName = Db.getAirline(rhs).mAirlineName;
						if (TextUtils.isEmpty(lhsAirlineName) && !TextUtils.isEmpty(rhsAirlineName)) {
							return 1;
						}
						else if (!TextUtils.isEmpty(lhsAirlineName) && TextUtils.isEmpty(rhsAirlineName)) {
							return -1;
						}
						else if (TextUtils.isEmpty(lhsAirlineName) && TextUtils.isEmpty(rhsAirlineName)) {
							return 0;
						}
						else {
							return lhsAirlineName.compareTo(rhsAirlineName);
						}
					}
				});

				// Run a first pass over all trips to calculate cheapest trips per airline/airport
				FlightTrip trip;
				FlightLeg leg;
				Set stopsSet = new HashSet<Integer>();
				for (int i = 0; i < mTrips.size(); i++) {
					trip = mTrips.get(i);
					leg = trip.getLeg(mLegPosition);

					mDepartureAirportCodes.add(leg.getAirport(true).mAirportCode);
					mArrivalAirportCodes.add(leg.getAirport(false).mAirportCode);
					stopsSet.add(Integer.valueOf(leg.getSegmentCount() - 1));

					// calculate cheapest trip per airport/airline
					evaluateTripPrice(leg.getAirport(true).mAirportCode, trip, mCheapestTripsByDepartureAirport);
					evaluateTripPrice(leg.getAirport(false).mAirportCode, trip, mCheapestTripsByArrivalAirport);
					for (String airline : leg.getPrimaryAirlines()) {
						evaluateTripPrice(airline, trip, mCheapestTripsByAirline);
					}
				}
				mNumberOfStops = new ArrayList<Integer>(stopsSet);
				Collections.sort(mNumberOfStops);

				// Filter results (if user called for it)
				FlightFilter filter = getFilter(mLegPosition);

				// Filter trips by number of stops and by airports
				filterTripsByStops(mLegPosition, mTrips, filter.getStops());
				filterTripsByAirport(mLegPosition, mTrips, filter.getDepartureAirports(), true);
				filterTripsByAirport(mLegPosition, mTrips, filter.getArrivalAirports(), false);

				// Generate a list of airlines available after being filtered by stops/airports but
				// before filtered by airline. This data will be used to correctly display the airline
				// filter in the UI
				for (int i = 0; i < mTrips.size(); i++) {
					trip = mTrips.get(i);
					leg = trip.getLeg(mLegPosition);
					for (String airline : leg.getPrimaryAirlines()) {
						mAirlinesFilteredByStopsAndAirports.add(airline);
					}
				}

				// Filter out preferred airlines
				filterTripsByAirline(mLegPosition, mTrips, filter.getPreferredAirlines());

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

				// Run a second pass over the filtered trips

				// Calculate the min/max time, and lowest prices
				if (mTrips.size() > 0) {
					trip = mTrips.get(0);
					leg = trip.getLeg(mLegPosition);
					mMinTime = leg.getFirstWaypoint().getMostRelevantDateTime();
					mMaxTime = leg.getLastWaypoint().getMostRelevantDateTime();
				}

				// Calculate a set of of cheapest trips per airport/airline
				Map<String, FlightTrip> depAirportMap = new HashMap<String, FlightTrip>();
				Map<String, FlightTrip> arrAirportMap = new HashMap<String, FlightTrip>();
				Map<String, FlightTrip> airlineMap = new HashMap<String, FlightTrip>();

				for (int a = 0; a < mTrips.size(); a++) {
					trip = mTrips.get(a);
					leg = trip.getLeg(mLegPosition);

					DateTime minTime = leg.getFirstWaypoint().getMostRelevantDateTime();
					DateTime maxTime = leg.getLastWaypoint().getMostRelevantDateTime();

					if (minTime.isBefore(mMinTime)) {
						mMinTime = minTime;
					}
					if (maxTime.isAfter(mMaxTime)) {
						mMaxTime = maxTime;
					}

					// Generate the cheapest trips maps for the filtered trips
					evaluateTripPrice(leg.getAirport(true).mAirportCode, trip, depAirportMap);
					evaluateTripPrice(leg.getAirport(false).mAirportCode, trip, arrAirportMap);
					evaluateTripPrice(leg.getFirstAirlineCode(), trip, airlineMap);

				}

				// Update the cheapest trips to reflect the filtered trips
				updateCheapestTrips(mCheapestTripsByDepartureAirport, depAirportMap);
				updateCheapestTrips(mCheapestTripsByArrivalAirport, arrAirportMap);
				updateCheapestTrips(mCheapestTripsByAirline, airlineMap);
			}

			return mTrips;
		}

		public int getCount() {
			return getTrips().size();
		}

		public Map<String, FlightTrip> getCheapestTripsByAirline() {
			ensureCalculations();
			return mCheapestTripsByAirline;
		}

		public Map<String, FlightTrip> getCheapestTripsByDepartureAirport() {
			ensureCalculations();
			return mCheapestTripsByDepartureAirport;
		}

		public Map<String, FlightTrip> getCheapestTripsByArrivalAirport() {
			ensureCalculations();
			return mCheapestTripsByArrivalAirport;
		}

		public Map<String, FlightTrip> getCheapestTripsByAirport(boolean departureAirport) {
			if (departureAirport) {
				return getCheapestTripsByDepartureAirport();
			}
			else {
				return getCheapestTripsByArrivalAirport();
			}
		}

		public Set<String> getAirportCodes(boolean departureAirport) {
			if (departureAirport) {
				return getDepartureAirportCodes();
			}
			else {
				return getArrivalAirportCodes();
			}
		}

		public Set<String> getDepartureAirportCodes() {
			ensureCalculations();
			return mDepartureAirportCodes;
		}

		public Set<String> getArrivalAirportCodes() {
			ensureCalculations();
			return mArrivalAirportCodes;
		}

		public List<Integer> getNumberOfStops() {
			ensureCalculations();
			return mNumberOfStops;
		}

		public int getMaxNumberOfStops() {
			ensureCalculations();
			if (mNumberOfStops.size() == 0) {
				return 0;
			}
			return mNumberOfStops.get(mNumberOfStops.size() - 1).intValue();
		}

		public Set<String> getAirlinesFilteredByStopsAndAirports() {
			ensureCalculations();
			return mAirlinesFilteredByStopsAndAirports;
		}

		// If this trip is in fact cheapest, add it to the cheapest trip map
		private void evaluateTripPrice(String key, FlightTrip trip, Map<String, FlightTrip> lowestPriceMap) {
			FlightTrip cheapest = lowestPriceMap.get(key);
			if (cheapest == null || trip.getAverageTotalFare().compareTo(cheapest.getAverageTotalFare()) < 0) {
				lowestPriceMap.put(key, trip);
			}
		}

		// loop through the cheapest trips from the filtered set and update the prices in the entire set
		private void updateCheapestTrips(Map<String, FlightTrip> all, Map<String, FlightTrip> filtered) {
			// Update the cheapest chips map to reflect the cheapest
			for (String key : filtered.keySet()) {
				all.put(key, filtered.get(key));
			}
		}

		private void filterTripsByStops(int legPosition, List<FlightTrip> trips, int stops) {
			// For STOPS_ANY, just return the list.
			if (stops < 0) {
				return;
			}

			FlightLeg leg;
			Iterator<FlightTrip> iterator = trips.iterator();
			while (iterator.hasNext()) {
				leg = iterator.next().getLeg(legPosition);

				if (leg.getSegmentCount() - 1 > stops) {
					iterator.remove();
				}
			}
		}

		private void filterTripsByAirport(int legPos, List<FlightTrip> trips, Set<String> airports,
										  boolean departureAirport) {
			if (airports.isEmpty()) {
				return;
			}

			FlightLeg leg;
			Airport airport;
			Iterator<FlightTrip> iterator = trips.iterator();
			while (iterator.hasNext()) {
				leg = iterator.next().getLeg(legPos);
				airport = leg.getAirport(departureAirport);

				if (!airports.contains(airport.mAirportCode)) {
					iterator.remove();
				}
			}
		}

		// We filter based upon the marketing airline (not the operating airline)
		private void filterTripsByAirline(int legPos, List<FlightTrip> trips, Set<String> airlines) {
			// Special case: if no airlines are provided, this means no filtering is to take place
			if (airlines.size() == 0) {
				return;
			}

			FlightLeg leg;
			Iterator<FlightTrip> iterator = trips.iterator();
			while (iterator.hasNext()) {
				leg = iterator.next().getLeg(legPos);

				if (Collections.disjoint(airlines, leg.getPrimaryAirlines())) {
					iterator.remove();
				}
			}
		}

		private void ensureCalculations() {
			if (mTrips == null) {
				getTrips();
			}
		}

		public DateTime getMinTime() {
			if (mMinTime == null) {
				getTrips();
			}

			return mMinTime;
		}

		public DateTime getMaxTime() {
			if (mMinTime == null) {
				getTrips();
			}

			return mMaxTime;
		}

		public void notifyFilterChanged() {
			mTrips = null;
			mMinTime = null;
			mMaxTime = null;
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
	// FlightStats flight rating - not persisting to disk currently because who cares?

	public FlightStatsRating getFlightStatsRating(Flight flight) {
		return mFlightStatsRatings.get(flight);
	}

	public void addFlightStatsRating(Flight flight, FlightStatsRating rating) {
		mFlightStatsRatings.put(flight, rating);
	}

	//////////////////////////////////////////////////////////////////////////
	// Booking / Checkout / Trip bucket conversion

	/**
	 * A stripped down version of the FlightSearch used only for checkout
	 */
	public FlightSearch generateForTripBucket() {
		FlightSearch flightSearch = new FlightSearch();

		flightSearch.mSearchParams = mSearchParams.clone();

		return flightSearch;
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
