package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import android.support.test.espresso.ViewAssertion;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

public class HotelDetailEtpFreeCancellationTest extends HotelTestCase {

	@Test
	public void testETPHotelWithoutFreeCancellationHavingRenovation() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("hotel_etp_renovation_resort");
		HotelScreen.waitForDetailsLoaded();

		assertViewsBasedOnETPAndFreeCancellation(true, false);
		assertPayLaterPayNowRooms();
	}

	@Test
	public void testNonETPHotelWithoutFreeCancellation() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("happypath");
		HotelScreen.waitForDetailsLoaded();

		assertViewsBasedOnETPAndFreeCancellation(false, false);
	}

	@Test
	public void testNonETPHotelWithFreeCancellation() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("hotel_non_etp_with_free_cancellation");
		HotelScreen.waitForDetailsLoaded();

		assertViewsBasedOnETPAndFreeCancellation(false, true);
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

	private void assertViewsBasedOnETPAndFreeCancellation(boolean hasETP, boolean hasFreeCancellation)
		throws Throwable {
		Common.delay(1);
		//resort fees view not displayed,it is only displayed when you scroll down
		HotelScreen.resortFeesText().check(matches(not(isDisplayed())));

		HotelScreen.etpAndFreeCancellationMessagingContainer().check(displayedWhen(hasETP || hasFreeCancellation));

		// Check when only one of etp and free cancellation is shown
		HotelScreen.etpInfoText().check(displayedWhen(hasETP && !hasFreeCancellation));
		HotelScreen.freeCancellation().check(displayedWhen(!hasETP && hasFreeCancellation));

		// Check when etp and free cancellation is shown
		HotelScreen.etpInfoTextSmall().check(displayedWhen(hasETP && hasFreeCancellation));
		HotelScreen.freeCancellationSmall().check(displayedWhen(hasETP && hasFreeCancellation));
		HotelScreen.horizontalDividerBwEtpAndFreeCancellation().check(displayedWhen(hasETP && hasFreeCancellation));

		//common amenities text is displayed
		if (hasETP) {
			HotelScreen.commonAmenitiesText().perform(scrollTo()).check(matches(isDisplayed()));
		}

		HotelScreen.addRoom().perform(scrollTo());
		Common.delay(1);
		onView(allOf(withId(R.id.room_header_image), isDisplayed())).perform(swipeUp());
		Common.delay(1);
		onView(allOf(withId(R.id.room_header_image), isDisplayed())).perform(swipeUp());
		Common.delay(1);
		HotelScreen.etpPlaceholder().check(matches(hasETP ? isDisplayed() : not(isDisplayed())));
		HotelScreen.payNowAndLaterOptions().check(matches(hasETP ? isDisplayed() : not(isDisplayed())));

		//is displayed after scrolling down
		HotelScreen.resortFeesText().check(matches(isDisplayed()));
	}

	private ViewAssertion displayedWhen(boolean b) {
		return matches(b ? isDisplayed() : not(isDisplayed()));
	}

}
