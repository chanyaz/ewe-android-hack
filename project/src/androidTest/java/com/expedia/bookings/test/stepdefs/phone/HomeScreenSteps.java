package com.expedia.bookings.test.stepdefs.phone;

import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;

import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

public class HomeScreenSteps {

	@Given("^I have the app installed and I open it\\.$")
	public void validateHomeScreenAppears() throws Throwable {
		EspressoUtils.waitForViewNotYetInLayoutToDisplay(allOf(withText("Hotels"), isCompletelyDisplayed()), 10,
			TimeUnit.SECONDS);
	}

	@Then("^I should be able to click Hotels button to launch the hotel search screen$")
	public void homeButtonClick() throws Throwable {
		NewLaunchScreen.hotelsLaunchButton().perform(click());
	}

	@Then("^I should be able to click LOB button with caption \"(.*?)\" on home page$")
	public void homePackagesButtonClick(String displayName) throws Throwable {
		NewLaunchScreen.packagesLaunchButton(displayName).perform(click());
	}
}

