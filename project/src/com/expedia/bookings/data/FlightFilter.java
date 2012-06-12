package com.expedia.bookings.data;

import java.util.HashSet;
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

	private Sort mSort;

	private Set<String> mPreferredAirlines;

	private DataSetObservable mDataSetObservable = new DataSetObservable();

	public FlightFilter() {
		mPreferredAirlines = new HashSet<String>();

		reset();
	}

	public void reset() {
		mSort = Sort.PRICE;
		mPreferredAirlines.clear();
	}

	public void setSort(Sort sort) {
		mSort = sort;
	}

	public Sort getSort() {
		return mSort;
	}

	public void toggleAirline(String airlineCode) {
		if (mPreferredAirlines.contains(airlineCode)) {
			mPreferredAirlines.remove(airlineCode);
		}
		else {
			mPreferredAirlines.add(airlineCode);
		}
	}

	public Set<String> getPreferredAirlines() {
		return mPreferredAirlines;
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
