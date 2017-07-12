package com.expedia.bookings.test.stepdefs.phone.bundleDeals;


import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class PackagesSearchResultsSteps {

	@And("^I wait for hotels results for packages to load$")
	public void waitForHotelsForPackage() throws Throwable {
		HotelScreen.hotelResultsList().perform(waitForViewToDisplay());
	}

	@And("^I tap on bundle overview sliding widget bar at the bottom$")
	public void launchBundleOverviewFromHSR() throws Throwable {
		PackageScreen.bundlePriceWidget().perform(waitForViewToDisplay());
		PackageScreen.bundlePriceWidget().perform(click());
		PackageScreen.inboundFlightInfo().perform(waitForViewToDisplay());
	}

	@Then("^Hotels SRP is displayed$")
	public void validateHotelResultsShown() throws Throwable {
		HotelScreen.hotelResultsList().check(matches(isDisplayed()));
	}
}
