package com.expedia.bookings.test.tests.tablet.Flights.ui.regression;

import java.util.GregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.mobiata.android.util.SettingUtils;

import junit.framework.AssertionFailedError;

import static com.expedia.bookings.test.utils.EspressoUtils.slowSwipeUp;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;

/**
 * Created by dmadan on 5/27/14.
 */
public class FlightSearchResultsSortTests extends ActivityInstrumentationTestCase2 {

	public FlightSearchResultsSortTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = "FlightSearchResultsSortTests";

	Context mContext;
	DateTime mNow;
	int mCheck = 1;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		getActivity();
	}

	// Helper methods

	public void executeAFlightSearch() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("New York, NY");
		Launch.clickSuggestion("New York, NY");
		Results.clickOriginButton();
		Results.typeInOriginEditText("Paris, France");
		Results.clickSuggestion("Paris, France");
		Results.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(1);
		Results.clickDate(startDate, null);
		Results.clickSearchNow();
		Results.flightList().perform(slowSwipeUp());
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

	private float getTimeMillisFromTextView(String str, int searchOffset) {
		Pair<Integer, Integer> hourAndMinutes = getHourMinutePairFromTimeTextView(str);
		if (mCheck == 1) {
			mNow = DateTime.now();
			mCheck = 0;
		}
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

	// Test methods

	public void testSortByPrice() throws Exception {
		executeAFlightSearch();
		Results.clickToSortByPrice();
		int totalFlights = EspressoUtils.getListCount(Results.flightList());

		// If number of flights > 1, continue with test
		if (totalFlights > 1) {

			//Initialize first flight row and its associated variables
			DataInteraction previousRow = Results.flightAtIndex(1);
			String previousPriceString = EspressoUtils.getListItemValues(previousRow, R.id.price_text_view);
			float previousPrice = getCleanFloatFromTextView(previousPriceString);

			// Iterate through list and compare current values with previous values
			for (int j = 1; j < totalFlights - 1; j++) {
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
		pressBack();
		pressBack();
	}

	public void testSortByArrival() throws Exception {
		executeAFlightSearch();
		Results.clickToSortByArrival();
		int totalFlights = EspressoUtils.getListCount(Results.flightList());
		// If number of flights > 1, continue with test
		if (totalFlights > 1) {
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
			float previousArrivalTime = getTimeMillisFromTextView(previousArrivalTimeString, additionalDaysPrevious + 1);

			// Iterate through list and compare current arrival time with previous arrival time
			for (int j = 1; j < totalFlights - 1; j++) {
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
				float currentArrivalTime = getTimeMillisFromTextView(currentArrivalTimeString, additionalDaysCurrent + 1);

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
		pressBack();
		pressBack();
	}

	public void testSortByDeparture() throws Exception {
		executeAFlightSearch();
		Results.clickToSortByDeparture();
		int totalFlights = EspressoUtils.getListCount(Results.flightList());

		// If number of flights > 1, continue with test
		if (totalFlights > 1) {
			//Initialize first flight row and its associated departure time
			DataInteraction previousRow = Results.flightAtIndex(1);
			String textView = EspressoUtils.getListItemValues(previousRow, R.id.flight_time_text_view);
			String previousDepartureTimeString = textView.substring(0, textView.indexOf("t") - 1);
			float previousDepartureTime = getTimeMillisFromTextView(previousDepartureTimeString, 1);

			// Iterate through list by section and compare current departure time with previous departure time
			for (int j = 1; j < totalFlights - 1; j++) {
				DataInteraction currentRow = Results.flightAtIndex(j);
				textView = EspressoUtils.getListItemValues(currentRow, R.id.flight_time_text_view);
				String currentDepartureTimeString = textView.substring(0, textView.indexOf("t") - 1);
				float currentDepartureTime = getTimeMillisFromTextView(currentDepartureTimeString, 1);

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
		pressBack();
		pressBack();
	}

	public void testSortByDuration() throws Exception {
		executeAFlightSearch();
		Results.clickToSortByDuration();
		int totalFlights = EspressoUtils.getListCount(Results.flightList());

		// Only run test if number of flights is > 1
		if (totalFlights > 1) {
			// get first flight's duration
			Results.clickFlightAtIndex(1);
			String duration = EspressoUtils.getText(R.id.flight_overall_duration_text_view);
			Pair<Integer, Integer> previousDuration = getHourMinutePairFromHeaderTextView(duration);
			pressBack();
			Pair<Integer, Integer> currentDuration;

			//iterate through list of flights, compare currently indexed flight's duration with flight at index - 1
			for (int j = 1; j < totalFlights - 1; j++) {
				Results.clickFlightAtIndex(j);
				String currentDurationString = EspressoUtils.getText(R.id.flight_overall_duration_text_view);
				currentDuration = getHourMinutePairFromHeaderTextView(currentDurationString);
				pressBack();

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
		pressBack();
		pressBack();
	}

	public void testAirlineFilter() throws Exception {
		executeAFlightSearch();

		//get number of airlines in flight results filter
		int numberOfAirlines = Integer.parseInt(Results.getfilterAirlineView(-1));

		//go through all airlines in filter and verify the airline name in search results
		for (int i = 0; i < numberOfAirlines; i++) {
			String airlineFilterName = Results.getfilterAirlineView(i);
			Results.clickAirlineFilter(airlineFilterName);

			int totalFlights = EspressoUtils.getListCount(Results.flightList());

			//iterate through list of flights and verify filter airline name
			for (int j = 1; j < totalFlights - 1; j++) {
				DataInteraction currentRow = Results.flightAtIndex(j);
				String airlineName = EspressoUtils.getListItemValues(currentRow, R.id.airline_text_view);
				assertEquals(airlineFilterName, airlineName);
				ScreenActions.enterLog(TAG, "Airline name in Flight filters: " + airlineFilterName + " is same as: " + airlineName + " in the Flight results");
			}
			Results.clickAirlineFilter(airlineFilterName);
		}
		pressBack();
		pressBack();
	}
}
