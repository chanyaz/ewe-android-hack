package com.expedia.bookings.test.stepdefs.phone;

import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;

import static android.support.test.espresso.action.ViewActions.click;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class HomeScreenSteps {

	@Given("^I launch the App$")
	public void validateHomeScreenAppears() throws Throwable {
		NewLaunchScreen.shopButton().perform(waitForViewToDisplay(), click());
		NewLaunchScreen.hotelsLaunchButton().perform(ViewActions.waitForViewToCompletelyDisplay());
	}

	@And("^I launch \"(.*?)\" LOB$")
	public void homeButtonClick(String lob) throws Throwable {
		switch (lob) {
		case "Hotels":
			NewLaunchScreen.hotelsLaunchButton().perform(waitForViewToDisplay(), click());
			break;
		case "Flights":
			NewLaunchScreen.flightLaunchButton().perform(waitForViewToDisplay(), click());
			break;
		}
	}

	@And("^I set Flight Round Trip AB test to \"(.*?)\"")
	public void setFlightRoundTripABTest(String variant) throws Throwable {
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightTest,AbacusUtils.EBAndroidAppMaterialFlightSearchRoundTripMessage,AbacusUtils.EBAndroidAppFlightUrgencyMessage);
	}

	@And("^2 AB test")
	public void secondabtest() throws Throwable {

	}
}

