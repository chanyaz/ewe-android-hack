package com.expedia.bookings.test.stepdefs.phone.flights;

import com.expedia.bookings.test.phone.newflights.FlightsOverviewScreen;

import cucumber.api.java.en.And;

public class FlightsOverviewScreenSteps {
	@And("^I click on checkout button$")
	public void clickOnCheckoutButton() throws Throwable {
		FlightsOverviewScreen.clickOnCheckoutButton();
	}
}
