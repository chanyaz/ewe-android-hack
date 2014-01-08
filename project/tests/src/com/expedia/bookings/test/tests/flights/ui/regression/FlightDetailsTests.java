package com.expedia.bookings.test.tests.flights.ui.regression;

import android.view.View;

import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.test.tests.pageModels.flights.FlightsSearchResultRow;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.test.utils.FlightsTestDriver;

public class FlightDetailsTests extends CustomActivityInstrumentationTestCase<FlightSearchActivity> {

	private static final String TAG = FlightDetailsTests.class.getName();
	FlightsTestDriver mDriver;

	public FlightDetailsTests() {
		super(FlightSearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
	}

	private void checkBaggageFeeInfoButton() {
		mDriver.flightLegScreen().clickBaggageInfoView();
		mDriver.delay(1);
		assertTrue(mDriver.searchText(mDriver.flightLegScreen().baggageFees()));
		mDriver.goBack();
	}

	public void testOneWayFlightDetails() throws Exception {
		// search for a flight that should always be direct
		doASearch("SFO", "LAX", 2, 0);
		mDriver.scrollDown();
		mDriver.delay();
		verifyFlightDetails(true);
	}

	public void testRoundTripFlightDetails() throws Exception {
		doASearch("SFO", "LAX", 1, 2);
		mDriver.scrollDown();
		mDriver.delay();
		verifyFlightDetails(false);
	}

	// Helpers

	private void doASearch(String departure, String arrival, int departureOffset, int arrivalOffset) throws Exception {
		mDriver.flightsSearchScreen().clickClearSelectedDatesButton();
		mUser.setDepartureAirport(departure);
		mUser.setArrivalAirport(arrival);
		mDriver.flightsSearchScreen().clickDepartureAirportField();
		mDriver.flightsSearchScreen().clearDepartureAirportField();
		mDriver.flightsSearchScreen().enterDepartureAirport(mUser.getDepartureAirport());
		mDriver.flightsSearchScreen().enterArrivalAirport(mUser.getArrivalAirport());
		mDriver.delay();
		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		mDriver.flightsSearchScreen().clickDate(departureOffset);
		if (arrivalOffset > 0) {
			mDriver.flightsSearchScreen().clickDate(arrivalOffset);
		}
		mDriver.goBack();
		mDriver.flightsSearchScreen().clickSearchButton();
		mDriver.waitForStringToBeGone(mDriver.flightsSearchLoading().getLoadingFlightsString());
		mDriver.flightsSearchResultsScreen().clickSortFlightsButton();
		mDriver.flightsSearchResultsScreen().clickToSortByDuration();
		mDriver.delay();
	}

	// Extremely long helper method that helps to have me
	// not duplicate all of the code.
	// Verifies that flight details info on the card matches the flight search results info

	private void verifyFlightDetails(boolean oneWay) throws Exception {
		int numberOfFlightsToTest = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount() - 1;
		for (int i = 0; i < numberOfFlightsToTest; i++) {
			View row = mDriver.flightsSearchResultsScreen().searchResultListView().getChildAt(i);
			FlightsSearchResultRow rowModel = new FlightsSearchResultRow(row);
			String resultsFlightName = rowModel.getAirlineTextView().getText().toString();
			String resultsDepartureTime = rowModel.getDepartureTimeTextView().getText().toString();
			String resultsArrivalTime = rowModel.getArrivalTimeTextView().getText().toString();
			String resultsPriceString = rowModel.getPriceTextView().getText().toString();
			mDriver.flightsSearchResultsScreen().clickOnView(row);
			mDriver.waitForStringToBeGone(mDriver.flightLegScreen().checkingForPriceChangesString());

			String detailsFlightName = mDriver.flightLegScreen().airlineTextView().getText().toString();
			String detailsDepartureTime = mDriver.flightLegScreen().departureTimeTextView().getText().toString();
			String detailsArrivalTime = mDriver.flightLegScreen().arrivalTimeTextView().getText().toString();
			String detailsHeaderPrice = mDriver.flightLegScreen().rightHeaderView().getText().toString();
			String detailsString = mDriver.flightLegScreen().detailsTextView().getText().toString();
			String cardDurationString = detailsString.substring(0,
					detailsString.indexOf(' ', detailsString.indexOf(' ')));
			String headerDurationString = mDriver.flightLegScreen().durationTextView().getText().toString()
					.substring(0, detailsString.indexOf(' ', detailsString.indexOf(' ')));

			assertTrue(detailsFlightName.contains(resultsFlightName));
			assertEquals(resultsDepartureTime, detailsDepartureTime);
			assertEquals(resultsArrivalTime, detailsArrivalTime);
			assertTrue(detailsHeaderPrice.contains(resultsPriceString));
			assertEquals(cardDurationString, headerDurationString);

			// Test baggage fee info button
			checkBaggageFeeInfoButton();
			if (oneWay) {
				mDriver.flightLegScreen().clickCancelButton();
				mDriver.delay();
			}
			else {
				mDriver.flightLegScreen().clickSelectFlightButton();
				mDriver.waitForStringToBeGone(mDriver.flightLegScreen().checkingForPriceChangesString());
				mDriver.scrollDown();
				numberOfFlightsToTest = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount() - 1;
				for (int j = 1; j < numberOfFlightsToTest; j++) {
					row = mDriver.flightsSearchResultsScreen().searchResultListView().getChildAt(j);
					rowModel = new FlightsSearchResultRow(row);
					resultsFlightName = rowModel.getAirlineTextView().getText().toString();
					resultsDepartureTime = rowModel.getDepartureTimeTextView().getText().toString();
					resultsArrivalTime = rowModel.getArrivalTimeTextView().getText().toString();
					resultsPriceString = rowModel.getPriceTextView().getText().toString();
					mDriver.delay();
					mDriver.flightsSearchResultsScreen().clickOnView(row);
					mDriver.waitForStringToBeGone(mDriver.flightLegScreen().checkingForPriceChangesString());

					detailsFlightName = mDriver.flightLegScreen().airlineTextView().getText().toString();
					detailsDepartureTime = mDriver.flightLegScreen().departureTimeTextView().getText().toString();
					detailsArrivalTime = mDriver.flightLegScreen().arrivalTimeTextView().getText().toString();
					detailsHeaderPrice = mDriver.flightLegScreen().rightHeaderView().getText().toString();
					detailsString = mDriver.flightLegScreen().detailsTextView().getText().toString();
					cardDurationString = detailsString.substring(0,
							detailsString.indexOf(' ', detailsString.indexOf(' ')));
					headerDurationString = mDriver.flightLegScreen().durationTextView().getText().toString()
							.substring(0, detailsString.indexOf(' ', detailsString.indexOf(' ')));

					assertTrue(detailsFlightName.contains(resultsFlightName));
					assertEquals(resultsDepartureTime, detailsDepartureTime);
					assertEquals(resultsArrivalTime, detailsArrivalTime);
					assertTrue(detailsHeaderPrice.contains(resultsPriceString));
					assertEquals(cardDurationString, headerDurationString);
					mDriver.flightLegScreen().clickCancelButton();
					mDriver.delay();
				}
				mDriver.goBack();
				mDriver.goBack();
				mDriver.delay();
			}
		}
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
