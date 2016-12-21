package com.expedia.bookings.test.stepdefs.phone.packages;

import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;


/**
 * Created by pchaudhari on 26/10/16.
 */

public class SearchScreenSteps {

	@Then("^I enter \"(.*?)\" in flying from input box on packages search page$")
	public void entersource(String arg1) throws Throwable {
		SearchScreen.searchEditText().perform(typeText(arg1));
		SearchScreen.suggestionList().perform(ViewActions.waitForViewToDisplay());
	}

	@Then("^I select source as \"(.*?)\" from the suggestions on packages search page$")
	public void selectSourceSuggest(String sourceSearchSuggest) throws Throwable {
		SearchScreen.selectLocation(sourceSearchSuggest);
	}

	@Then("^I enter \"(.*?)\" in flying to input box on packages search page$")
	public void enterDestination(String destination) throws Throwable {
		SearchScreen.searchEditText().perform(typeText(destination));
		SearchScreen.suggestionList().perform(ViewActions.waitForViewToDisplay());
	}

	@Then("^I select destination as \"(.*?)\" from the suggestions on packages search page$")
	public void selectDestinationSearchSuggest(String destination) throws Throwable {
		SearchScreen.selectLocation(destination);
	}

	@Then("^I select dates as (\\d+) and (\\d+) on packages search page")
	public void selectPackageDates(final int startDate, final int endDate) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(startDate);
		LocalDate enDate = LocalDate.now().plusDays(endDate);
		SearchScreen.selectDatesOnly(stDate, enDate);
	}

	@Then("^I select start date as (\\d+) on packages search page")
	public void selectStartDateOnly(final int startDate) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(startDate);
		SearchScreen.selectDatesOnly(stDate, null);
	}

	@Then("^I click on calender done button")
	public void clickCalenderDoneButton() throws Throwable {
		SearchScreen.searchAlertDialogDone().perform(click());
	}

	@Then("^I select travellers")
	public void selectTravellerAndChild() throws Throwable {
		SearchScreen.selectGuestsButton().perform(click());
	}

	@Then("^I increase trvallers' count to 6")
	public void increaseTravellersCount() throws Throwable {
		for (int i = 0; i < 5; i++) {
			SearchScreen.incrementAdultsButton();
		}
		SearchScreen.searchAlertDialogDone().perform(click());
	}

	@Then("^I look for the alert message saying \"(.*?)\"$")
	public void searchForTheAlert(String message) throws Throwable {
		onView(allOf(withText(message))).inRoot(
			withDecorView(not(Matchers.is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))))
			.check(matches(isDisplayed()));
		SearchScreen.searchAlertDialogDone().perform(click());
	}

	@Then("^I click on search button on packages search page")
	public void searchForTheDeals() throws Throwable {
		SearchScreen.searchButton().perform(click());
	}

	@Then("^I validate that the number of days displayed is (\\d+) on the calender")
	public void checkNumberOfDaysSelected(final int days) throws Throwable {
		onView(allOf(withText(containsString(String.valueOf(days) + " nights")), withId(R.id.instructions)))
			.inRoot(withDecorView(
				not(Matchers.is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))))
			.check(matches(isDisplayed()));
	}

	@Then("^I validate that the results appear")
	public void waitingForPackagesHotelsResultsToAppear() throws Throwable {
		HotelScreen.hotelResultsList().perform(ViewActions.waitForViewToDisplay());
	}

}
