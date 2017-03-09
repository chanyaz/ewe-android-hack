package com.expedia.bookings.test.stepdefs.phone.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.newflights.FlightsScreen;

import org.joda.time.LocalDate;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

public class FlightDetailsScreenSteps {


	@And("^I click on the flight with airline name \"(.*?)\" at \"(.*?)\"$")
	public void clickOnFlightWithAirlineNameAndTime(String airlineName, String airlineTime)
		throws Throwable {
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), airlineName, airlineTime);
		//FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(),0);
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
			allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
			.check(matches(withText(containsString(count + " Traveler"))));
	}

	@And("^on Flight detail check the date is as user selected$")
	public void validateDateOnDetails() throws Throwable {
		LocalDate startDate = LocalDate.now()
			.plusDays(Integer.parseInt(TestUtilFlights.dataSet.get("start_date")));
		String date = String.valueOf(startDate.getDayOfMonth());
		String year = String.valueOf(startDate.getYear());
		String month = getMonth(startDate.getMonthOfYear());
		onView(
			allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
			.check(matches(withText(containsString(date))));
		onView(
			allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
			.check(matches(withText(containsString(month))));
		onView(
			allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
			.check(matches(withText(containsString(year))));
	}

	public static String getMonth(int month) {
		switch (month) {
		case 1:
			return "Jan";
		case 2:
			return "Feb";
		case 3:
			return "Mar";
		case 4:
			return "Apr";
		case 5:
			return "May";
		case 6:
			return "Jun";
		case 7:
			return "Jul";
		case 8:
			return "Aug";
		case 9:
			return "Sep";
		case 10:
			return "Oct";
		case 11:
			return "Nov";
		default:
			return "Dec";
		}

	}


	@And("^price displayed on flight details is \"([^\"]*)\"$")
	public void verifyPriceOnOverview(String price) throws Throwable {
		onView(withId(R.id.flight_overview_urgency_messaging))
			.check(matches(withText(containsString(price))));

	}

	@And("^flight time on the flight details is \"([^\"]*)\"$")
	public void verifyFlightDepartureArrivalTime(String time) throws Throwable {
		onView(allOf(withParent(withParent(withParent(withId(R.id.breakdown_container)))),
			withId(R.id.departure_arrival_time)))
			.check(matches(withText(time)));
	}

	@And("^airport names on the flight details is \"([^\"]*)\"$")
	public void verifyDepartureArrivalAirportName(String airport) throws Throwable {
		onView(allOf(withParent(withParent(withParent(withId(R.id.breakdown_container)))),
			withId(R.id.departure_arrival_airport)))
			.check(matches(withText(airport)));
	}

	@And("^airline name on the flight details is \"([^\"]*)\"$")
	public void verifyAirlineName(String airline) throws Throwable {
		onView(allOf(withParent(withParent(withParent(withId(R.id.breakdown_container)))),
			withId(R.id.airline_airplane_type)))
			.check(matches(withText(containsString(airline))));
	}

	@And("^flight duration on the flight details is \"([^\"]*)\"$")
	public void verifyFlightDuration(String duration) throws Throwable {
		onView(allOf(withParent(withParent(withParent(withId(R.id.breakdown_container)))),
			withId(R.id.flight_duration)))
			.check(matches(withText(duration)));
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


}
