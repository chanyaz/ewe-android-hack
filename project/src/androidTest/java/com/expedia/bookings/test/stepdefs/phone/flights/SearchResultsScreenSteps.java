package com.expedia.bookings.test.stepdefs.phone.flights;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.hamcrest.Matchers;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen;
import com.expedia.bookings.test.stepdefs.phone.TestUtil;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static com.expedia.bookings.test.stepdefs.phone.flights.SortSteps.getFlightTimeAtPosition;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;

public class SearchResultsScreenSteps {
	@Then("^I select outbound flight at position (\\d+) and reach inbound FSR$")
	public void selectOutboundFlightReachInboundFSR(int pos) throws Throwable {
		int position = pos - 1;
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), position);
		FlightsScreen.selectOutboundFlight().perform(click());
	}

	@Then("^I select outbound flight at position (\\d+)$")
	public void selectOutboundFlight(int pos) throws Throwable {
		int position = pos - 1;
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), position);
	}

	@And("^I select inbound flight at position (\\d+) and reach overview$")
	public void selectInboundFlight(int pos) throws Throwable {
		int position = pos - 1;
		FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), position);
		FlightsScreen.selectInboundFlight().perform(ViewActions.waitForViewToDisplay(), click());
	}

	@And("^I select inbound flight at position (\\d+)$")
	public void selectInboundFlightReachOverview(int pos) throws Throwable {
		int position = pos - 1;
		FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), position);
	}

	@And("^I wait for results to load$")
	public void waitForResultsToLoad() throws Throwable {
		onView(allOf(withId(R.id.sort_filter_button), isDescendantOfA(withId(R.id.widget_flight_outbound)))).perform(waitFor(isDisplayed(), 40, TimeUnit.SECONDS));
	}

	@And("^I wait for inbound flights results to load$")
	public void waitForInboundFlightsToLoad() throws Throwable {
		onView(allOf(withId(R.id.fsr_container), hasDescendant(withId(R.id.list_view)), hasSibling(allOf(
			withId(R.id.docked_outbound_flight_selection),
			withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))))
			.perform(waitFor(isDisplayed(), 10, TimeUnit.SECONDS));
	}

	@Then("^on FSR the destination is \"(.*?)\"$")
	public void verifyDestination(String destination) throws Throwable {
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Select flight to"))))
			.check(matches(withText(containsString(destination))));
	}

	@And("^Validate that flight search results are displayed$")
	public void validateThatFlightSearchResultsDisplayed() throws Throwable {
		onView(allOf(withId(R.id.list_view), isDescendantOfA(withId(R.id.widget_flight_outbound)))).check(matches(isDisplayed()));
	}

	@And("^Validate that flight search results are displayed for inbound flights$")
	public void validateThatFlightSearchResultsDisplayedOnInboundFSR() throws Throwable {
		onView(allOf(withId(R.id.fsr_container), hasDescendant(withId(R.id.list_view)), hasSibling(allOf(
			withId(R.id.docked_outbound_flight_selection),
			withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))))
			.check(matches(isDisplayed()));
	}

	@Then("^I click on search icon to go to search form$")
	public void clickOnSearchIcon() throws Throwable {
		waitForResultsToLoad();
		onView(withId(R.id.menu_search)).perform(click());
	}

	@And("^I click on sort and filter icon and isOutBound : (true|false)$")
	public void clickOnSortAndFilterIcon(boolean outBound) throws Throwable {
		onView(allOf(withId(R.id.sort_filter_button), (outBound ? isDescendantOfA(withId(R.id.widget_flight_outbound))
			: isDescendantOfA(withId(R.id.widget_flight_inbound))))).perform(click());
	}

	private ViewInteraction xSellPackageBanner() {
		return onView(withId(R.id.card_view_package_banner));
	}

	@Then("^Validate that XSell Package Banner is displayed with title \"(.*?)\" and description \"(.*?)\"$")
	public void validateXSellPackageDisplayed(String title, String description) throws Throwable {
		xSellPackageBanner().check(matches(isDisplayed()));
		onView(allOf(withId(R.id.package_flight_banner_title), withText(title))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.package_flight_banner_description), withText(description))).check(matches(isDisplayed()));
	}

	@And("^I tap on XSell Package Banner$")
	public void clickXSellPackageBanner() throws Throwable {
		xSellPackageBanner().perform(click());
	}

	@Then("^Validate Hotel Search Results Screen Header$")
	public void checkForBundlesScreen(Map<String, String> parameters) throws Throwable {
		onView(withId(R.id.title)).perform(waitFor(isDisplayed(), 40, TimeUnit.SECONDS));
		onView(allOf(withId(R.id.title), withText(parameters.get("title")))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.subtitle), withText(getPackageHotelSubTitle(parameters)))).check(matches(isDisplayed()));
	}

	private String getPackageHotelSubTitle(Map<String, String> parameters) {
		StringBuilder stringBuilder = new StringBuilder("");
		stringBuilder.append(TestUtil.getFormattedDateString(parameters.get("start_date"), parameters.get("end_date")));
		stringBuilder.append(", " + parameters.get("guest"));
		return stringBuilder.toString();
	}

	@Then("^Validate that XSell Package Banner is not displayed$")
	public void validateXSellPackageNotDisplayed() throws Throwable {
		xSellPackageBanner().check(doesNotExist());
	}

	@Then("^Validate flight time field at cell (\\d+) has \"(.*?)\"$")
	public void validateFlightCellElapsedDays(int cellNumber, String flightTime) {
		onView(allOf(withId(R.id.list_view), (isDescendantOfA(withId(R.id.widget_flight_outbound)))))
			.perform(RecyclerViewActions.scrollToPosition(cellNumber))
			.check(RecyclerViewAssertions.assertionOnItemAtPosition(cellNumber, hasDescendant(
				allOf(withId(R.id.flight_time_detail_text_view), withText(containsString(flightTime))))));
	}

	@Then("^Verify if any flight has elapsed time$")
	public void verifyFlightHasElapsedDays() {
		ViewInteraction recyclerViewInteraction = onView(allOf(withId(R.id.list_view), (isDescendantOfA(withId(R.id.widget_flight_outbound)))));
		assert (checkFlightTimeInFSR(recyclerViewInteraction, "[-?,+?]\\d\\d?[d]"));
	}

	@And("^I choose the flight with airline name \"(.*?)\" and isOutBound : (true|false)$")
	public void selectFlightWithAirlineName(String airlineName, boolean outBound)  throws Throwable {
		ViewInteraction recyclerViewInteraction = onView(Matchers.allOf(withId(R.id.list_view), (outBound ? isDescendantOfA(withId(R.id.widget_flight_outbound))
			: isDescendantOfA(withId(R.id.widget_flight_inbound)))));
		int index = getIndexWithName(recyclerViewInteraction, airlineName);

		if (outBound) {
			selectOutboundFlight(index);
		}
		else {
			selectInboundFlight(index);
		}
	}

	public boolean checkFlightTimeInFSR(ViewInteraction recyclerViewInteraction, String stringToMatch) {
		int items = EspressoUtils.getListCount(recyclerViewInteraction);
		for (int index = 1; index < items; index++) {
			recyclerViewInteraction.perform(RecyclerViewActions.scrollToPosition(index));
			String flightTime = getFlightTimeAtPosition(index, recyclerViewInteraction).toString();
			if (flightTime.contains(stringToMatch)) {
				return true;
			}
		}
		return false;
	}

	public int getIndexWithName(ViewInteraction recyclerViewInteraction, String stringToMatch) {
		int items = EspressoUtils.getListCount(recyclerViewInteraction);
		for (int index = 1; index < items; index++) {
			recyclerViewInteraction.perform(RecyclerViewActions.scrollToPosition(index + 1));
			String airlineName = getAirlineNameAtPosition(index, recyclerViewInteraction).toString();
			if (airlineName.contains(stringToMatch)) {
				return index;
			}
		}
		return -1;
	}

	public static AtomicReference<String> getAirlineNameAtPosition(int pos, ViewInteraction viewInteraction) {
		AtomicReference<String> airineName = new AtomicReference<>();
		viewInteraction
			.perform(ViewActions.waitForViewToDisplay(), ViewActions.getAirlineNameAtPosition(pos, airineName));
		return airineName;
	}

}
