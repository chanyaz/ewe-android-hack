package com.expedia.bookings.test.stepdefs.phone.flights;


import java.text.ParseException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen;
import com.expedia.bookings.test.stepdefs.phone.TestUtil;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;

public class SortSteps {

	@Then("^flight results are sorted by \"([^\"]*)\"$")
	public void flightResultsSortedBy(String sortCriteria) throws Throwable {
		switch (sortCriteria.toLowerCase(Locale.getDefault())) {
		case "price":
			assertResultsSortedByPrice(FlightsScreen.outboundFlightList());
			break;
		case "departure time":
			assertResultsSortedByDepartureTime(FlightsScreen.outboundFlightList());
			break;
		case "arrival time":
			assertResultsSortedByArrivalTime(FlightsScreen.outboundFlightList());
			break;
		case "duration":
			assertResultsSortedByDuration(FlightsScreen.outboundFlightList());
			break;
		}
	}

	@Then("^\"([^\"]*)\" sorting is shown as selected$")
	public void filterSelectedIs(String criteria) throws Throwable {
		if (criteria.equalsIgnoreCase("price")) {
			onView(withId(R.id.sort_by_selection_spinner)).check(matches(withSpinnerText(containsString("Price"))));
		}
	}

	@And("^save the sort and filter selection$")
	public void pressDoneOnFilterScreen() throws Throwable {
		onView(withText("Done")).perform(click());
	}

	@And("^I sort results by \"([^\"]*)\"$")
	public void selectSort(String criteria) throws Throwable {
		onView(withId(R.id.sort_by_selection_spinner)).perform(ViewActions.waitForViewToDisplay(), click());

		switch (criteria.toLowerCase(Locale.getDefault())) {
		case "price":
			onView(withText("Price")).perform(ViewActions.waitForViewToDisplay(), click());
			break;
		case "departure time":
			onView(withText("Departure Time")).perform(ViewActions.waitForViewToDisplay(), click());
			break;
		case "arrival time":
			onView(withText("Arrival Time")).perform(ViewActions.waitForViewToDisplay(), click());
			break;
		case "duration":
			onView(withText("Duration")).perform(ViewActions.waitForViewToDisplay(), click());

		}
	}

	public void assertResultsSortedByPrice(ViewInteraction viewInteraction) {
		int items = EspressoUtils.getListCount(viewInteraction);
		Pattern p = Pattern.compile("(\\d+)");
		for (int i = 1; i < items - 1; i++) {
			viewInteraction.perform(RecyclerViewActions.scrollToPosition(i + 1));
			Matcher priceMatcherCurrentPosition = p.matcher(getPriceAtPosition(i, viewInteraction).toString());
			Matcher priceMatcherNextPosition = p.matcher(getPriceAtPosition(i + 1, viewInteraction).toString());
			if (priceMatcherCurrentPosition.find() && priceMatcherNextPosition.find()) {
				assertTrue(new Integer(priceMatcherCurrentPosition.group(1)) <= new Integer(priceMatcherNextPosition.group(1)));
			}
			else {
				assertTrue("Price mismatch", false);
			}
		}
	}


	public void assertResultsSortedByDepartureTime(ViewInteraction viewInteraction) throws ParseException {
		int items = EspressoUtils.getListCount(viewInteraction);
		for (int i = 1; i < items - 1; i++) {
			viewInteraction.perform(RecyclerViewActions.scrollToPosition(i + 1));
			int currentPositionTime = TestUtil.convertFlightDepartureTimeToInteger(
				getFlightTimeAtPosition(i, viewInteraction).toString().split(" - ")[0]);

			int nextPositionTime = TestUtil.convertFlightDepartureTimeToInteger(
				getFlightTimeAtPosition(i + 1, viewInteraction).toString().split(" - ")[0]);

			assertTrue("Current position time: " + currentPositionTime + " next position Time: " + nextPositionTime,
				currentPositionTime <= nextPositionTime);
		}

	}

	public void assertResultsSortedByArrivalTime(ViewInteraction viewInteraction) throws ParseException {
		int items = EspressoUtils.getListCount(viewInteraction);
		for (int i = 1; i < items - 1; i++) {
			viewInteraction.perform(RecyclerViewActions.scrollToPosition(i + 1));
			int currentPositionTime = TestUtil.convertArrivalTimeToInteger(
				getFlightTimeAtPosition(i, viewInteraction).toString().split(" - ")[1]);

			int nextPositionTime = TestUtil.convertArrivalTimeToInteger(
				getFlightTimeAtPosition(i + 1, viewInteraction).toString().split(" - ")[1]);

			assertTrue("Current position time: " + currentPositionTime + " next position Time: " + nextPositionTime,
				currentPositionTime <= nextPositionTime);
		}

	}

	public void assertResultsSortedByDuration(ViewInteraction viewInteraction) {
		int items = EspressoUtils.getListCount(viewInteraction);

		for (int i = 1; i < items - 1; i++) {
			viewInteraction.perform(RecyclerViewActions.scrollToPosition(i + 1));
			int currentPositionDuration = TestUtil
				.convertDurationToInteger(getDurationAtPosition(i, viewInteraction).toString());
			int nextPositionDuration = TestUtil
				.convertDurationToInteger(getDurationAtPosition(i + 1, viewInteraction).toString());

			assertTrue("Current position duration: " + currentPositionDuration + " next position duration: " + nextPositionDuration,
				currentPositionDuration <= nextPositionDuration);
		}
	}

	public AtomicReference<String> getPriceAtPosition(int pos, ViewInteraction viewInteraction) {
		AtomicReference<String> price = new AtomicReference<>();
		viewInteraction
			.perform(ViewActions.getFlightPriceAtPosition(pos, price));

		return price;
	}

	public static AtomicReference<String> getFlightTimeAtPosition(int pos, ViewInteraction viewInteraction) {
		AtomicReference<String> time = new AtomicReference<>();
		viewInteraction
			.perform(ViewActions.waitForViewToDisplay(), ViewActions.getFlightTimeAtPosition(pos, time));

		return time;
	}

	public AtomicReference<String> getDurationAtPosition(int pos, ViewInteraction viewInteraction) {
		AtomicReference<String> duration = new AtomicReference<>();
		viewInteraction.perform(ViewActions.waitForViewToDisplay(), ViewActions.getFlightDurationAtPosition(pos, duration));

		return duration;
	}
}
