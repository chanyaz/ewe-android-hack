package com.expedia.bookings.test.stepdefs.phone;


import com.expedia.bookings.test.pagemodels.common.NewLaunchScreen;
import cucumber.api.java.en.And;
import static android.support.test.espresso.action.ViewActions.click;

import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class HomeScreenSteps {

	@And("^I launch \"(.*?)\" LOB$")
	public void launchLOB(String lob) throws Throwable {
		switch (lob) {
		case "Hotels":
			NewLaunchScreen.hotelsLaunchButton().perform(waitForViewToDisplay(), click());
			break;
		case "Flights":
			NewLaunchScreen.flightLaunchButton().perform(waitForViewToDisplay(), click());
			break;
		case "Bundle Deals":
			NewLaunchScreen.packagesLaunchButton().perform(waitForViewToDisplay(), click());
			break;
		}
	}
}


