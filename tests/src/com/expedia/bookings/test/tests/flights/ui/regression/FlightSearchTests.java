package com.expedia.bookings.test.tests.flights.ui.regression;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.test.utils.FlightsTestDriver;

public class FlightSearchTests extends CustomActivityInstrumentationTestCase<FlightSearchActivity> {

	private static final String TAG = "FlightSearchTests";
	FlightsTestDriver mDriver;

	public FlightSearchTests() {
		super(FlightSearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
	}

	public void testDuplicateAirportSearchGivesErrorMessage() throws Exception {
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
	}

	public void testGuestButtonTextView() throws Exception {
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
		mUser.setAirportsToRandomUSAirports();
		mDriver.flightsSearchScreen().clickDepartureAirportField();
		mDriver.flightsSearchScreen().enterDepartureAirport(mUser.getDepartureAirport());
		mDriver.flightsSearchScreen().clickArrivalAirportField();
		mDriver.flightsSearchScreen().enterArrivalAirport(mUser.getArrivalAirport());
		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		mDriver.flightsSearchScreen().clickDate(1);
		mDriver.delay(1);
		mDriver.flightsSearchScreen().clickClearSelectedDatesButton();
		mDriver.delay(3);
		String buttonString = mDriver.flightsSearchScreen().selectDepartureButton().getHint().toString();
		String expectedString = getString(R.string.hint_select_departure);
		assertEquals(expectedString, buttonString);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
