package com.expedia.bookings.test.ui.tablet.tests.flights;

import java.util.GregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.ui.tablet.pagemodels.SortFilter;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

import android.support.test.espresso.DataInteraction;

import junit.framework.AssertionFailedError;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by dmadan on 5/27/14.
 */
public class FlightSearchResultsSortTests extends TabletTestCase {

	DateTime mNow;

	// Helper methods

	public void executeAFlightSearch() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Search.clickOriginButton();
		Search.typeInOriginEditText("San Francisco, CA");
		Search.clickSuggestion("San Francisco, CA");
		Search.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(5);
		Search.clickDate(startDate, null);
		Search.clickSearchPopupDone();
		Results.swipeUpFlightList();
		mNow = DateTime.now();
	}

	private float getCleanFloatFromTextView(String str) {
		str = str.replaceAll("[^\\d.]", "");
		return Float.parseFloat(str);
	}

	private int getCleanAdditionalDay(String timeString) {
		int day = Integer.parseInt(timeString.substring(timeString.indexOf("+") + 1, timeString.indexOf("+") + 2));
		return day;
	}

	private String getCleanArrivalTime(String flightTextView) {
		return flightTextView.substring(0, flightTextView.indexOf("+") - 1);
	}

	private Pair<Integer, Integer> getHourMinutePairFromHeaderTextView(String duration) {
		String timeString = duration;
		int hour = Integer.parseInt(timeString.substring(0, timeString.indexOf('h')));
		int minutes;
		if (timeString.contains("m")) {
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
		int minutes = Integer.parseInt(timeString.substring(timeString.indexOf(':') + 1, timeString.indexOf(' ')));
		Pair<Integer, Integer> hourAndMinutes = new Pair<Integer, Integer>(hour, minutes);
		return hourAndMinutes;
	}

	private float getTimeMillisFromTextView(String str, int searchOffset, DateTime mNow) {
		Pair<Integer, Integer> hourAndMinutes = getHourMinutePairFromTimeTextView(str);
		int month = mNow.getMonthOfYear();
		int day = mNow.getDayOfMonth() + searchOffset;
		int daysInMonth = new GregorianCalendar().getActualMaximum(month);
		if (day > daysInMonth) {
			day = day - daysInMonth;
			month = month + 1;
		}
		DateTime flightTime = new DateTime(mNow.getYear(), month, day, hourAndMinutes.first, hourAndMinutes.second, 0);
		float diffInMillis = flightTime.getMillis() - mNow.getMillis();
		return diffInMillis;
	}

	public void verifySortByPrice(int totalFlights) {
		//Initialize first flight row and its associated variables
		DataInteraction previousRow = Results.flightAtIndex(1);
		String previousPriceString = EspressoUtils.getListItemValues(previousRow, R.id.price_text_view);
		float previousPrice = getCleanFloatFromTextView(previousPriceString);

		// Iterate through list and compare current values with previous values
		for (int j = 1; j < totalFlights - 2; j++) {
			DataInteraction currentRow = Results.flightAtIndex(j);
			String currentPriceString = EspressoUtils.getListItemValues(currentRow, R.id.price_text_view);
			float currentPrice = getCleanFloatFromTextView(currentPriceString);

			if (currentPrice < previousPrice) {
				throw new AssertionFailedError("Row's price was "
					+ currentPriceString
					+ ", which is less than previously listed time "
					+ previousPriceString);
			}
			previousPriceString = currentPriceString;
			previousPrice = currentPrice;
		}
	}

	public void verifySortbyArrival(int totalFlights) {
		//Initialize first flight row and its associated arrival time
		DataInteraction previousRow = Results.flightAtIndex(1);
		int additionalDaysPrevious = 0;
		String textView = EspressoUtils.getListItemValues(previousRow, R.id.flight_time_text_view);
		String previousArrivalTimeString = textView.substring(textView.indexOf("o") + 2);

		//get additional day value from flight time text
		if (previousArrivalTimeString.contains("day")) {
			additionalDaysPrevious = getCleanAdditionalDay(previousArrivalTimeString);
			//get arrival time from "flight time" text view of first flight row
			previousArrivalTimeString = getCleanArrivalTime(previousArrivalTimeString);
		}
		float previousArrivalTime = getTimeMillisFromTextView(previousArrivalTimeString, additionalDaysPrevious + 1, mNow);

		// Iterate through list and compare current arrival time with previous arrival time
		for (int j = 1; j < totalFlights - 2; j++) {
			DataInteraction currentRow = Results.flightAtIndex(j);
			int additionalDaysCurrent = 0;
			textView = EspressoUtils.getListItemValues(currentRow, R.id.flight_time_text_view);
			String currentArrivalTimeString = textView.substring(textView.indexOf("o") + 2);

			//get additional day value from flight time text in current flight result row
			if (currentArrivalTimeString.contains("day")) {
				additionalDaysCurrent = getCleanAdditionalDay(currentArrivalTimeString);
				//get arrival time from "flight time" text view
				currentArrivalTimeString = getCleanArrivalTime(currentArrivalTimeString);
			}
			float currentArrivalTime = getTimeMillisFromTextView(currentArrivalTimeString, additionalDaysCurrent + 1, mNow);

			if (currentArrivalTime < previousArrivalTime) {
				throw new AssertionFailedError("Row's arrival time was "
					+ currentArrivalTimeString
					+ ", which is earlier than previously listed time "
					+ previousArrivalTimeString);
			}
			previousArrivalTimeString = currentArrivalTimeString;
			previousArrivalTime = currentArrivalTime;
		}
	}

	public void verifySortByDeparture(int totalFlights) {

		//Initialize first flight row and its associated departure time
		DataInteraction previousRow = Results.flightAtIndex(1);
		String textView = EspressoUtils.getListItemValues(previousRow, R.id.flight_time_text_view);
		String previousDepartureTimeString = textView.substring(0, textView.indexOf("t") - 1);
		float previousDepartureTime = getTimeMillisFromTextView(previousDepartureTimeString, 1, mNow);

		// Iterate through list by section and compare current departure time with previous departure time
		for (int j = 1; j < totalFlights - 2; j++) {
			DataInteraction currentRow = Results.flightAtIndex(j);
			textView = EspressoUtils.getListItemValues(currentRow, R.id.flight_time_text_view);
			String currentDepartureTimeString = textView.substring(0, textView.indexOf("t") - 1);
			float currentDepartureTime = getTimeMillisFromTextView(currentDepartureTimeString, 1, mNow);

			if (currentDepartureTime < previousDepartureTime) {
				throw new AssertionFailedError("Row's departure time was "
					+ currentDepartureTimeString
					+ ", which is earlier than previously listed time "
					+ previousDepartureTimeString);
			}
			previousDepartureTimeString = currentDepartureTimeString;
			previousDepartureTime = currentDepartureTime;
		}
	}

	public void verifySortByDuration(int totalFlights) {

		// get first flight's duration
		Results.clickFlightAtIndex(1);
		String duration = EspressoUtils.getText(R.id.flight_overall_duration_text_view);
		Pair<Integer, Integer> previousDuration = getHourMinutePairFromHeaderTextView(duration);
		Common.pressBack();
		Pair<Integer, Integer> currentDuration;

		//iterate through list of flights, compare currently indexed flight's duration with flight at index - 1
		for (int j = 1; j < totalFlights - 2; j++) {
			Results.clickFlightAtIndex(j);
			String currentDurationString = EspressoUtils.getText(R.id.flight_overall_duration_text_view);
			currentDuration = getHourMinutePairFromHeaderTextView(currentDurationString);
			Common.pressBack();

			if (currentDuration.first < previousDuration.first
				|| (currentDuration.first == previousDuration.first
				&& currentDuration.second < previousDuration.second)) {
				throw new AssertionFailedError("Duration of: " + currentDuration.first + "h "
					+ currentDuration.second + "m is shorter than the previous duration "
					+ previousDuration.first + "h " + previousDuration.second + "m.");
			}
			previousDuration = currentDuration;
		}
	}
	// Test methods

	public void testSortByPrice() throws Throwable {
		executeAFlightSearch();
		SortFilter.clickToSortByPrice();
		int totalFlights = EspressoUtils.getListCount(Results.flightList());

		// If number of flights > 1, continue with test
		if (totalFlights > 1) {
			verifySortByPrice(totalFlights);
		}
		Common.pressBack();
		rotateScreen();
		//results are correctly sorted after rotating screen
		verifySortByPrice(totalFlights);

		//rotate it back to landscape and click on first list item.
		rotateScreen();
		Results.clickFlightAtIndex(1);
	}

	public void testSortByArrival() throws Throwable {
		executeAFlightSearch();
		SortFilter.clickToSortByArrival();
		int totalFlights = EspressoUtils.getListCount(Results.flightList());
		// If number of flights > 1, continue with test
		if (totalFlights > 1) {
			verifySortbyArrival(totalFlights);
		}
		Common.pressBack();
		rotateScreen();
		//results are correctly sorted after rotating screen
		verifySortbyArrival(totalFlights);

		//rotate it back to landscape and click on first list item.
		rotateScreen();
		Results.clickFlightAtIndex(1);
	}

	public void testSortByDeparture() throws Throwable {
		executeAFlightSearch();
		SortFilter.clickToSortByDeparture();
		int totalFlights = EspressoUtils.getListCount(Results.flightList());

		// If number of flights > 1, continue with test
		if (totalFlights > 1) {
			verifySortByDeparture(totalFlights);
		}
		Common.pressBack();
		rotateScreen();
		//results are correctly sorted after rotating screen
		verifySortByDeparture(totalFlights);

		//rotate it back to landscape and click on first list item.
		rotateScreen();
		Results.clickFlightAtIndex(1);
	}

	public void testSortByDuration() throws Throwable {
		executeAFlightSearch();
		SortFilter.clickToSortByDuration();
		int totalFlights = EspressoUtils.getListCount(Results.flightList());

		// Only run test if number of flights is > 1
		if (totalFlights > 1) {
			verifySortByDuration(totalFlights);
		}
		Common.pressBack();
		rotateScreen();
		//results are correctly sorted after rotating screen
		verifySortByDuration(totalFlights);

		//rotate it back to landscape and click on first list item.
		rotateScreen();
		Results.clickFlightAtIndex(1);
	}

	public void testAirlineFilter() throws Exception {
		executeAFlightSearch();

		//get number of airlines in flight results filter
		int numberOfAirlines = Integer.parseInt(SortFilter.getfilterAirlineView(-1));

		//go through all airlines in filter and verify the airline name in search results
		for (int i = 0; i < numberOfAirlines; i++) {
			String airlineFilterName = SortFilter.getfilterAirlineView(i);
			SortFilter.clickAirlineFilter(airlineFilterName);

			int totalFlights = EspressoUtils.getListCount(Results.flightList());

			//iterate through list of flights and verify filter airline name
			for (int j = 1; j < totalFlights - 1; j++) {
				DataInteraction currentRow = Results.flightAtIndex(j);
				String airlineName = EspressoUtils.getListItemValues(currentRow, R.id.airline_text_view);
				assertEquals(airlineFilterName, airlineName);
			}
			SortFilter.clickAirlineFilter(airlineFilterName);
		}
	}

	public void testGeneralUIExpectations() throws Exception {
		executeAFlightSearch();

		onView(allOf(withId(R.id.sticky_number_of_items), hasSibling(withId(R.id.sticky_subtitle)))).check(matches(isDisplayed()));
		//flight search results list displays the number of results

		EspressoUtils.assertViewIsDisplayed(R.id.route_description_text);
		//route description text is displayed

		EspressoUtils.assertViewIsDisplayed(R.id.departure_image);
		EspressoUtils.assertViewIsDisplayed(R.id.arrival_image);
		//origin and destination is displayed on the map

		EspressoUtils.assertViewIsDisplayed(R.id.flight_filter_control);
		//stops filter is displayed
	}
}
