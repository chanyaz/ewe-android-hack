package com.expedia.bookings.test.ui.phone.tests.flights;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import android.support.test.espresso.DataInteraction;

import junit.framework.AssertionFailedError;

import static android.support.test.espresso.Espresso.pressBack;

/**
 * Created by dmadan on 5/6/14.
 */
public class FlightSearchResultsSortTest extends PhoneTestCase {

	private static final String TAG = "FlightSearchResultsTest";

	DateTime mNow;

	// Helper methods

	private void executeAFlightSearch() throws Exception {
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.enterArrivalAirport("LAX");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate mStartDate = LocalDate.now().plusDays(1);
		FlightsSearchScreen.clickDate(mStartDate);
		FlightsSearchScreen.clickSearchButton();
		mNow = DateTime.now();
	}

	private float getCleanFloatFromTextView(String str) {
		str = str.replaceAll("[^\\d.]", "");
		return Float.parseFloat(str);
	}

	private Pair<Integer, Integer> getHourMinutePairFromHeaderTextView(String duration) {
		String timeString = duration;
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

	private Pair<Integer, Integer> getHourMinutePairFromTimeTextView(String str) {
		String timeString = str;
		int hour = Integer.parseInt(timeString.substring(0, timeString.indexOf(':')));
		if (timeString.contains("PM") && hour < 12) {
			hour += 12;
		}
		if (timeString.contains("AM") && hour == 12) {
			hour = 0;
		}
		int minutes = Integer.parseInt(timeString.substring(timeString.indexOf(':') + 1, timeString.lastIndexOf(' ')));
		Pair<Integer, Integer> hourAndMinutes = new Pair<Integer, Integer>(hour, minutes);
		return hourAndMinutes;
	}

	private float getTimeMillisFromTextView(String str, int searchOffset, DateTime now) {
		Pair<Integer, Integer> hourAndMinutes = getHourMinutePairFromTimeTextView(str);
		int month = now.getMonthOfYear();
		int day = now.getDayOfMonth() + searchOffset;
		int daysInMonth = new GregorianCalendar().getActualMaximum(Calendar.DAY_OF_MONTH);
		if (day > daysInMonth) {
			day = day - daysInMonth;
			month = month + 1;
		}
		DateTime flightTime = new DateTime(now.getYear(), month, day, hourAndMinutes.first, hourAndMinutes.second, 0);
		float diffInMillis = flightTime.getMillis() - now.getMillis();
		return diffInMillis;
	}

	// Test methods

	public void testSortByPrice() throws Exception {
		executeAFlightSearch();
		FlightsSearchResultsScreen.clickSortFlightsButton();
		FlightsSearchResultsScreen.clickToSortByPrice();

		int totalFlights = EspressoUtils.getListCount(FlightsSearchResultsScreen.searchResultListView());

		// If number of flights > 1, continue with test
		if (totalFlights > 1) {
			//Initialize first flight row and its associated variables
			DataInteraction previousRow = FlightsSearchResultsScreen.listItem().atPosition(2);
			String resultsPriceStringP = EspressoUtils.getListItemValues(previousRow, R.id.price_text_view);
			float previousRowPrice = getCleanFloatFromTextView(resultsPriceStringP);

			// Iterate through list by section, current values with previous values
			for (int j = 2; j < totalFlights - 2; j++) {
				DataInteraction currentFlightRowView = FlightsSearchResultsScreen.listItem().atPosition(j);
				String resultsPriceStringC = EspressoUtils.getListItemValues(currentFlightRowView, R.id.price_text_view);

				float currentRowPrice = getCleanFloatFromTextView(resultsPriceStringC);
				if (currentRowPrice < previousRowPrice) {
					throw new AssertionFailedError("Row's price was "
						+ resultsPriceStringC
						+ ", which is earlier than previously listed time "
						+ resultsPriceStringP);
				}
				previousRowPrice = currentRowPrice;
			}
		}
	}

	public void testSortByArrival() throws Exception {
		executeAFlightSearch();
		FlightsSearchResultsScreen.clickSortFlightsButton();
		FlightsSearchResultsScreen.clickToSortByArrival();

		int totalFlights = EspressoUtils.getListCount(FlightsSearchResultsScreen.searchResultListView());

		// If number of flights > 1, continue with test
		if (totalFlights > 1) {
			//Initialize first flight row and its associated variables
			DataInteraction previousRow = FlightsSearchResultsScreen.listItem().atPosition(2);
			int additionalDaysPreviousRow = 0;
			String multiDayTextView = EspressoUtils.getListItemValues(previousRow, R.id.multi_day_text_view);
			if (!multiDayTextView.equals("")) {
				additionalDaysPreviousRow = (int) getCleanFloatFromTextView(multiDayTextView);
			}

			String previousArrivalTime = EspressoUtils.getListItemValues(previousRow, R.id.arrival_time_text_view);
			float previousRowArrivalTime = getTimeMillisFromTextView(previousArrivalTime, additionalDaysPreviousRow + 1, mNow);

			// Iterate through list by section, current values with previous values
			for (int j = 2; j < totalFlights - 2; j++) {
				DataInteraction currentFlightRowView = FlightsSearchResultsScreen.listItem().atPosition(j);
				int additionalDaysCurrentRow = 0;
				String currentMultiDayTextView = EspressoUtils.getListItemValues(currentFlightRowView, R.id.multi_day_text_view);
				if (!currentMultiDayTextView.equals("")) {
					additionalDaysCurrentRow = (int) getCleanFloatFromTextView(currentMultiDayTextView);
				}
				String currentArrivalTime = EspressoUtils.getListItemValues(currentFlightRowView, R.id.arrival_time_text_view);
				float currentRowArrivalTime = getTimeMillisFromTextView(currentArrivalTime, additionalDaysCurrentRow + 1, mNow);
				ScreenActions.enterLog(TAG, "current time " + currentArrivalTime);
				if (currentRowArrivalTime < previousRowArrivalTime) {
					throw new AssertionFailedError("Row's arrival time was "
						+ currentArrivalTime
						+ ", which is earlier than previously listed time "
						+ previousArrivalTime);
				}
				previousRowArrivalTime = currentRowArrivalTime;
				previousArrivalTime = currentArrivalTime;
			}
		}
	}

	public void testSortByDeparture() throws Exception {
		executeAFlightSearch();
		FlightsSearchResultsScreen.clickSortFlightsButton();
		FlightsSearchResultsScreen.clickToSortByDeparture();

		int totalFlights = EspressoUtils.getListCount(FlightsSearchResultsScreen.searchResultListView());
		// If number of flights > 1, continue with test
		if (totalFlights > 1) {
			//Initialize first flight row and its associated variables
			DataInteraction previousRow = FlightsSearchResultsScreen.listItem().atPosition(1);
			String previousDepartureTimeString = EspressoUtils.getListItemValues(previousRow, R.id.departure_time_text_view);
			float previousDepartureTime = getTimeMillisFromTextView(previousDepartureTimeString, 1, mNow);

			// Iterate through list by section, current values with previous values
			for (int j = 1; j < totalFlights - 2; j++) {
				DataInteraction currentFlightRowView = FlightsSearchResultsScreen.listItem().atPosition(j);
				String currentDepartureTimeString = EspressoUtils.getListItemValues(currentFlightRowView, R.id.departure_time_text_view);
				float currentRowDepartureTime = getTimeMillisFromTextView(currentDepartureTimeString, 1, mNow);

				if (currentRowDepartureTime < previousDepartureTime) {
					throw new AssertionFailedError("Row's departure time was "
						+ currentDepartureTimeString
						+ ", which is earlier than previously listed time "
						+ previousDepartureTimeString);
				}
				previousDepartureTime = currentRowDepartureTime;
			}
		}
	}


	public void testSortByDuration() throws Exception {
		executeAFlightSearch();
		FlightsSearchResultsScreen.clickSortFlightsButton();
		FlightsSearchResultsScreen.clickToSortByDuration();

		int totalFlights = EspressoUtils.getListCount(FlightsSearchResultsScreen.searchResultListView());

		// Only run test if number of flights is > 1
		if (totalFlights > 1) {
			// get first flight's duration
			DataInteraction previousRow = FlightsSearchResultsScreen.listItem().atPosition(1);
			FlightsSearchResultsScreen.clickListItem(1);
			String duration = EspressoUtils.getText(R.id.left_text_view);
			Pair<Integer, Integer> previousDuration = getHourMinutePairFromHeaderTextView(duration);
			pressBack();
			Pair<Integer, Integer> currentDuration;

			//iterate through list of flights, comparing currently indexed flight's duration with flight at index - 1
			for (int j = 1; j < totalFlights - 2; j++) {
				DataInteraction currentRow = FlightsSearchResultsScreen.listItem().atPosition(j);
				FlightsSearchResultsScreen.clickListItem(j);
				String currentDurationString = EspressoUtils.getText(R.id.left_text_view);
				currentDuration = getHourMinutePairFromHeaderTextView(currentDurationString);
				pressBack();
				if (currentDuration.first < previousDuration.first
					|| (currentDuration.first == previousDuration.first
					&& currentDuration.second < previousDuration.second)) {
					throw new AssertionFailedError("Duration of: " + currentDuration.first + "h "
						+ currentDuration.second + "m was not shorter than the previous duration "
						+ previousDuration.first + "h " + previousDuration.second + "m.");
				}
				previousDuration = currentDuration;
			}
		}
	}
}




