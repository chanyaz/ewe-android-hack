package com.expedia.bookings.test.phone.newhotels;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

public class NewHotelDetailTest extends HotelTestCase {
	/*
		public void testPayLaterHotelWithFreeCancellation() throws Throwable {
			final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
			final DateTime endDateTime = startDateTime.plusDays(3);
			HotelScreen.location().perform(typeText("SFO"));
			HotelScreen.selectLocation("San Francisco, CA");
			HotelScreen.selectDateButton().perform(click());
			HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
			HotelScreen.searchButton().perform(click());
			HotelScreen.selectHotel(13);

			assertViewsBasedOnETPAndFreeCancellation(true, true, "ETP_Hotel_With_Free_Cancellation");
			assertPayLaterPayNowRooms();
		}
	*/

	public void testVIPHotel() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		HotelScreen.selectDateButton().perform(click());
		HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		HotelScreen.searchButton().perform(click());
		HotelScreen.selectHotelWithName("vip_hotel");
		Common.delay(2);
		HotelScreen.clickVIPAccess();
		Common.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(getActivity().getString(R.string.vip_access_message));
		Common.pressBack();
		Common.delay(1);
		EspressoUtils.assertViewIsDisplayed(R.id.vip_access_message);

	}

	public void testETPHotelWithoutFreeCancellationHavingRenovation() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		HotelScreen.selectDateButton().perform(click());
		HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		HotelScreen.searchButton().perform(click());
		HotelScreen.selectHotel(12);

		assertViewsBasedOnETPAndFreeCancellation(true, false, "ETP_Hotel_Without_Free_Cancellation");
		assertPayLaterPayNowRooms();
		Common.delay(2);

		// checking we are not showing rating and amenities container
		HotelScreen.ratingContainer().check(matches(not(isDisplayed())));
		HotelScreen.amenityContainer().check(matches(not(isDisplayed())));

		//if current allotment < 5, we show number of rooms left on "collapsed room container"
		//otherwise we just show free cancellation message

		HotelScreen.clickPayNow();
		onView(allOf(withId(R.id.room_header_image), isDisplayed())).perform(swipeUp());
		Common.delay(1);
		onView(withText("View Room")).perform(scrollTo());
		onView(allOf(withId(R.id.collapsed_urgency_text_view), withParent(
			withId(R.id.collapsed_container)), isDisplayed(), hasSibling(withText("2 double"))))
			.check(matches(withText("1 Room Left!")));

		onView(withText("View Room")).perform(click());

		Common.delay(1);
		onView(allOf(withId(R.id.collapsed_urgency_text_view), withParent(
			withId(R.id.collapsed_container)), isDisplayed(), hasSibling(withText("One King Bed"))))
			.check(matches(withText("Non-refundable")));
	}

	/*
		public void testNonETPHotelWithoutFreeCancellation() throws Throwable {
			final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
			final DateTime endDateTime = startDateTime.plusDays(3);
			HotelScreen.location().perform(typeText("SFO"));
			HotelScreen.selectLocation("San Francisco, CA");
			HotelScreen.selectDateButton().perform(click());
			HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
			HotelScreen.searchButton().perform(click());
			HotelScreen.selectHotel(2);

			assertViewsBasedOnETPAndFreeCancellation(false, false, "Non_ETP_Hotel_Without_Free_Cancellation");
		}

		public void testNonETPHotelWithFreeCancellation() throws Throwable {
			final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
			final DateTime endDateTime = startDateTime.plusDays(3);
			HotelScreen.location().perform(typeText("SFO"));
			HotelScreen.selectLocation("San Francisco, CA");
			HotelScreen.selectDateButton().perform(click());
			HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
			HotelScreen.searchButton().perform(click());
			HotelScreen.selectHotel(14);

			assertViewsBasedOnETPAndFreeCancellation(false, true, "Non_ETP_Hotel_With_Free_Cancellation");
		}
	*/
	private void assertPayLaterPayNowRooms() throws Throwable {
		//pay now view should show all the rooms and
		//pay later view should only show the rooms with pay later offer

		int numberOfPayNowRooms = EspressoUtils.getListChildCount(HotelScreen.roomsContainer());

		HotelScreen.clickPayLater();
		int numberOfPayLaterRooms = EspressoUtils.getListChildCount(HotelScreen.roomsContainer());
		screenshot("Pay_Later_Rooms");

		assertTrue(numberOfPayNowRooms > numberOfPayLaterRooms);
	}

	private void assertViewsBasedOnETPAndFreeCancellation(boolean hasETP, boolean hasFreeCancellation,
		String screenshotTitle) throws Throwable {
		screenshot(screenshotTitle);

		//resort fees view not displayed,it is only displayed when you scroll down
		HotelScreen.resortFeesText().check(matches(not(isDisplayed())));

		//common amenities text is displayed
		if (hasETP) {
			HotelScreen.commonAmenitiesText().perform(scrollTo()).check(matches(isDisplayed()));
		}

		HotelScreen.etpAndFreeCancellationMessagingContainer().check(
			matches((hasETP || hasFreeCancellation) ? isDisplayed() : not(isDisplayed())));
		HotelScreen.etpInfoText().check(matches(hasETP ? isDisplayed() : not(isDisplayed())));
		HotelScreen.freeCancellation().check(matches(hasFreeCancellation ? isDisplayed() : not(isDisplayed())));
		HotelScreen.horizontalDividerBwEtpAndFreeCancellation().check(
			matches((hasETP && hasFreeCancellation) ? isDisplayed() : not(isDisplayed())));

		Common.delay(1);
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

}
