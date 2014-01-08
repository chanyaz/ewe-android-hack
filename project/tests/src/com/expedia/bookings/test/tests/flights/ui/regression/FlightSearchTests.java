package com.expedia.bookings.test.tests.flights.ui.regression;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.test.utils.FlightsTestDriver;
import com.expedia.bookings.utils.ClearPrivateDataUtil;

public class FlightSearchTests extends CustomActivityInstrumentationTestCase<SearchActivity> {

	private static final String TAG = "FlightSearchTests";
	FlightsTestDriver mDriver;

	public FlightSearchTests() {
		super(SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		ClearPrivateDataUtil.clear(mContext);
		mDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser.setAirportsToRandomUSAirports();
	}

	public void testDuplicateAirportSearchGivesErrorMessage() throws Exception {
		mDriver.enterLog(TAG, "START TEST: Duplicate airport search gives error message.");
		mDriver.launchScreen().launchFlights();
		mDriver.flightsSearchScreen().clickDepartureAirportField();
		mDriver.flightsSearchScreen().enterDepartureAirport("SFO");
		mDriver.flightsSearchScreen().clickArrivalAirportField();
		mDriver.flightsSearchScreen().enterArrivalAirport("SFO");
		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		mDriver.flightsSearchScreen().clickDate(1);
		mDriver.flightsSearchScreen().clickSearchButton();
		mDriver.delay();
		if (!mDriver.searchText(mDriver.flightsSearchScreen().departureAndArrivalAreTheSameErrorMessage())) {
			throw new Exception(
					"Searching for a flight with the same departure and arrival airports didn't yield the appropriate error message");
		}
		mDriver.clickOnText(mDriver.flightsSearchScreen().okString());
	}

	public void testGuestButtonTextView() throws Exception {
		mDriver.enterLog(TAG, "START TEST: Number of guests shows correctly in textview");
		mDriver.launchScreen().launchFlights();
		for (int i = 1; i <= 6; i++) {
			mDriver.flightsSearchScreen().clickPassengerSelectionButton();
			mDriver.delay(1);
			String adultQuantity = mDriver.flightsSearchScreen().getAdultsQuantityString(i);
			mDriver.clickOnText(adultQuantity);
			mDriver.delay();
			String numberOfPassengersShownString = mDriver.flightsSearchScreen().passengerNumberTextView().getText()
					.toString();
			int numberOfPassengersShown = Integer.parseInt(numberOfPassengersShownString);
			if (!(numberOfPassengersShown == i)) {
				throw new Exception("Number of guests shown in text view: " + numberOfPassengersShown
						+ " does not equal the number selected " + i);
			}
		}
	}

	public void testClearingSelectedDates() throws Exception {
		mDriver.enterLog(TAG, "START TEST: Clearing selected dates works.");
		mUser.setAirportsToRandomUSAirports();
		mDriver.launchScreen().launchFlights();
		mDriver.flightsSearchScreen().clickDepartureAirportField();
		mDriver.flightsSearchScreen().enterDepartureAirport(mUser.getDepartureAirport());
		mDriver.flightsSearchScreen().clickArrivalAirportField();
		mDriver.flightsSearchScreen().enterArrivalAirport(mUser.getArrivalAirport());
		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		mDriver.delay();
		mDriver.flightsSearchScreen().clickDate(1);
		mDriver.delay(1);
		mDriver.flightsSearchScreen().clickClearSelectedDatesButton();
		mDriver.delay(3);
		String buttonString = mDriver.flightsSearchScreen().selectDepartureButton().getHint().toString();
		String expectedString = getString(R.string.hint_select_departure);
		assertEquals(expectedString, buttonString);
	}

	public void testBackingOutOfASearchAndResuming() throws Exception {
		// Not crashing during the course of this test IS passing
		mDriver.enterLog(TAG, "START TEST: Backing out of test during loading screen, and restarting it.");
		mDriver.launchScreen().launchFlights();
		mDriver.flightsSearchScreen().enterDepartureAirport(mUser.getDepartureAirport());
		mDriver.flightsSearchScreen().enterArrivalAirport(mUser.getArrivalAirport());
		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		mDriver.delay();
		mDriver.flightsSearchScreen().clickDate(0);
		mDriver.flightsSearchScreen().clickSearchButton();
		mDriver.delay(1);
		mDriver.goBack();
		mDriver.flightsSearchScreen().clickSearchButton();
		mDriver.waitForStringToBeGone(mDriver.flightsSearchLoading().getLoadingFlightsString());
	}

	public void testTryingToSearchWithMissingInfo() {
		mDriver.enterLog(TAG, "START TEST: Can't search until all data is added.");
		mDriver.launchScreen().launchFlights();
		mDriver.flightsSearchScreen().enterDepartureAirport(mUser.getDepartureAirport());
		try {
			mDriver.flightsSearchScreen().clickSearchButton();
		}
		catch (Error e) {
			mDriver.enterLog(TAG, "Wasn't able to click the search button without arrival, as expected.");
		}
		mDriver.flightsSearchScreen().enterArrivalAirport(mUser.getArrivalAirport());
		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		try {
			mDriver.flightsSearchScreen().clickSearchButton();
		}
		catch (Error e) {
			mDriver.enterLog(TAG, "Wasn't able to click the search button without date, as expected.");
		}
		mDriver.delay();
		mDriver.flightsSearchScreen().clickDate(0);
		mDriver.delay();
		assertTrue(mDriver.flightsSearchScreen().searchButton().isShown());
		mDriver.enterLog(TAG, "Successfully asserted that the search button is shown.");
	}

	public void testOneWayInternationalFlight() throws Exception {
		// Not crashing during the course of this test IS passing
		// Also, actually getting search results
		mDriver.enterLog(TAG, "START TEST: One way international flight search scenario");
		mDriver.launchScreen().launchFlights();
		mUser.setAirportsToRandomINTLAirports();
		mDriver.flightsSearchScreen().enterDepartureAirport(mUser.getDepartureAirport());
		mDriver.flightsSearchScreen().enterArrivalAirport(mUser.getArrivalAirport());
		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		mDriver.delay();
		mDriver.flightsSearchScreen().clickDate(1);
		mDriver.flightsSearchScreen().clickSearchButton();
		mDriver.waitForStringToBeGone(mDriver.flightsSearchLoading().getLoadingFlightsString());
	}

	public void testRoundTripInternationalFlight() throws Exception {
		// Not crashing during the course of this test IS passing
		// Also, actually getting search results
		mDriver.enterLog(TAG, "START TEST: Round trip international flight search scenario");
		mDriver.launchScreen().launchFlights();
		mUser.setAirportsToRandomINTLAirports();
		mDriver.flightsSearchScreen().enterDepartureAirport(mUser.getDepartureAirport());
		mDriver.flightsSearchScreen().enterArrivalAirport(mUser.getArrivalAirport());
		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		mDriver.delay();
		mDriver.flightsSearchScreen().clickDate(1);
		mDriver.flightsSearchScreen().clickDate(2);
		mDriver.flightsSearchScreen().clickSearchButton();
		mDriver.waitForStringToBeGone(mDriver.flightsSearchLoading().getLoadingFlightsString());
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
