package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import android.support.test.espresso.ViewAssertion;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;

public class HotelDetailEtpFreeCancellationTest extends HotelTestCase {

	@Test
	public void testETPHotelWithoutFreeCancellationHavingRenovation() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("hotel_etp_renovation_resort");
		HotelInfoSiteScreen.waitForDetailsLoaded();

		assertViewsBasedOnETPAndFreeCancellation(true, false);
		onView(withId(R.id.detail_container)).perform(ViewActions.swipeDown());
		assertPayLaterPayNowRooms();
	}

	@Test
	public void testNonETPHotelWithoutFreeCancellation() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("happypath");
		HotelInfoSiteScreen.waitForDetailsLoaded();

		assertViewsBasedOnETPAndFreeCancellation(false, false);
	}

	@Test
	public void testNonETPHotelWithFreeCancellation() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("hotel_non_etp_with_free_cancellation");
		HotelInfoSiteScreen.waitForDetailsLoaded();

		assertViewsBasedOnETPAndFreeCancellation(false, true);
	}

	private void assertPayLaterPayNowRooms() throws Throwable {
		//pay now view should show all the rooms and
		//pay later view should only show the rooms with pay later offer

		int numberOfPayNowRooms = EspressoUtils.getListChildCount(HotelInfoSiteScreen.roomsContainer());

		HotelInfoSiteScreen.clickPayLater();
		int numberOfPayLaterRooms = EspressoUtils.getListChildCount(HotelInfoSiteScreen.roomsContainer());
		screenshot("Pay_Later_Rooms");

		assertTrue(numberOfPayNowRooms > numberOfPayLaterRooms);
	}

	private void assertViewsBasedOnETPAndFreeCancellation(boolean hasETP, boolean hasFreeCancellation)
		throws Throwable {
		Common.delay(1);
		//resort fees view not displayed,it is only displayed when you scroll down
		HotelInfoSiteScreen.resortFeesText().check(matches(not(isDisplayed())));

		HotelInfoSiteScreen.etpAndFreeCancellationMessagingContainer().check(displayedWhen(hasETP || hasFreeCancellation));

		// Check when only one of etp and free cancellation is shown
		HotelInfoSiteScreen.etpInfoText().check(displayedWhen(hasETP && !hasFreeCancellation));
		HotelInfoSiteScreen.freeCancellation().check(displayedWhen(!hasETP && hasFreeCancellation));

		// Check when etp and free cancellation is shown
		HotelInfoSiteScreen.etpInfoTextSmall().check(displayedWhen(hasETP && hasFreeCancellation));
		HotelInfoSiteScreen.freeCancellationSmall().check(displayedWhen(hasETP && hasFreeCancellation));
		HotelInfoSiteScreen.horizontalDividerBwEtpAndFreeCancellation().check(displayedWhen(hasETP && hasFreeCancellation));

		//common amenities text is displayed
		if (hasETP) {
			HotelInfoSiteScreen.commonAmenitiesText().perform(scrollTo()).check(matches(isDisplayed()));
		}

		HotelInfoSiteScreen.clickStickySelectRoom();
		HotelInfoSiteScreen.etpPlaceholder().check(matches(hasETP ? isDisplayed() : not(isDisplayed())));
		HotelInfoSiteScreen.payNowAndLaterOptions().check(matches(hasETP ? isDisplayed() : not(isDisplayed())));

		//is displayed after scrolling down
		HotelInfoSiteScreen.resortFeesText().check(matches(isDisplayed()));
	}

	private ViewAssertion displayedWhen(boolean b) {
		return matches(b ? isDisplayed() : not(isDisplayed()));
	}

}
