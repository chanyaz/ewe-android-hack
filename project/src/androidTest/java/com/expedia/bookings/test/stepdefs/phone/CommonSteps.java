package com.expedia.bookings.test.stepdefs.phone;

import java.util.List;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.NewLaunchScreen;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;

import static android.support.test.espresso.action.ViewActions.click;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class CommonSteps {
	AbacusResponse abacusResponse = new AbacusResponse();
	@And("^I set the POS to \"(.*?)\"$")
	public void setPOS(String name) throws Throwable {
		switch (name) {
			case "Indonesia":
				Common.setPOS(PointOfSaleId.INDONESIA);
				break;
			case "United States":
				Common.setPOS(PointOfSaleId.UNITED_STATES);
				break;
			case "Canada":
				Common.setPOS(PointOfSaleId.CANADA);
				break;
			case "Australia":
				Common.setPOS(PointOfSaleId.AUSTRALIA);
				break;
		}
	}

	@Given("^I launch the App$")
	public void validateHomeScreenAppears() throws Throwable {
		NewLaunchScreen.hotelsLaunchButton().perform(ViewActions.waitForViewToCompletelyDisplay());
		NewLaunchScreen.shopButton().perform(waitForViewToDisplay(), click());
	}

	@And("^I bucket the following tests$")
	public void bucketABTest(List<String> list) throws Throwable {
		if (list.contains("FlightsSeatClassAndBookingCode")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode.getKey(),
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("FlightByotSearch")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightByotSearch.getKey(),
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("FlightXSellPackage")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightsCrossSellPackageOnFSR.getKey(),
					AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("FlightRetainSearchParams")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightRetainSearchParams.getKey(),
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}

		Db.setAbacusResponse(abacusResponse);
	}

	@And("^I put following tests in control$")
	public void contronABTest(List<String> list) {
		if (list.contains("FlightsCrossSellPackage")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightsCrossSellPackageOnFSR.getKey(),
				AbacusUtils.DefaultVariant.CONTROL.ordinal());
		}
		Db.setAbacusResponse(abacusResponse);
	}



	@And("^I press back$")
	public void hitBack() {
		Common.pressBack();
	}

}
