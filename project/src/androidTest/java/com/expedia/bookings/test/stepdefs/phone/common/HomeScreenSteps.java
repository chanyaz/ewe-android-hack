package com.expedia.bookings.test.stepdefs.phone.common;

import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;

import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.action.ViewActions.click;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static com.expedia.bookings.test.pagemodels.common.LaunchScreen.waitForLOBHeaderToBeDisplayed;

public class HomeScreenSteps {
	@And("^I tap on \"(Shop Travel|Trips|Account)\" tab$")
	public static void switchToTab(String tab) throws Throwable {
		switch (tab) {
			case "Account":
				EspressoUtils.waitForViewNotYetInLayoutToDisplay(LaunchScreen.getAccountButton(), 10, TimeUnit.SECONDS);
				LaunchScreen.accountButton().perform(click());
				break;
			case "Trips":
				EspressoUtils.waitForViewNotYetInLayoutToDisplay(LaunchScreen.getTripsButton(), 10, TimeUnit.SECONDS);
				LaunchScreen.tripsButton().perform(click());
				break;
			case "Shop Travel":
				EspressoUtils.waitForViewNotYetInLayoutToDisplay(LaunchScreen.getShopButton(), 10, TimeUnit.SECONDS);
				LaunchScreen.shopButton().perform(click());
				break;
		}
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

	@And("^I launch Package LOB with \"(.*?)\" POS$")
	public void launchPackage(String pos) throws Throwable {
		LaunchScreen.packagesLaunchButtonForPOS(pos).perform(waitForViewToDisplay(), click());
	}

	@Then("^Validate that Launch screen is displayed$")
	public void validateLaunchPage() {
		waitForLOBHeaderToBeDisplayed();
	}
}


