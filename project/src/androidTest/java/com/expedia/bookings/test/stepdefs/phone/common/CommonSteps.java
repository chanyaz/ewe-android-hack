package com.expedia.bookings.test.stepdefs.phone.common;

import android.support.test.InstrumentationRegistry;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.ABTest;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.abacus.AbacusVariant;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.Settings;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import com.mobiata.android.util.SettingUtils;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;

import static android.support.test.espresso.action.ViewActions.click;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToCompletelyDisplay;

public class CommonSteps {
	AbacusResponse abacusResponse = new AbacusResponse();
	@And("^I set the POS to \"(.*?)\"$")
	@Deprecated
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
			case "South Korea":
				Common.setPOS(PointOfSaleId.SOUTH_KOREA);
				break;
			case "Mexico":
				Common.setPOS(PointOfSaleId.MEXICO);
				break;
		}
	}

	@And("^I switch the POS to \"(.*?)\"$")
	public static void switchToPOS(String name) throws Throwable {
		Field pos = PointOfSaleId.class.getDeclaredField(name);
		pos.setAccessible(true);
		Common.setPOS((PointOfSaleId) pos.get(PointOfSaleId.class));
	}

	@And("^I set the Locale to \"(.*?)\" language of \"(.*?)\" dialect$")
	public void setLocale(String langCode, String dialectCode) throws Throwable {
		Common.setLocale(new Locale(langCode, dialectCode));
	}

	@Given("^I launch the App$")
	public static void validateHomeScreenAppears() throws Throwable {
		LaunchScreen.hotelsLaunchButton().perform(waitForViewToCompletelyDisplay());
		LaunchScreen.shopButton().perform(waitForViewToCompletelyDisplay(), click());
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

	@And("^I enable Satallite features flag for \"(.*?)\"$")
	public void enableSatelliteFeatureFlag(String key) {
		Common.enableSatelliteFeatureFlag(key);
	}

	@And("^I press back$")
	public void hitBack() {
		Common.pressBack();
	}

	@And("^I tap on back button$")
	public void tapBackButton() {
		Common.genericPressBack();
	}

	@And("^I press back following number of times: (\\d+)$")
	public void hitBackNumberOfTimes(int number) {
		for (int iterator = 0 ; iterator < number ; iterator++) {
			Common.pressBack();
		}
	}

	@And("^I set the server to \"(.*?)\"$")
	public void setServer(String name) throws Throwable {
		switch (name) {
			case "Int":
				Settings.setServer("Integration");
				break;
			case "Prod":
				Settings.setServer("Production");
				break;
		}
	}
}
