package com.expedia.bookings.test.stepdefs.phone.hotel;

import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import org.joda.time.LocalDate;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;

public class SearchScreenSteps {
	@Then("^I enter destination as \"(.*?)\"$")
	public void enterDestination(String arg1) throws Throwable {
		SearchScreen.searchEditText().perform(typeText(arg1));
	}


	@Then("^I select (\\d+) , (\\d+) as check in and checkout date$")
	public void selectDates(final int startDate, final int enDate) throws Throwable {
		LocalDate strtDate = LocalDate.now().plusDays(startDate);
		LocalDate endDate = LocalDate.now().plusDays(enDate);
		SearchScreen.selectDates(strtDate, endDate);
	}

	@Then("^I select \"(.*?)\" as search suggestion$")
	public void selectSearchSuggestion(String arg1) throws Throwable {
		SearchScreen.selectLocation(arg1);
	}

	@Given("^I click on Search Button$")
	public void iCLickOnSearchButton() throws Throwable {
		SearchScreen.searchButton().perform(click());
	}
}

