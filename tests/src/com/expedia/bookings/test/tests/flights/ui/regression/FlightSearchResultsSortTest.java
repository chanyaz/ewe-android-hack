package com.expedia.bookings.test.tests.flights.ui.regression;

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
		DateTime now = new DateTime();
		DateTime searchTime = now.withHourOfDay(hourAndMinutes.first).withMinuteOfHour(hourAndMinutes.second)
				.withSecondOfMinute(0).withMillis(0)
				.plusDays(searchOffset);
		mDriver.enterLog(TAG, "HEY! " + searchTime.toString());
		return searchTime.getMillis();
	}

	private void setUpAndExecuteAFlightSearch() throws Exception {
		mUser.setAirportsToRandomUSAirports();
		mDriver.flightsSearchScreen().clickDepartureAirportField();
		mDriver.flightsSearchScreen().clearDepartureAirportField();
		mDriver.flightsSearchScreen().enterDepartureAirport(mUser.getDepartureAirport());
		mDriver.flightsSearchScreen().clickArrivalAirportField();
		mDriver.flightsSearchScreen().clearArrivalAirportField();
		mDriver.flightsSearchScreen().enterArrivalAirport(mUser.getArrivalAirport());
		mDriver.flightsSearchScreen().clickClearSelectedDatesButton();
		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		mDriver.flightsSearchScreen().clickDate(1);
		mDriver.flightsSearchScreen().clickSearchButton();
		mDriver.waitForStringToBeGone(mDriver.flightsSearchLoading().getLoadingFlightsString());
	}

	// Test methods

	public void testSortByPrice() throws Exception {
		setUpAndExecuteAFlightSearch();
		if (!mDriver.searchText(mDriver.flightsSearchResultsScreen().noFlightsWereFound(), 1, false, true)) {
			mDriver.flightsSearchResultsScreen().clickSortFlightsButton();
			mDriver.delay(1);
			mDriver.flightsSearchResultsScreen().clickToSortByPrice();
			mDriver.delay(1);
			int totalHotels = mDriver.flightsSearchResultsScreen().searchResultListView().getCount();
			int hotelsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount();
			if (totalHotels > 1) {
				View topHotelRow = mDriver.flightsSearchResultsScreen().searchResultListView()
						.getChildAt(1);
				FlightsSearchResultRow previousRow = new FlightsSearchResultRow(topHotelRow);
				float previousRowPrice = getCleanFloatFromTextView(previousRow.getPriceTextView());

				for (int j = 0; j < totalHotels / hotelsPerScreenHeight; j++) {
					for (int i = 1; i < hotelsPerScreenHeight; i++) {
						View currentHotelRowView = mDriver.flightsSearchResultsScreen().searchResultListView()
								.getChildAt(i);
						FlightsSearchResultRow currentRow = new FlightsSearchResultRow(currentHotelRowView);
						float currentRowPrice = getCleanFloatFromTextView(currentRow.getPriceTextView());
						mDriver.enterLog(TAG, "PRICE " + currentRowPrice + " >= " + previousRowPrice);
						assertTrue(currentRowPrice >= previousRowPrice);
						previousRowPrice = currentRowPrice;
					}
					mDriver.scrollDown();
					hotelsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount();
				}
			}
		}
	}

	public void testSortByDeparture() throws Exception {
		setUpAndExecuteAFlightSearch();
		if (!mDriver.searchText(mDriver.flightsSearchResultsScreen().noFlightsWereFound(), 1, false, true)) {
			mDriver.flightsSearchResultsScreen().clickSortFlightsButton();
			mDriver.delay(1);
			mDriver.flightsSearchResultsScreen().clickToSortByDeparture();
			mDriver.delay(1);
			int totalHotels = mDriver.flightsSearchResultsScreen().searchResultListView().getCount();
			int hotelsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount();
			if (totalHotels > 1) {
				View topHotelRow = mDriver.flightsSearchResultsScreen().searchResultListView()
						.getChildAt(1);
				FlightsSearchResultRow previousRow = new FlightsSearchResultRow(topHotelRow);
				float previousDepartureTime = getTimeMillisFromTextView(previousRow.getDepartureTimeTextView(), 1);

				for (int j = 0; j < totalHotels / hotelsPerScreenHeight; j++) {
					for (int i = 1; i < hotelsPerScreenHeight; i++) {
						View currentHotelRowView = mDriver.flightsSearchResultsScreen().searchResultListView()
								.getChildAt(i);
						FlightsSearchResultRow currentRow = new FlightsSearchResultRow(currentHotelRowView);
						float currentRowDepartureTime = getTimeMillisFromTextView(
								currentRow.getDepartureTimeTextView(), 1);
						mDriver.enterLog(TAG, "DEPARTURE " + currentRowDepartureTime + " >= " + previousDepartureTime);
						assertTrue(currentRowDepartureTime >= previousDepartureTime);
						previousDepartureTime = currentRowDepartureTime;
					}
					mDriver.scrollDown();
					hotelsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount();
				}
			}
		}
	}

	public void testSortByArrival() throws Exception {
		setUpAndExecuteAFlightSearch();
		if (!mDriver.searchText(mDriver.flightsSearchResultsScreen().noFlightsWereFound(), 1, false, true)) {
			mDriver.flightsSearchResultsScreen().clickSortFlightsButton();
			mDriver.delay(1);
			mDriver.flightsSearchResultsScreen().clickToSortByArrival();
			mDriver.delay(1);
			int totalHotels = mDriver.flightsSearchResultsScreen().searchResultListView().getCount();
			int hotelsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount();
			if (totalHotels > 1) {
				View topHotelRow = mDriver.flightsSearchResultsScreen().searchResultListView()
						.getChildAt(1);
				FlightsSearchResultRow previousRow = new FlightsSearchResultRow(topHotelRow);
				float previousArrivalTime = getTimeMillisFromTextView(previousRow.getArrivalTimeTextView(), 1);

				for (int j = 0; j < totalHotels / hotelsPerScreenHeight; j++) {
					for (int i = 1; i < hotelsPerScreenHeight; i++) {
						View currentHotelRowView = mDriver.flightsSearchResultsScreen().searchResultListView()
								.getChildAt(i);
						FlightsSearchResultRow currentRow = new FlightsSearchResultRow(currentHotelRowView);
						float currentRowArrivalTime = getTimeMillisFromTextView(
								currentRow.getArrivalTimeTextView(), 1);
						mDriver.enterLog(TAG, "ARRIVAL " + currentRowArrivalTime + " >= " + previousArrivalTime);
						assertTrue(currentRowArrivalTime >= previousArrivalTime);
						previousArrivalTime = currentRowArrivalTime;
					}

					mDriver.scrollDown();
					hotelsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount();
				}
			}
		}
	}

	public void testSortByDuration() throws Exception {
		setUpAndExecuteAFlightSearch();
		if (!mDriver.searchText(mDriver.flightsSearchResultsScreen().noFlightsWereFound(), 1, false, true)) {
			mDriver.flightsSearchResultsScreen().clickSortFlightsButton();
			mDriver.delay(1);
			mDriver.flightsSearchResultsScreen().clickToSortByDuration();
			mDriver.delay(1);
			int totalHotels = mDriver.flightsSearchResultsScreen().searchResultListView().getCount();
			int hotelsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount();
			if (totalHotels > 1) {
				View topHotelRow = mDriver.flightsSearchResultsScreen().searchResultListView()
						.getChildAt(1);
				FlightsSearchResultRow previousRow = new FlightsSearchResultRow(topHotelRow);
				float previousArrivalTime = getTimeMillisFromTextView(previousRow.getArrivalTimeTextView(), 1);
				float previousDepartureTime = getTimeMillisFromTextView(previousRow.getDepartureTimeTextView(), 1);
				float previousDurationTime = previousArrivalTime - previousDepartureTime;

				for (int j = 0; j < totalHotels / hotelsPerScreenHeight; j++) {
					for (int i = 1; i < hotelsPerScreenHeight; i++) {
						View currentHotelRowView = mDriver.flightsSearchResultsScreen().searchResultListView()
								.getChildAt(i);
						FlightsSearchResultRow currentRow = new FlightsSearchResultRow(currentHotelRowView);
						float currentRowArrivalTime = getTimeMillisFromTextView(
								currentRow.getArrivalTimeTextView(), 1);
						float currentRowDepartureTime = getTimeMillisFromTextView(
								currentRow.getDepartureTimeTextView(), 1);
						float currentRowDurationTime = currentRowArrivalTime - currentRowDepartureTime;
						mDriver.enterLog(TAG, "DURATION " + currentRowDurationTime + " >= " + previousDurationTime);
						assertTrue(currentRowDurationTime >= previousDurationTime);
						previousArrivalTime = currentRowArrivalTime;
					}

					mDriver.scrollDown();
					hotelsPerScreenHeight = mDriver.flightsSearchResultsScreen().searchResultListView().getChildCount();
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
