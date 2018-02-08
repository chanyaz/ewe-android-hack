package com.expedia.bookings.test.stepdefs.phone.hotel;

import java.util.Map;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;

import org.joda.time.LocalDate;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;

public class SearchScreenSteps {
	@When("^I search for \"(.*?)\" and select the item with the magnifying glass$")
	public void searchAndSelectItemWithMagnifyingGlass(String location) throws Throwable {
		enterDestination(location);
		SearchScreen.selectItemWithMagnifyingGlass();
	}

	@When("^I search for hotels and choose a specific location$")
	public void searchAndSelectSpecificLocation(Map<String, String> searchOpts) throws Throwable {
		enterDestination(searchOpts.get("location"));
		SearchScreen.selectSpecificLocationWithText(searchOpts.get("suggestion"));
	}

	@When("^I search for hotels with following criteria$")
	public void consolidatedSearch(Map<String, String> searchOpts) throws Throwable {
		enterDestination(searchOpts.get("location"));
		selectSearchSuggestion(searchOpts.get("suggestion"));
	}

	@Then("^I enter destination as \"(.*?)\"$")
	public void enterDestination(String arg1) throws Throwable {
		Common.delay(1);
		SearchScreen.searchEditText().perform(typeText(arg1));
	}

	@Then("^I select hotel with the text \"(.*?)\"$")
	public void selectHotelWithText(String text) throws Throwable {
		SearchScreen.selectHotelWithText(text);
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
	public void clickOnSearchButton() throws Throwable {
		SearchScreen.searchButton().perform(click());
	}

	@Given("^I click on \"(.*?)\" within 'Did You Mean...' popup$")
	public void clickOnDidYouMeanPopupSuggestion(String suggestionOption) throws Throwable {
		SearchScreen.didYouMeanAlertSuggestion(suggestionOption).perform(click());
	}
}

