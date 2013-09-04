package com.expedia.bookings.test.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.test.tests.pageModels.flights.FlightLegScreen;
import com.expedia.bookings.test.tests.pageModels.flights.FlightsSearchLoading;
import com.expedia.bookings.test.tests.pageModels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.tests.pageModels.flights.FlightsSearchScreen;

public class FlightsTestDriver extends TestDriver {

	private FlightsSearchScreen mFlightsSearchScreen;
	private FlightsSearchLoading mFlightsSearchLoading;
	private FlightsSearchResultsScreen mFlightsSearchResultsScreen;
	private FlightLegScreen mFlightLegScreen;

	public FlightsTestDriver(Instrumentation instrumentation, Activity activity, Resources res) {
		super(instrumentation, activity, res);
		mFlightsSearchScreen = null;
		mFlightsSearchLoading = null;
		mFlightsSearchResultsScreen = null;
		mFlightLegScreen = null;
	}

	public FlightsSearchScreen flightsSearchScreen() {
		if (mFlightsSearchScreen == null) {
			mFlightsSearchScreen = new FlightsSearchScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mFlightsSearchScreen;
	}

	public FlightsSearchLoading flightsSearchLoading() {
		if (mFlightsSearchLoading == null) {
			mFlightsSearchLoading = new FlightsSearchLoading(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mFlightsSearchLoading;
	}

	public FlightsSearchResultsScreen flightsSearchResultsScreen() {
		if (mFlightsSearchResultsScreen == null) {
			mFlightsSearchResultsScreen = new FlightsSearchResultsScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mFlightsSearchResultsScreen;
	}

	public FlightLegScreen flightLegScreen() {
		if (mFlightLegScreen == null) {
			mFlightLegScreen = new FlightLegScreen(mInstrumentation, getCurrentActivity(), mRes);
		}
		return mFlightLegScreen;
	}

}
