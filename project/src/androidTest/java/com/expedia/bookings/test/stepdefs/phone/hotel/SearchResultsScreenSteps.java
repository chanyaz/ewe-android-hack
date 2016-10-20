package com.expedia.bookings.test.stepdefs.phone.hotel;

import com.expedia.bookings.test.phone.hotels.HotelScreen;

import cucumber.api.java.en.Then;

public class SearchResultsScreenSteps {

	@Then("^I wait for the results to appear$")
	public void waitForSearchResultsToAppear() throws Throwable {
		HotelScreen.waitForResultsLoaded();
	}
}

