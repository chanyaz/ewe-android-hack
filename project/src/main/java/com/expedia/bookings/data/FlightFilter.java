package com.expedia.bookings.data;

import java.util.HashSet;
import java.util.Set;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

public class FlightFilter {

	public enum Sort {
		PRICE,
		DEPARTURE,
		ARRIVAL,
		DURATION,
	}

	// Filter flight search by # of stops
	private static final int STOPS_VIEW_ID_OFFSET = 1; // Ensures a positive View id
	private static final int STOPS_UNSPECIFIED = -1;

	private int mStops = STOPS_UNSPECIFIED;
	private int mDefaultNumberOfStops = STOPS_UNSPECIFIED;

	private Sort mSort;

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
		mStops = mDefaultNumberOfStops;
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

	public static int getStopsViewIdFromStopsValue(int stopsValue) {
		return stopsValue + STOPS_VIEW_ID_OFFSET;
	}

	public static int getStopsValueFromStopsViewId(int stopsViewId) {
		return stopsViewId - STOPS_VIEW_ID_OFFSET;
	}

	public void initFromFlightSearch(FlightSearch.FlightTripQuery query) {
		// Number of stops
		mDefaultNumberOfStops = query.getMaxNumberOfStops();
		mStops = mDefaultNumberOfStops;

		// Airports
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
