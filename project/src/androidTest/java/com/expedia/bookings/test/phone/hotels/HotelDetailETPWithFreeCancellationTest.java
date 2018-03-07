package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;

import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.CoreMatchers.not;

public class HotelDetailETPWithFreeCancellationTest extends HotelTestCase {

	@Test
	public void testPayLaterHotelWithFreeCancellation() throws Throwable {
		SearchScreenActions.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("hotel_etp_renovation_resort_with_free_cancellation");
		HotelInfoSiteScreen.waitForDetailsLoaded();

		assertViewsBasedOnETPAndFreeCancellation();
		assertPayLaterPayNowRooms();
	}

	private void assertPayLaterPayNowRooms() throws Throwable {
		//pay now view should show all the rooms and
		//pay later view should only show the rooms with pay later offer
		HotelInfoSiteScreen.payNowAndLaterOptions().perform(scrollTo());
		int numberOfPayNowRooms = EspressoUtils.getListChildCount(HotelInfoSiteScreen.roomsContainer());

		HotelInfoSiteScreen.clickPayLater();
		int numberOfPayLaterRooms = EspressoUtils.getListChildCount(HotelInfoSiteScreen.roomsContainer());
		screenshot("Pay_Later_Rooms");

		assertTrue(numberOfPayNowRooms > numberOfPayLaterRooms);
	}

	private void assertViewsBasedOnETPAndFreeCancellation() throws Throwable {
		Common.delay(1);
		//resort fees view not displayed,it is only displayed when you scroll down
		HotelInfoSiteScreen.resortFeesText().check(matches(not(isDisplayed())));

		HotelInfoSiteScreen.etpAndFreeCancellationMessagingContainer().check(matches(isDisplayed()));

		// Check when only one of etp and free cancellation is shown
		HotelInfoSiteScreen.etpInfoText().check(matches(not(isDisplayed())));
		HotelInfoSiteScreen.freeCancellation().check(matches(not(isDisplayed())));

		// Check when etp and free cancellation is shown
		HotelInfoSiteScreen.etpInfoTextSmall().check(matches(isDisplayed()));
		HotelInfoSiteScreen.freeCancellationSmall().check(matches(isDisplayed()));
		HotelInfoSiteScreen.horizontalDividerBwEtpAndFreeCancellation().check(matches(isDisplayed()));

		//common amenities text is displayed
		HotelInfoSiteScreen.commonAmenitiesText().perform(scrollTo()).check(matches(isDisplayed()));

		HotelInfoSiteScreen.clickStickySelectRoom();
		HotelInfoSiteScreen.etpPlaceholder().check(matches(isDisplayed()));
		HotelInfoSiteScreen.payNowAndLaterOptions().check(matches(isDisplayed()));

		//is displayed after scrolling down
		HotelInfoSiteScreen.resortFeesText().check(matches(isDisplayed()));

		//we need to come back to the top after we're done asserting.
		HotelInfoSiteScreen.etpAndFreeCancellationMessagingContainer().perform(scrollTo());
	}
}
