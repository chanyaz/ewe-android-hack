package com.expedia.bookings.test.stepdefs.phone.hotel;

import java.util.Map;

import android.content.res.Resources;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;

import org.joda.time.LocalDate;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static android.support.test.espresso.action.ViewActions.click;

public class SearchScreenSteps {
	@When("^I search for \"(.*?)\" and select the item with the magnifying glass$")
	public void searchAndSelectItemWithMagnifyingGlass(String location) throws Throwable {
		enterDestination(location);
		SearchScreenActions.selectItemWithMagnifyingGlass();
	}

	@When("^I search for hotels and choose a specific location$")
	public void searchAndSelectSpecificLocation(Map<String, String> searchOpts) throws Throwable {
		enterDestination(searchOpts.get("location"));
		SearchScreenActions.selectLocationIconWithSiblingText(searchOpts.get("suggestion"));
	}

	@When("^I search for hotels with following criteria$")
	public void consolidatedSearch(Map<String, String> searchOpts) throws Throwable {
		enterDestination(searchOpts.get("location"));
		selectSearchSuggestion(searchOpts.get("suggestion"));
	}

	@Then("^I enter destination as \"(.*?)\"$")
	public void enterDestination(String arg1) throws Throwable {
		Common.delay(1);
		SearchScreen.waitForSearchEditText();
		SearchScreen.searchEditTypeAhead(arg1);
		Common.delay(1); //Needed, because the hierarchy is not immediately available.
	}

	@Then("^I select (.*?) with the text \"(.*?)\"$")
	public void selectHotelWithText(String locType, String text) throws Throwable {
		switch (locType) {
			case "hotel": SearchScreenActions.selectHotelIconWithSiblingText(text); break;
			case "location": SearchScreenActions.selectLocationIconWithSiblingText(text); break;
			case "location in hierarchy": SearchScreenActions.selectLocationInHierarchyWithSiblingText(text); break;
			default: throw new Resources.NotFoundException("'" + locType + "' has not yet been implemented");
		}

	}

	@Then("^I select (\\d+) , (\\d+) as check in and checkout date$")
	public void selectDates(final int startDate, final int enDate) throws Throwable {
		LocalDate strtDate = LocalDate.now().plusDays(startDate);
		LocalDate endDate = LocalDate.now().plusDays(enDate);
		SearchScreenActions.chooseDatesWithDialog(strtDate, endDate);
	}

	@Then("^I select \"(.*?)\" as search suggestion$")
	public void selectSearchSuggestion(String arg1) throws Throwable {
		SearchScreenActions.selectLocation(arg1);
	}

	@Given("^I click on Search Button$")
	public void clickOnSearchButton() throws Throwable {
		SearchScreen.searchButton().perform(click());
	}
}

