package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

public class HotelDetailETPWithFreeCancellationTest extends HotelTestCase {

	@Test
	public void testPayLaterHotelWithFreeCancellation() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("hotel_etp_renovation_resort_with_free_cancellation");
		HotelScreen.waitForDetailsLoaded();

		assertViewsBasedOnETPAndFreeCancellation();
		assertPayLaterPayNowRooms();
	}

	private void assertPayLaterPayNowRooms() throws Throwable {
		//pay now view should show all the rooms and
		//pay later view should only show the rooms with pay later offer

		int numberOfPayNowRooms = EspressoUtils.getListChildCount(HotelScreen.roomsContainer());

		HotelScreen.clickPayLater();
		int numberOfPayLaterRooms = EspressoUtils.getListChildCount(HotelScreen.roomsContainer());
		screenshot("Pay_Later_Rooms");

		assertTrue(numberOfPayNowRooms > numberOfPayLaterRooms);
	}

	private void assertViewsBasedOnETPAndFreeCancellation() throws Throwable {
		Common.delay(1);
		//resort fees view not displayed,it is only displayed when you scroll down
		HotelScreen.resortFeesText().check(matches(not(isDisplayed())));

		HotelScreen.etpAndFreeCancellationMessagingContainer().check(matches(isDisplayed()));

		// Check when only one of etp and free cancellation is shown
		HotelScreen.etpInfoText().check(matches(not(isDisplayed())));
		HotelScreen.freeCancellation().check(matches(not(isDisplayed())));

		// Check when etp and free cancellation is shown
		HotelScreen.etpInfoTextSmall().check(matches(isDisplayed()));
		HotelScreen.freeCancellationSmall().check(matches(isDisplayed()));
		HotelScreen.horizontalDividerBwEtpAndFreeCancellation().check(matches(isDisplayed()));

		//common amenities text is displayed
		HotelScreen.commonAmenitiesText().perform(scrollTo()).check(matches(isDisplayed()));


		HotelScreen.addRoom().perform(scrollTo());
		Common.delay(1);
		onView(allOf(withId(R.id.room_header_image), isDisplayed())).perform(swipeUp());
		Common.delay(1);
		onView(allOf(withId(R.id.room_header_image), isDisplayed())).perform(swipeUp());
		Common.delay(1);
		HotelScreen.etpPlaceholder().check(matches(isDisplayed()));
		HotelScreen.payNowAndLaterOptions().check(matches(isDisplayed()));

		//is displayed after scrolling down
		HotelScreen.resortFeesText().check(matches(isDisplayed()));
	}
}
