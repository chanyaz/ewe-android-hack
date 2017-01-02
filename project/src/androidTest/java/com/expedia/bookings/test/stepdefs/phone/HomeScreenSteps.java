package com.expedia.bookings.test.stepdefs.phone;

import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;

import static android.support.test.espresso.action.ViewActions.click;

public class HomeScreenSteps {

	@Given("^I launch the App$")
	public void validateHomeScreenAppears() throws Throwable {
		NewLaunchScreen.hotelsLaunchButton().perform(ViewActions.waitForViewToCompletelyDisplay());
	}

	@And("^I launch \"(.*?)\" LOB$")
	public void homeButtonClick(String lob) throws Throwable {
		switch (lob) {
		case "Hotels":
			NewLaunchScreen.hotelsLaunchButton().perform(ViewActions.waitForViewToDisplay(), click());
			break;
		case "Flights":
			NewLaunchScreen.flightLaunchButton().perform(ViewActions.waitForViewToDisplay(), click());
			break;
		}
	}
}

