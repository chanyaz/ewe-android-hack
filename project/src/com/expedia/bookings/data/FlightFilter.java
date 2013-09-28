package com.expedia.bookings.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

//TODO: Filter based on departure/arrival specs
public class FlightFilter {

	public static enum Sort {
		PRICE,
		DEPARTURE,
		ARRIVAL,
		DURATION
	}

	//Filter flight search by # of stops
	public static final int STOPS_ANY = -1;
	public static final int STOPS_MAX = 1; //currently specifies "One or Less Stop"
	public static final int STOPS_NONSTOP = 0;

	private Sort mSort;

	private int mStops;

	private Set<String> mPreferredAirlines;
	private boolean mHasInitPreferredAirlines = false;

	private DataSetObservable mDataSetObservable = new DataSetObservable();

	public FlightFilter() {
		mPreferredAirlines = new HashSet<String>();

		reset();
	}

	public void reset() {
		mSort = Sort.PRICE;
		mStops = STOPS_ANY;
		mPreferredAirlines.clear();
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
		mHasInitPreferredAirlines = true;
		for (FlightTrip trip : trips) {
			mPreferredAirlines.add(trip.getLeg(legPosition).getPrimaryAirlines().iterator().next());
		}
	}

	public boolean hasInitPreferredAirlines() {
		return mHasInitPreferredAirlines;
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
