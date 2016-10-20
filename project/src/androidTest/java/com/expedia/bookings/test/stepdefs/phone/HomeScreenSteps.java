package com.expedia.bookings.test.stepdefs.phone;

import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

public class HomeScreenSteps {

	@Given("^I have the app installed and I open it\\.$")
	public void validateHomeScreenAppears() throws Throwable {
		NewLaunchScreen.hotelsLaunchButton().check(matches(isDisplayed()));
	}

	@Then("^I should be able to click Hotels button to launch the hotel search screen$")
	public void homeButtonClick() throws Throwable {
		NewLaunchScreen.hotelsLaunchButton().perform(click());
	}

}

