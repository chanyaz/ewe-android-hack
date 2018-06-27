package com.expedia.bookings.test.stepdefs.phone.hotel;

import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.SearchResultsScreen;
import com.expedia.bookings.test.pagemodels.hotels.SearchResultsScreen.IndividualResult;
import com.expedia.bookings.test.pagemodels.hotels.SearchResultsScreen.SearchResultList;

import cucumber.api.java.en.And;

public class SearchResultsScreenSteps {

	@And("^I scroll to hotel with name (.*?)$")
	public void iScrollToHotelWithName(String name) throws Throwable {
		SearchResultList.scrollToResultWithName(name);
	}

	@And("^I verify the hotel (.*?) has a vip label$")
	public void iVerifyHotelWithNameHasVIPLabel(String name) throws Throwable {
		SearchResultList.resultWithName(name).verifyVipLabelIsVisible();
	}

	@And("^I click on a hotel with name (.*?)$")
	public void iClickOnHotelWithName(String name) throws Throwable {
		SearchResultList.resultWithName(name).click();
		HotelInfoSiteScreen.waitForPageToLoad();
	}

	@And("^I wait for hotel search results to load$")
	public void waitForHotelResultsToLoad() throws Throwable {
		SearchResultsScreen.waitForResultsToLoad();
	}

	@And("^I verify pinned hotel name is (.*?)$")
	public void iVerifyPinnedHotelName(String hotelName) throws Throwable {
		IndividualResult hotelResult = SearchResultList.resultAtPosition(1);
		hotelResult.verifyIsPinned();
		hotelResult.verifyName(hotelName);
	}
	@And("^I click on pinned hotel$")
	public void iClickOnPinnedHotel() throws Throwable {
		IndividualResult hotelResult = SearchResultList.resultAtPosition(1);
		hotelResult.verifyIsPinned();
		hotelResult.click();
	}
}
