package com.expedia.bookings.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

public class FlightFilter {

	public static enum Sort {
		PRICE,
		DEPARTURE,
		ARRIVAL,
		DURATION,
	}

	//Filter flight search by # of stops
	public static final int STOPS_UNSPECIFIED = -2;
	public static final int STOPS_ANY = -1;
	public static final int STOPS_NONSTOP = 0;
	public static final int STOPS_MAX = 1; //currently specifies "One or Less Stop"

	private Sort mSort;

	private int mStops;

	private Set<String> mPreferredAirlines;

	private Set<String> mDepartureAirports;
	private Set<String> mArrivalAirports;

	private DataSetObservable mDataSetObservable = new DataSetObservable();

	public FlightFilter() {
		mPreferredAirlines = new HashSet<String>();
		mDepartureAirports = new HashSet<String>();
		mArrivalAirports = new HashSet<String>();

		reset();
	}

	public void reset() {
		mSort = Sort.PRICE;
		mStops = STOPS_ANY;
		mPreferredAirlines.clear();
		mDepartureAirports.clear();
		mArrivalAirports.clear();
	}

	public void setSort(Sort sort) {
		mSort = sort;
	}

	public Sort getSort() {
		return mSort;
	}

	public int getStops() {
		return mStops;
	}

	public void setStops(int stops) {
		mStops = stops;
	}

	// This filter depends upon airlines generated at runtime, dynamic based on the given FlightSearch.
	// Supply a list of trips to initialize this filter, where flights from all airlines are shown by default.
	public void initPreferredAirlines(List<FlightTrip> trips, int legPosition) {
		for (FlightTrip trip : trips) {
			mPreferredAirlines.add(trip.getLeg(legPosition).getPrimaryAirlines().iterator().next());
		}
	}

	public void initAirports(FlightSearch.FlightTripQuery query) {
		mDepartureAirports = new HashSet<String>(query.getDepartureAirportCodes());
		mArrivalAirports = new HashSet<String>(query.getArrivalAirportCodes());
	}

	public void setPreferredAirline(String airlineCode, boolean isPreferred) {
		if (isPreferred) {
			mPreferredAirlines.add(airlineCode);
		}
		else {
			mPreferredAirlines.remove(airlineCode);
		}
	}

	public Set<String> getPreferredAirlines() {
		return mPreferredAirlines;
	}

	public boolean hasPreferredAirlines() {
		return mPreferredAirlines.size() != 0;
	}

	public Set<String> getDepartureAirports() {
		return mDepartureAirports;
	}

	public Set<String> getArrivalAirports() {
		return mArrivalAirports;
	}

	/**
	 * Returns the selected airports for this given filter.
	 * @param departureAirport - true means return departure airports, false means return arrival airports
	 * @return
	 */
	public Set<String> getAirports(boolean departureAirport) {
		if (departureAirport) {
			return mDepartureAirports;
		}
		else {
			return mArrivalAirports;
		}
	}

	public void addAirport(boolean departureAirport, String airportCode) {
		getAirports(departureAirport).add(airportCode);
	}

	public void removeAirport(boolean departureAirport, String airportCode) {
		getAirports(departureAirport).remove(airportCode);
	}

	public boolean containsAirport(boolean departureAirport, String airportCode) {
		Set<String> airports = departureAirport ? mDepartureAirports : mArrivalAirports;
		return airports.contains(airportCode);
	}

	public void notifyFilterChanged() {
		mDataSetObservable.notifyChanged();
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		mDataSetObservable.registerObserver(observer);
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		mDataSetObservable.unregisterObserver(observer);
	}
}
