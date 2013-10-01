package com.expedia.bookings.test.tests.flights.ui.regression;

import junit.framework.AssertionFailedError;

import org.joda.time.DateTime;

import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.test.tests.pageModels.flights.FlightsSearchResultRow;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.test.utils.FlightsTestDriver;

public class FlightSearchResultsSortTest extends CustomActivityInstrumentationTestCase<FlightSearchActivity> {

	private static final String TAG = "FlightSearchResultsTest";
	FlightsTestDriver mDriver;

	public FlightSearchResultsSortTest() {
		super(FlightSearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
	}

	// Helper methods

	private float getCleanFloatFromTextView(TextView t) {
		String str = t.getText().toString();
		str = str.replaceAll("[^\\d.]", "");
		return Float.parseFloat(str);
	}

	private Pair<Integer, Integer> getHourMinutePairFromHeaderTextView(TextView t) {
		String timeString = t.getText().toString();
		int hour = Integer.parseInt(timeString.substring(0, timeString.indexOf('h')));
		int minutes;
		if (timeString.indexOf('m') < timeString.indexOf('-')) {
			minutes = Integer.parseInt(timeString.substring(timeString.indexOf('h') + 2, timeString.indexOf('m')));
		}
		else {
			minutes = 0;
		}
		Pair<Integer, Integer> hourAndMinutes = new Pair<Integer, Integer>(hour, minutes);
		return hourAndMinutes;
	}

	private Pair<Integer, Integer> getHourMinutePairFromTimeTextView(TextView t) {
		String timeString = t.getText().toString();
		int hour = Integer.parseInt(timeString.substring(0, timeString.indexOf(':')));
		if (timeString.contains("PM") && hour < 12) {
			hour += 12;
		}
		int minutes = Integer.parseInt(timeString.substring(timeString.indexOf(':') + 1, timeString.lastIndexOf(' ')));
		Pair<Integer, Integer> hourAndMinutes = new Pair<Integer, Integer>(hour, minutes);
		return hourAndMinutes;
	}

	private float getTimeMillisFromTextView(TextView t, int searchOffset) {
		Pair<Integer, Integer> hourAndMinutes = getHourMinutePairFromTimeTextView(t);
		DateTime now = DateTime.now();
		DateTime searchTime = new DateTime(now).withHourOfDay(hourAndMinutes.first)
				.withMinuteOfHour(hourAndMinutes.second)
				.withSecondOfMinute(0).withMillis(0)
				.plusDays(searchOffset);
		return searchTime.getMillis();
	}

	private void setUpAndExecuteAFlightSearch(int daysOffset) throws Exception {
		mUser.setAirportsToRandomUSAirports();
		mDriver.flightsSearchScreen().clickDepartureAirportField();
		mDriver.flightsSearchScreen().clearDepartureAirportField();
		mDriver.flightsSearchScreen().enterDepartureAirport(mUser.getDepartureAirport());
		mDriver.flightsSearchScreen().clickArrivalAirportField();
		mDriver.flightsSearchScreen().clearArrivalAirportField();
		mDriver.flightsSearchScreen().enterArrivalAirport(mUser.getArrivalAirport());
		mDriver.flightsSearchScreen().clickClearSelectedDatesButton();
		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		mDriver.flightsSearchScreen().clickDate(daysOffset);
		mDriver.flightsSearchScreen().clickSearchButton();
		mDriver.waitForStringToBeGone(mDriver.flightsSearchLoading().getLoadingFlightsString());
	}

	// Test methods

	public void testSortByPrice() throws Exception {
		int dateOffset = 1;
		setUpAndExecuteAFlightSearch(dateOffset);

		// Only run test if search returned results
		if (!mDriver.searchText(mDriver.flightsSearchResultsScreen().noFlightsWereFound(), 1, false, true)) {

			// Sort by price
			mDriver.flightsSearchResultsScreen().clickSortFlightsButton();
			mDriver.delay(1);
			mDriver.flightsSearchResultsScreen().clickToSortByPrice();
			mDriver.delay(1);

			int totalFlights = mDriver.flightsSearchResultsScreen().searchResultListView().getCount();
			int flightsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount();

			// If number of flights > 1, continue with test
			if (totalFlights > 1) {
				//Initialize first flight row and its associated variables
				View topFlightRow = mDriver.flightsSearchResultsScreen().searchResultListView()
						.getChildAt(1);
				FlightsSearchResultRow previousRow = new FlightsSearchResultRow(topFlightRow);
				float previousRowPrice = getCleanFloatFromTextView(previousRow.getPriceTextView());

				// Iterate through list by section, current values with previous values
				for (int j = 0; j < totalFlights / flightsPerScreenHeight; j++) {
					for (int i = 1; i < flightsPerScreenHeight; i++) {
						View currentFlightRowView = mDriver.flightsSearchResultsScreen().searchResultListView()
								.getChildAt(i);
						FlightsSearchResultRow currentRow = new FlightsSearchResultRow(currentFlightRowView);
						float currentRowPrice = getCleanFloatFromTextView(currentRow.getPriceTextView());
						if (currentRowPrice < previousRowPrice) {
							throw new AssertionFailedError("Row's price was "
									+ currentRow.getPriceTextView().getText().toString()
									+ ", which is earlier than previously listed time "
									+ previousRow.getPriceTextView().getText().toString());
						}
						previousRowPrice = currentRowPrice;
					}
					mDriver.scrollDown();
					flightsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView()
							.getChildCount();
				}
			}
		}
	}

	public void testSortByDeparture() throws Exception {
		int dateOffset = 1;
		setUpAndExecuteAFlightSearch(dateOffset);

		// Only run test if search returned results
		if (!mDriver.searchText(mDriver.flightsSearchResultsScreen().noFlightsWereFound(), 1, false, true)) {
			// Sort by departure time
			mDriver.flightsSearchResultsScreen().clickSortFlightsButton();
			mDriver.delay(1);
			mDriver.flightsSearchResultsScreen().clickToSortByDeparture();
			mDriver.delay(1);

			int totalFlights = mDriver.flightsSearchResultsScreen().searchResultListView().getCount();
			int flightsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount();

			// If number of flights > 1, continue with test
			if (totalFlights > 1) {
				//Initialize first flight row and its associated variables
				View topFlightRow = mDriver.flightsSearchResultsScreen().searchResultListView()
						.getChildAt(1);
				FlightsSearchResultRow previousRow = new FlightsSearchResultRow(topFlightRow);
				float previousDepartureTime = getTimeMillisFromTextView(previousRow.getDepartureTimeTextView(),
						dateOffset);

				// Iterate through list by section, current values with previous values
				for (int j = 0; j < totalFlights / flightsPerScreenHeight; j++) {
					for (int i = 1; i < flightsPerScreenHeight; i++) {
						View currentFlightRowView = mDriver.flightsSearchResultsScreen().searchResultListView()
								.getChildAt(i);
						FlightsSearchResultRow currentRow = new FlightsSearchResultRow(currentFlightRowView);
						float currentRowDepartureTime = getTimeMillisFromTextView(
								currentRow.getDepartureTimeTextView(), dateOffset);
						if (currentRowDepartureTime < previousDepartureTime) {
							throw new AssertionFailedError("Row's departure time was "
									+ currentRow.getDepartureTimeTextView().getText().toString()
									+ ", which is earlier than previously listed time "
									+ previousRow.getDepartureTimeTextView().getText().toString());
						}
						previousDepartureTime = currentRowDepartureTime;
					}
					mDriver.scrollDown();
					flightsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView()
							.getChildCount();
				}
			}
		}
	}

	public void testSortByArrival() throws Exception {
		int dateOffset = 1;
		setUpAndExecuteAFlightSearch(dateOffset);
		// Only run test if search returned results
		if (!mDriver.searchText(mDriver.flightsSearchResultsScreen().noFlightsWereFound(), 1, false, true)) {
			// Sort by arrival time
			mDriver.flightsSearchResultsScreen().clickSortFlightsButton();
			mDriver.delay(1);
			mDriver.flightsSearchResultsScreen().clickToSortByArrival();
			mDriver.delay(1);
			int totalFlights = mDriver.flightsSearchResultsScreen().searchResultListView().getCount();
			int flightsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount();

			// If number of flights > 1, continue with test
			if (totalFlights > 1) {
				//Initialize first flight row and its associated variables
				View topFlightRow = mDriver.flightsSearchResultsScreen().searchResultListView()
						.getChildAt(1);
				FlightsSearchResultRow previousRow = new FlightsSearchResultRow(topFlightRow);
				int additionalDaysPreviousRow = 0;
				if (!previousRow.getMultiDayTextView().getText().toString().equals("")) {
					additionalDaysPreviousRow = (int) getCleanFloatFromTextView(previousRow.getMultiDayTextView());
				}
				float previousRowArrivalTime = getTimeMillisFromTextView(previousRow.getArrivalTimeTextView(),
						dateOffset + additionalDaysPreviousRow);

				// Iterate through list by section, current values with previous values
				for (int j = 0; j < totalFlights / flightsPerScreenHeight; j++) {
					for (int i = 1; i < flightsPerScreenHeight; i++) {
						View currentFlightRowView = mDriver.flightsSearchResultsScreen().searchResultListView()
								.getChildAt(i);
						FlightsSearchResultRow currentRow = new FlightsSearchResultRow(currentFlightRowView);
						int additionalDaysCurrentRow = 0;
						if (!currentRow.getMultiDayTextView().getText().toString().equals("")) {
							additionalDaysCurrentRow = (int) getCleanFloatFromTextView(currentRow
									.getMultiDayTextView());
						}
						float currentRowArrivalTime = getTimeMillisFromTextView(
								currentRow.getArrivalTimeTextView(), dateOffset + additionalDaysCurrentRow);
						if (currentRowArrivalTime < previousRowArrivalTime) {
							throw new AssertionFailedError("Row's arrival time was "
									+ currentRow.getArrivalTimeTextView().getText().toString()
									+ ", which is earlier than previously listed time "
									+ previousRow.getArrivalTimeTextView().getText().toString());
						}
						previousRowArrivalTime = currentRowArrivalTime;
					}
					mDriver.scrollDown();
					flightsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView()
							.getChildCount();
				}
			}
		}
	}

	public void testSortByDuration() throws Exception {
		int dateOffset = 1;
		setUpAndExecuteAFlightSearch(dateOffset);
		// Only run test if search returned results
		if (!mDriver.searchText(mDriver.flightsSearchResultsScreen().noFlightsWereFound(), 1, false, true)) {
			// Set sort by duration
			mDriver.flightsSearchResultsScreen().clickSortFlightsButton();
			mDriver.delay(1);
			mDriver.flightsSearchResultsScreen().clickToSortByDuration();
			mDriver.delay(1);
			int totalFlights = mDriver.flightsSearchResultsScreen().searchResultListView().getCount();
			final int flightsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView()
					.getChildCount();

			// Only run test if number of flights is > 1
			if (totalFlights > 1) {
				// get first flight's duration
				mDriver.flightsSearchResultsScreen().selectFlightFromList(1);
				mDriver.delay();
				Pair<Integer, Integer> previousDuration = getHourMinutePairFromHeaderTextView(mDriver
						.flightLegScreen().durationTextView());
				mDriver.goBack();
				Pair<Integer, Integer> currentDuration;
				int rowsCurrentlyShown = mDriver.flightsSearchResultsScreen().searchResultListView()
						.getChildCount() - 2;
				//iterate through list of flights, comparing currently indexed flight's duration with flight at index - 1
				for (int j = 0; j < totalFlights / flightsPerScreenHeight; j++) {
					for (int i = 1; i < rowsCurrentlyShown; i++) {
						mDriver.flightsSearchResultsScreen().selectFlightFromList(i);
						mDriver.delay();
						currentDuration = getHourMinutePairFromHeaderTextView(mDriver.flightLegScreen()
								.durationTextView());
						mDriver.goBack();
						if (currentDuration.first < previousDuration.first
								|| (currentDuration.first == previousDuration.first
								&& currentDuration.second < previousDuration.second)) {
							throw new AssertionFailedError("Duration of: " + currentDuration.first + "h "
									+ currentDuration.second + "m was not shorter than the previous duration "
									+ previousDuration.first + "h " + previousDuration.second + "m.");
						}
						previousDuration = currentDuration;
					}
					mDriver.scrollDown();
					rowsCurrentlyShown = mDriver.flightsSearchResultsScreen().searchResultListView()
							.getChildCount() - 3;
					mDriver.delay(1);
				}
			}
		}
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
