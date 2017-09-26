package com.expedia.bookings.test.stepdefs.phone.hotel;

import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSite;
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelSearchResults;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

public class SearchResultsScreenSteps {

	@Then("^I can see hotel search results$")
	public void waitForSearchResultsToAppear() throws Throwable {
		HotelScreen.waitForResultsLoaded(30);
	}

	@And("^I verify that the vip label is present for any of the hotels in the list$")
	public void iVerifyThatTheVipLabelIsForAnyOfTheHotelsInTheList() throws Throwable {
		HotelSearchResults.verifyVipLabelIsPresentInResultList();
	}

	@And("^I click on a hotel with a vip label$")
	public void iClickOnTheVipLabel() throws Throwable {
		HotelSearchResults.uPlusVipLabel().click();
		HotelInfoSite.waitForPageToLoad();
	}

	@And("^I wait for hotel search results to load$")
	public void waitForHotelResultsToLoad() throws Throwable {
		HotelSearchResults.waitForResultsToLoad();
	}
}
