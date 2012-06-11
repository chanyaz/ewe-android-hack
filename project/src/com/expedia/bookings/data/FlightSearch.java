package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FlightSearch {

	private FlightSearchParams mSearchParams = new FlightSearchParams();
	private FlightSearchResponse mSearchResponse;
	private FlightLeg[] mSelectedLegs;
	private FlightFilter[] mFilters;

	public void reset() {
		mSearchParams.reset();
		mSearchResponse = null;
		mSelectedLegs = null;
		mFilters = null;
	}

	public FlightSearchParams getSearchParams() {
		return mSearchParams;
	}

	public void setSearchResponse(FlightSearchResponse searchResponse) {
		mSearchResponse = searchResponse;

		// Clear the selected legs and filters, as we've got new results
		mSelectedLegs = null;
		mFilters = null;
	}

	public List<FlightTrip> getTrips(int legPosition) {
		return getTrips(legPosition, true);
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
	public List<FlightTrip> getTrips(int legPosition, boolean useFilter) {
		List<FlightTrip> validTrips = new ArrayList<FlightTrip>();

		final List<FlightTrip> trips = mSearchResponse.getTrips();
		final int tripCount = trips.size();
		final FlightLeg[] selectedLegs = getSelectedLegs();
		final int legCount = selectedLegs.length;

		// Filter out invalid trips
		for (int a = 0; a < tripCount; a++) {
			FlightTrip trip = trips.get(a);

			boolean addLeg = true;
			for (int b = 0; b < legCount; b++) {
				if (b != legPosition && selectedLegs[b] != null && !selectedLegs[b].equals(trip.getLeg(b))) {
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

		// Filter results (if user called for it)
		if (useFilter) {
			FlightFilter filter = getFilter(legPosition);

			// TODO: Filter based on departure/arrival specs

			// TODO: Filter out preferred airlines

			// Sort the results
			Comparator<FlightTrip> comparator;
			switch (filter.getSort()) {
			case DEPARTURE:
				comparator = new FlightTrip.DepartureComparator(legPosition);
				break;
			case ARRIVAL:
				comparator = new FlightTrip.ArrivalComparator(legPosition);
				break;
			case DURATION:
				comparator = new FlightTrip.DurationComparator(legPosition);
				break;
			case PRICE:
			default:
				comparator = FlightTrip.PRICE_COMPARATOR;
				break;
			}
			Collections.sort(validTrips, comparator);
		}

		return validTrips;
	}

	public FlightSearchResponse getSearchResponse() {
		return mSearchResponse;
	}

	public FlightLeg[] getSelectedLegs() {
		if (mSelectedLegs == null || mSelectedLegs.length != mSearchParams.getQueryLegCount()) {
			mSelectedLegs = new FlightLeg[mSearchParams.getQueryLegCount()];
		}

		return mSelectedLegs;
	}

	public void setSelectedLeg(int position, FlightLeg leg) {
		getSelectedLegs()[position] = leg;
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
}
