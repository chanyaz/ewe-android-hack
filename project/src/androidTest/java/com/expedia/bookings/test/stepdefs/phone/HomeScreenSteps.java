package com.expedia.bookings.test.stepdefs.phone;


import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.support.Users;
import com.expedia.bookings.test.support.Users.User;
import cucumber.api.java.en.And;
import static android.support.test.espresso.action.ViewActions.click;

import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class HomeScreenSteps {
	@And("^I tap on \"(Shop|Trip|Account)\" tab$")
	public void homeScreenSwitchToTab(String tab) throws Throwable {
		switch (tab) {
			case "Account":
				LaunchScreen.accountButton().perform(waitForViewToDisplay(), click());
				break;
			case "Trips":
				LaunchScreen.tripsButton().perform(waitForViewToDisplay(), click());
				break;
			case "Shop":
				LaunchScreen.shopButton().perform(waitForViewToDisplay(), click());
				break;
		}
	}

	@And("^I login with \"(.*?)\" tier user$")
	public void logInGivenUserTier(String userTier) throws Throwable {
		User user =  Users.Companion.findUser("tier", userTier);

		LogInScreen.logInButton().perform(waitForViewToDisplay(), click());
		LogInScreen.typeTextEmailEditText(user.getEmail());
		LogInScreen.typeTextPasswordEditText(user.getPassword());
		LogInScreen.clickOnLoginButton();
	}

	@And("^I launch \"(.*?)\" LOB$")
	public void launchLOB(String lob) throws Throwable {
		switch (lob) {
		case "Hotels":
			LaunchScreen.hotelsLaunchButton().perform(waitForViewToDisplay(), click());
			break;
		case "Flights":
			LaunchScreen.flightLaunchButton().perform(waitForViewToDisplay(), click());
			break;
		case "Bundle Deals":
			LaunchScreen.packagesLaunchButton().perform(waitForViewToDisplay(), click());
			break;
		}
	}
}


