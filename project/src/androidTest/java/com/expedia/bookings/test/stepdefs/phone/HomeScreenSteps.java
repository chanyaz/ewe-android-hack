package com.expedia.bookings.test.stepdefs.phone;


import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import cucumber.api.java.en.And;
import static android.support.test.espresso.action.ViewActions.click;

import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class HomeScreenSteps {

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


