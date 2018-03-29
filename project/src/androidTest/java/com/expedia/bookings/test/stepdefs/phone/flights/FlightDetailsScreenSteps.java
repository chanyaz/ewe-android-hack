package com.expedia.bookings.test.stepdefs.phone.flights;

import org.hamcrest.core.AllOf;
import org.joda.time.LocalDate;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen;
import com.expedia.bookings.test.stepdefs.phone.TestUtil;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static android.support.test.espresso.action.ViewActions.click;

public class FlightDetailsScreenSteps {


	@And("^I click on the flight with airline name \"(.*?)\" at \"(.*?)\"$")
	public void clickOnFlightWithAirlineNameAndTime(String airlineName, String airlineTime)
		throws Throwable {
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), airlineName, airlineTime);
	}

	@Then("^on flight details screen the destination is \"([^\"]*)\"$")
	public void destinationIs(String destination) throws Throwable {
		onView(
			allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Flight to"))))
			.check(matches(withText(containsString(destination))));

	}

	@And("^on flight details the traveler count is (\\d+)$")
	public void validateTravelerCountOnDetails(int count) throws Throwable {
		onView(
			allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("travelers"))))
			.check(matches(withText(containsString(count + " travelers"))));
	}

	@And("^on Flight detail check the date is as user selected$")
	public void validateDateOnDetails() throws Throwable {

		LocalDate startDate = TestUtil.getDateFromOffset(
			Integer.parseInt(TestUtil.dataSet.get("start_date")));
		String date = TestUtil.getDayFromDate(startDate);
		String year = TestUtil.getYearFromDate(startDate);
		String month = TestUtil.getMonthFromDate(startDate);
		onView(
			allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("traveler"))))
			.check(matches(withText(containsString(date))));
		onView(
			allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("traveler"))))
			.check(matches(withText(containsString(month))));
		onView(
			allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("traveler"))))
			.check(matches(withText(containsString(year))));
	}

	@And("^price displayed on flight details is \"([^\"]*)\"$")
	public void verifyPriceOnOverview(String price) throws Throwable {
		onView(allOf(withId(R.id.flight_overview_urgency_messaging), isDescendantOfA(withId(R.id.widget_flight_outbound))))
			.check(matches(withText(containsString(price))));

	}

	@And("^flight time on the flight details is \"([^\"]*)\"$")
	public void verifyFlightDepartureArrivalTime(String time) throws Throwable {
		onView(allOf(withParent(withParent(withParent(withParent(withId(R.id.breakdown_container))))),
			withId(R.id.departure_arrival_time), withText(time)))
			.check(matches(isDisplayed()));
	}

	@And("^flight time for segment (\\d+) on the flight details is \"([^\"]*)\"$")
	public void verifyFlightDepartureArrivalTimeForMultileg(int seg, String time) throws Throwable {
		onView(allOf(withParent(withParent(withParent(withParent(withId(R.id.breakdown_container))))),
			withId(R.id.departure_arrival_time), withText(time)))
			.check(matches(isDisplayed()));
	}

	@And("^airport names on the flight details is \"([^\"]*)\"$")
	public void verifyDepartureArrivalAirportName(String airport) throws Throwable {
		onView(allOf(withParent(withParent(withParent(withId(R.id.breakdown_container)))),
		withId(R.id.departure_arrival_airport), withText(airport)))
		.check(matches(isDisplayed()));


	}

	@And("^airline name on the flight details is \"([^\"]*)\"$")
	public void verifyAirlineName(String airline) throws Throwable {
	onView(allOf(withParent(withParent(withParent(withId(R.id.breakdown_container)))),
		withId(R.id.airline_airplane_type), withText(containsString(airline))))
		.check(matches(isDisplayed()));

	}

	@And("^flight duration on the flight details is \"([^\"]*)\"$")
	public void verifyFlightDuration(String duration) throws Throwable {
		onView(allOf(withParent(withParent(withParent(withId(R.id.breakdown_container)))),
			withId(R.id.flight_duration), withText(duration)))
			.check(matches(isDisplayed()));
	}

	@And("^flight class info is \"([^\"]*)\"$")
	public void verifyFlightClassInfo(String classInfo) throws Throwable {
		onView(allOf(withParent(withParent(withParent(withId(R.id.breakdown_container)))),
			withId(R.id.flight_seat_class_booking_code), withText(classInfo)))
			.check(matches(isDisplayed()));


	}

	@And("^flight total duration on the flight details is \"([^\"]*)\"$")
	public void verifyFlightTotalDuration(String totalDuration) throws Throwable {
		onView(withId(R.id.flight_total_duration))
			.check(matches(withText(containsString(totalDuration))));
	}

	@And("^Baggage link \"([^\"]*)\" is present on the flight details$")
	public void verifyBaggageLink(String baggageText) throws Throwable {
		onView(withId(R.id.show_baggage_fees))
			.check(matches(withText(containsString(baggageText))));
	}

	@And("^Select button \"([^\"]*)\" is displayed at the bottom of the flight details screen$")
	public void verifySelectButtonAtBottom(String button) throws Throwable {
		onView(withId(R.id.select_flight_button)).check(matches(withText(containsString(button))));
	}

	@And("^flight layover airport is \"([^\"]*)\"$")
	public void verifyFlightLayoverAirport(String layoverAirport) throws Throwable {
		onView(allOf(withParent(withParent(withParent(withId(R.id.breakdown_container)))),
			withId(R.id.flight_segment_layover_in)))
			.check(matches(withText(layoverAirport)));
	}

	@And("^flight layover is for \"([^\"]*)\"$")
	public void verifyLayoverDuration(String layoverDuration) throws Throwable {
		onView(allOf(withParent(withParent(withParent(withId(R.id.breakdown_container)))),
			withId(R.id.flight_segment_layover_duration)))
			.check(matches(withText(layoverDuration)));
	}

	@Then("^on flight details screen the urgency text is \"([^\"]*)\"$")
	public void verifyFlightDetailsUrgencyText(String urgencyText) throws Throwable {
		onView(allOf(withId(R.id.flight_overview_urgency_messaging), withText(containsString(urgencyText))))
			.check(matches(isDisplayed()));

	}

	@Then("^basic economy tooltip with text \"([^\"]*)\" isDisplayed : (true|false) and isOutbound : (true|false)$")
	public void verifyBasicEconomy(String text, boolean isDisplayed, boolean isOutbound) throws Throwable {
		onView(allOf(withId(R.id.flight_basic_economy_tooltip),
			(isOutbound ? isDescendantOfA(withId(R.id.widget_flight_outbound)) : isDescendantOfA(withId(R.id.widget_flight_inbound)))))
			.check(matches(allOf(withText(text),
				isDisplayed ? isDisplayed() : not(isDisplayed()))));
	}

	@Then("^Validate info of Basic Economy Dialog is \"([^\"]*)\"$")
	public void verifyInfoOfBasicEconomyDialog(String info) throws Throwable {
		validateInfo(info);
	}

	@Then("^Validate title info of Basic Economy Dialog is \"([^\"]*)\"$")
	public void verifyTitleInfoOfBasicEconomyDialog(String title) throws Throwable {
		validateTitle(title);
	}

	@And("^I click on the Basic Economy link isOutbound : (true|false)")
	public void clickOnBasicEconomyLink(boolean isOutbound) throws Throwable {
		onView(allOf(withId(R.id.flight_basic_economy_tooltip),
			(isOutbound ? isDescendantOfA(withId(R.id.widget_flight_outbound)) : isDescendantOfA(withId(R.id.widget_flight_inbound)))))
			.perform(click());
	}

	private void validateTitle(String title) throws Throwable {
		onView(withText(title)).check(matches(
				isDisplayed()));
	}

	private void validateInfo(String info) throws Throwable {
		onView(AllOf.allOf(isDescendantOfA(withId(R.id.basic_economy_tooltip_info_container)), withId(R.id.basic_economy_rules_tv),
			withText(info), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
			.check(matches(AllOf.allOf(withId(R.id.basic_economy_rules_tv),
				withText(info), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
			)));
	}
}
