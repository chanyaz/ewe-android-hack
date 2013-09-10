package com.expedia.bookings.test.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.test.tests.pageModels.flights.FlightLegScreen;
import com.expedia.bookings.test.tests.pageModels.flights.FlightsConfirmationScreen;
import com.expedia.bookings.test.tests.pageModels.flights.FlightsSearchLoading;
import com.expedia.bookings.test.tests.pageModels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.tests.pageModels.flights.FlightsSearchScreen;

public class FlightsTestDriver extends TestDriver {

	private FlightsSearchScreen mFlightsSearchScreen;
	private FlightsSearchLoading mFlightsSearchLoading;
	private FlightsSearchResultsScreen mFlightsSearchResultsScreen;
	private FlightLegScreen mFlightLegScreen;
	private FlightsConfirmationScreen mFlightsConfirmationScreen;

	public FlightsTestDriver(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public FlightsSearchScreen flightsSearchScreen() {
		if (mFlightsSearchScreen == null) {
			mFlightsSearchScreen = new FlightsSearchScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mFlightsSearchScreen;
	}

	public FlightsSearchLoading flightsSearchLoading() {
		if (mFlightsSearchLoading == null) {
			mFlightsSearchLoading = new FlightsSearchLoading(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mFlightsSearchLoading;
	}

	public FlightsSearchResultsScreen flightsSearchResultsScreen() {
		if (mFlightsSearchResultsScreen == null) {
			mFlightsSearchResultsScreen = new FlightsSearchResultsScreen(mInstrumentation, getCurrentActivity(), mRes,
					mPreferences);
		}
		return mFlightsSearchResultsScreen;
	}

	public FlightLegScreen flightLegScreen() {
		if (mFlightLegScreen == null) {
			mFlightLegScreen = new FlightLegScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mFlightLegScreen;
	}

	public FlightsConfirmationScreen flightsConfirmationScreen() {
		if (mFlightsConfirmationScreen == null) {
			mFlightsConfirmationScreen = new FlightsConfirmationScreen(mInstrumentation, getCurrentActivity(), mRes,
					mPreferences);
		}
		return mFlightsConfirmationScreen;
	}

}
