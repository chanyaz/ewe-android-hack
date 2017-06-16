package com.expedia.bookings.test.stepdefs.phone;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import org.joda.time.LocalDate;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;
import com.expedia.bookings.test.stepdefs.phone.model.ApiRequestData;

import junit.framework.Assert;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;

import static android.support.test.espresso.action.ViewActions.click;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class CommonSteps {
	public static Map<String, String> storeDataAtRuntime = new HashMap();
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
		AbacusResponse abacusResponse = new AbacusResponse();
		if (list.contains("FlightsSeatClassAndBookingCode")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode,
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("FlightByotSearch")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightByotSearch,
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("FlightRateDetailExpansion")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightRateDetailExpansion,
					AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("FlightXSellPackage")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightsCrossSellPackageOnFSR,
					AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("FlightRetainSearchParams")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightRetainSearchParams,
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}
		if (list.contains("FlightStaticSortFilter")) {
			abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppFlightStaticSortFilter,
				AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		}

		Db.setAbacusResponse(abacusResponse);
	}

	@And("^I press back$")
	public void hitBack() {
		Common.pressBack();
	}

	public static void validateRequestParams(Map<String, String> expParameters, ApiRequestData apiRequestData) {
		for (Map.Entry<String, String> entry : expParameters.entrySet()) {
			Assert.assertEquals(entry.getValue(), apiRequestData.getQueryParams().get(entry.getKey()).get(0));
		}
	}

	public static String getDateInMMMdd(String days) {
		LocalDate startDate = LocalDate.now().plusDays(Integer.parseInt(days));
		Format dateFormatter = new SimpleDateFormat("MMM d", Locale.US);
		return dateFormatter.format(startDate.toDate());
	}
}
