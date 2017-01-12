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
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.containsString;


public class SearchScreenSteps {

	@When("^I enter source and destination for flights")
	public void enterSourceAndDestination(Map<String, String> parameters) throws Throwable {
		SearchScreen.origin().perform(click());
		SearchScreen.searchEditText().perform(waitForViewToDisplay(),typeText(parameters.get("source")));
		SearchScreen.selectLocation(parameters.get("source_suggest"));
		SearchScreen.searchEditText().perform(waitForViewToDisplay(),typeText(parameters.get("destination")));
		SearchScreen.selectLocation(parameters.get("destination_suggest"));
	}

	@And("^I pick dates for flights")
	public void pickDates(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		LocalDate endDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("end_date")));
		SearchScreen.selectDates(stDate,endDate);
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
		SearchScreen.selectDates(stDate,null);
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
}
