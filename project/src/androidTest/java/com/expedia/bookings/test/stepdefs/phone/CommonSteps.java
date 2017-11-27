package com.expedia.bookings.test.stepdefs.phone;

import java.util.List;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;

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
			case "Singapore":
				Common.setPOS(PointOfSaleId.SINGAPORE);
				break;
			case "United Kingdom":
				Common.setPOS(PointOfSaleId.UNITED_KINGDOM);
				break;
			case "Japan":
				Common.setPOS(PointOfSaleId.JAPAN);
				break;
			case "New Zealand":
				Common.setPOS(PointOfSaleId.NEW_ZEALND);
				break;
			case "Malaysia":
				Common.setPOS(PointOfSaleId.MALAYSIA);
				break;
			case "Germany":
				Common.setPOS(PointOfSaleId.GERMANY);
				break;
			case "Thailand":
				Common.setPOS(PointOfSaleId.THAILAND);
				break;
		}
	}

	@Given("^I launch the App$")
	public void validateHomeScreenAppears() throws Throwable {
		LaunchScreen.hotelsLaunchButton().perform(ViewActions.waitForViewToCompletelyDisplay());
		LaunchScreen.shopButton().perform(waitForViewToDisplay(), click());
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
		if (list.contains("FlightTravelerFormRevamp")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp.getKey(),
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("FlightSubpub")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightSubpubChange.getKey(),
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("FlightFlex")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightFlexEnabled.getKey(),
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("FlightShowMoreInfo")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightsMoreInfoOnOverview.getKey(),
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("PackagesNewPOSLaunch")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppPackagesEnablePOS.getKey(),
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("PackagesBackFlowFromOverview")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.PackagesBackFlowFromOverview.getKey(),
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("EBAndroidAppPackagesDisplayFlightSeatingClass")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppPackagesDisplayFlightSeatingClass.getKey(),
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		Db.sharedInstance.setAbacusResponse(abacusResponse);
	}

	@And("^I put following tests in control$")
	public void contronABTest(List<String> list) {
		if (list.contains("FlightsCrossSellPackage")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightsCrossSellPackageOnFSR.getKey(),
				AbacusUtils.DefaultVariant.CONTROL.ordinal());
		}
		if (list.contains("PackagesBackFlowFromOverview")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.PackagesBackFlowFromOverview.getKey(),
				AbacusUtils.DefaultVariant.CONTROL.ordinal());
		}
		Db.sharedInstance.setAbacusResponse(abacusResponse);
	}

	@And("^I enable following features$")
	public void enableFeatures(List<String> list) {
		for (String key : list) {
			Common.setFeatureFlag(key, true);
		}
	}

	@And("^I disable following features$")
	public void disableFeatures(List<String> list) {
		for (String key : list) {
			Common.setFeatureFlag(key, false);
		}
	}


	@And("^I press back$")
	public void hitBack() {
		Common.pressBack();
	}

	@And("^I press back following number of times: (\\d+)$")
	public void hitBackNumberOfTimes(int number) {
		for (int iterator = 0 ; iterator < number ; iterator++) {
			Common.pressBack();
		}
	}

}
