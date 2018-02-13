package com.expedia.bookings.test.stepdefs.phone;

import android.support.test.InstrumentationRegistry;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.ABTest;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.abacus.AbacusVariant;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import com.mobiata.android.util.SettingUtils;

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
			case "France":
				Common.setPOS(PointOfSaleId.FRANCE);
				break;
			case "Italy":
				Common.setPOS(PointOfSaleId.ITALY);
				break;
		}
	}

	@Given("^I launch the App$")
	public void validateHomeScreenAppears() throws Throwable {
		LaunchScreen.hotelsLaunchButton().perform(ViewActions.waitForViewToCompletelyDisplay());
		LaunchScreen.shopButton().perform(waitForViewToDisplay(), click());
	}

	@And("^I bucket the following tests$")
	@Deprecated
	public void bucketABTest(List<String> list) throws Throwable {
		if (list.contains("FlightsSeatClassAndBookingCode")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode.getKey(),
					AbacusVariant.BUCKETED.getValue());
		}
		if (list.contains("FlightByotSearch")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightByotSearch.getKey(),
					AbacusVariant.BUCKETED.getValue());
		}
		if (list.contains("FlightXSellPackage")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightsCrossSellPackageOnFSR.getKey(),
					AbacusVariant.BUCKETED.getValue());
		}
		if (list.contains("FlightTravelerFormRevamp")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp.getKey(),
					AbacusVariant.BUCKETED.getValue());
		}
		if (list.contains("FlightSubpub")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightSubpubChange.getKey(),
					AbacusVariant.BUCKETED.getValue());
		}
		if (list.contains("FlightFlex")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightFlexEnabled.getKey(),
					AbacusVariant.BUCKETED.getValue());
		}
		if (list.contains("FlightShowMoreInfo")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightsMoreInfoOnOverview.getKey(),
					AbacusVariant.BUCKETED.getValue());
		}
		if (list.contains("PackagesNewPOSLaunch")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppPackagesEnablePOS.getKey(),
					AbacusVariant.BUCKETED.getValue());
		}
		if (list.contains("PackagesBackFlowFromOverview")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.PackagesBackFlowFromOverview.getKey(),
					AbacusVariant.BUCKETED.getValue());
		}
		if (list.contains("EBAndroidAppPackagesDisplayFlightSeatingClass")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppPackagesDisplayFlightSeatingClass.getKey(),
					AbacusVariant.BUCKETED.getValue());
		}
		if (list.contains("EBAndroidAppTripsMessageHotel")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppTripsMessageHotel.getKey(),
					AbacusVariant.BUCKETED.getValue());
		}
		if (list.contains("TripsHotelsM2")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.TripsHotelsM2.getKey(),
					AbacusVariant.BUCKETED.getValue());
		}

		Db.sharedInstance.setAbacusResponse(abacusResponse);
	}

	@And("^I put following tests in control$")
	@Deprecated
	public void contronABTest(List<String> list) {
		if (list.contains("FlightsCrossSellPackage")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightsCrossSellPackageOnFSR.getKey(),
					AbacusVariant.CONTROL.getValue());
		}
		if (list.contains("PackagesBackFlowFromOverview")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.PackagesBackFlowFromOverview.getKey(),
					AbacusVariant.CONTROL.getValue());
		}
		Db.sharedInstance.setAbacusResponse(abacusResponse);
	}

	@And("^I set bucketing rules for A/B tests as$")
	public void setBucketingRulesForTests(Map<String, String> map) throws Throwable {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			Field test = AbacusUtils.class.getField(entry.getKey());
			test.setAccessible(true);
			Field variant = AbacusVariant.class.getField(entry.getValue());
			variant.setAccessible(true);
			ABTest abTest = (ABTest) test.get(test);
			AbacusVariant abVariant = (AbacusVariant) variant.get(variant);

			if (abTest.getRemote()) {
				SettingUtils.save(
						InstrumentationRegistry.getTargetContext(),
						Integer.toString(abTest.getKey()),
						abVariant.getValue()
				);
			}

			abacusResponse.updateABTestForDebug(
					abTest.getKey(),
					abVariant.getValue()
			);
		}
		Db.sharedInstance.setAbacusResponse(abacusResponse);
	}

	@And("^I (enable|disable) following features$")
	public void enableDisableFeatures(String enableDisable, List<String> list) {
		Boolean enable = enableDisable.equalsIgnoreCase("enable");

		for (String key : list) {
			Common.setFeatureFlag(key, enable);
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
