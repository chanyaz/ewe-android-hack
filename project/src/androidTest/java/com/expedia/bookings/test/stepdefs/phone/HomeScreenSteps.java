package com.expedia.bookings.test.stepdefs.phone;


import java.util.List;
import java.util.Locale;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSaleId;
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

	@And("^I bucket the following tests$")
	public void bucketABTest(List<String> list) throws Throwable {
		AbacusResponse abacusResponse = new AbacusResponse();
		if (list.contains("RoundTripOnFlightsFSR")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppMaterialFlightSearchRoundTripMessage,
				AbacusUtils.DefaultVariate.BUCKETED.ordinal());
		}
		if (list.contains("UrgencyMessegingOnFSR")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightUrgencyMessage,
				AbacusUtils.DefaultVariate.BUCKETED.ordinal());
		}
		Db.setAbacusResponse(abacusResponse);
	}

	@And("^I change the POS to \"(.*?)\"$")
	public void changePOS(String newPOS) throws Throwable {
		switch (newPOS) {
		case "US":
			Common.setPOS(PointOfSaleId.UNITED_STATES);
		case "AU":
			Common.setPOS(PointOfSaleId.AUSTRALIA);
		}
	}

	@And("^I change the locale to \"(.*?)\"$")
	public void changeLocale(String newLocale) throws Throwable {
		Common.setLocale(new Locale("en", newLocale));
	}
}

