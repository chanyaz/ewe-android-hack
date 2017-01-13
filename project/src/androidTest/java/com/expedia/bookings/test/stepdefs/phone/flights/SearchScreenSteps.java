package com.expedia.bookings.test.stepdefs.phone.flights;

import java.util.Map;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.newflights.FlightsScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;


public class SearchScreenSteps {

	Map<String, String> parameters;
	int totalTravelers;

	@When("^I enter source and destination for flights")
	public void enterSourceAndDestination(Map<String, String> parameters) throws Throwable {
		SearchScreen.origin().perform(click());
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(parameters.get("source")));
		SearchScreen.selectLocation(parameters.get("source_suggest"));
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(parameters.get("destination")));
		SearchScreen.selectLocation(parameters.get("destination_suggest"));
	}

	@And("^I pick dates for flights")
	public void pickDates(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		LocalDate endDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("end_date")));
		SearchScreen.selectDates(stDate, endDate);
	}

	@And("^I change travellers count and press done")
	public void changeTravellersCount() throws Throwable {
		SearchScreen.selectGuestsButton().perform(click());
		SearchScreen.searchAlertDialogDone().perform(waitForViewToDisplay());
		SearchScreen.incrementAdultsButton();
		SearchScreen.incrementAdultsButton();
		SearchScreen.incrementChildrenButton();
		SearchScreen.searchAlertDialogDone().perform(click());
	}

	@And("^I select one way trip")
	public void selectOneWayTrip() throws Throwable {
		FlightsScreen.selectOneWay();
	}

	@And("^I pick departure date for flights")
	public void selectDepartureDate(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		SearchScreen.selectDates(stDate, null);
	}

	@Then("^I can trigger flights search")
	public void searchClick() throws Throwable {
		SearchScreen.searchButton().perform(click());
	}

	@Then("^departure field exists for flights search form")
	public void checkDepartureField() throws Throwable {
		SearchScreen.origin().check(matches(isDisplayed()));
		SearchScreen.origin().check(matches(withText(containsString("Flying from"))));
	}

	@And("arrival field exists for flights search form")
	public void checkArrivalField() throws Throwable {
		SearchScreen.destination().check(matches(isDisplayed()));
		SearchScreen.destination().check(matches(withText(containsString("Flying to"))));
	}

	@Then("^calendar field exists for flights search form")
	public void checkCalendarField() throws Throwable {
		onView(withId(R.id.calendar_card)).check(matches(isDisplayed()));
		onView(withId(R.id.calendar_card)).check(matches(withText(containsString("Select Dates"))));
	}


	@Then("^calendar field exists for one way flights search form")
	public void checkCalendarFieldOneWay() throws Throwable {
		onView(withId(R.id.calendar_card)).check(matches(isDisplayed()));
		onView(withId(R.id.calendar_card)).check(matches(withText(containsString("Select departure date"))));
	}

	@And("^I make a flight search with following parameters")
	public void flightSearchCall(Map<String, String> parameters) throws Throwable {
		this.parameters = parameters;
		enterSourceAndDestination(parameters);
		pickDates(parameters);
		int adult = Integer.parseInt(parameters.get("adults"));
		int child = Integer.parseInt(parameters.get("child"));
		this.totalTravelers = adult + child;
		SearchScreen.selectGuestsButton().perform(click());
		for (int i = 1; i < adult; i++) {
			SearchScreen.incrementAdultsButton();
		}
		for (int i = 0; i < child; i++) {
			SearchScreen.incrementChildrenButton();
		}
		SearchScreen.searchAlertDialogDone().perform(click());
		SearchScreen.searchButton().perform(click());
	}

	@Then("^on FSR the destination is \"(.*?)\"$")
	public void verifyDestination(String destination) throws Throwable {
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Select flight to"))))
			.check(matches(withText(containsString(destination))));
	}

	@And("^on FSR the date is as user selected")
	public void verifyDate() throws Throwable {
		LocalDate startDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		String date = String.valueOf(startDate.getDayOfMonth());
		String year = String.valueOf(startDate.getYear());
		String month = getMonth(startDate.getMonthOfYear());
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
			.check(matches(withText(containsString(date))));
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
			.check(matches(withText(containsString(month))));
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
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

	@And("^on inbound FSR the number of traveller are as user selected")
	public void verifyTravelersForInbound() throws Throwable {
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
			.check(matches(withText(containsString(totalTravelers + " Traveler"))));
	}

	@Then("^I verify date is as user selected for inbound flight")
	public void verifyDateForInboundFlight() throws Throwable {
		LocalDate startDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("end_date")));
		String date = String.valueOf(startDate.getDayOfMonth());
		String year = String.valueOf(startDate.getYear());
		String month = getMonth(startDate.getMonthOfYear());
		onView(allOf(withParent(withId(R.id.flights_toolbar)), hasSibling(withText("Select return flight")),
			withText(containsString("Traveler")))).check(matches(withText(containsString(date))));
		onView(allOf(withParent(withId(R.id.flights_toolbar)), hasSibling(withText("Select return flight")),
			withText(containsString("Traveler")))).check(matches(withText(containsString(month))));
		onView(allOf(withParent(withId(R.id.flights_toolbar)), hasSibling(withText("Select return flight")),
			withText(containsString("Traveler")))).check(matches(withText(containsString(year))));
	}

	@Then("^I select first flight")
	public void selectFirstFlight() throws Throwable {
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0);
		FlightsScreen.selectOutboundFlight().perform(click());
	}

	@And("^on outbound FSR the number of traveller are as user selected")
	public void verifyTravelersForOutbound() throws Throwable {
		onView(allOf(withParent(withId(R.id.flights_toolbar)), hasSibling(withText("Select return flight")),
			withText(containsString("Traveler"))))
			.check(matches(withText(containsString(totalTravelers + " Traveler"))));
	}

}
