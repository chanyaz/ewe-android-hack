package com.expedia.bookings.test.phone.newhotels;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

public class NewHotelDetailTest extends HotelTestCase {

	public void testNonETPHotel() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA");
		HotelScreen.selectDateButton().perform(click());
		HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		HotelScreen.searchButton().perform(click());
		HotelScreen.selectHotel(1);

		screenshot("Non_ETP_Hotel");
		//etp info not displayed for non etp hotel
		onView(withId(R.id.etp_info_container)).check(matches(not(isDisplayed())));

		//pay later and pay now options is not displayed for non etp hotel
		onView(withText("View Room")).perform(scrollTo());
		onView(withId(R.id.radius_pay_options)).check(matches(not(isDisplayed())));
	}

	public void testETPHotel() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA");
		HotelScreen.selectDateButton().perform(click());
		HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		HotelScreen.searchButton().perform(click());
		HotelScreen.selectHotel(11);

		assertViewsForETPHotel();
		assertPayLaterPayNowRooms();
	}

	private void assertPayLaterPayNowRooms() throws Throwable {
		//pay now view should show all the rooms and
		//pay later view should only show the rooms with pay later offer

		int numberOfPayNowRooms = EspressoUtils.getListChildCount(onView(withId(R.id.room_container)));

		HotelScreen.clickPayLater();
		int numberOfPayLaterRooms = EspressoUtils.getListChildCount(onView(withId(R.id.room_container)));
		screenshot("Pay_Later_Rooms");

		assertTrue(numberOfPayNowRooms > numberOfPayLaterRooms);
	}

	private void assertViewsForETPHotel() throws Throwable {
		screenshot("ETP_Hotel");
		//etp info displayed for etp hotel
		onView(withId(R.id.etp_info_container)).check(matches(isDisplayed()));

		//resort fees view not displayed,it is only displayed when you scroll down
		onView(withId(R.id.resort_fees_text)).check(matches(not(isDisplayed())));

		//pay later and pay now options is displayed for etp hotel
		onView(withId(R.id.radius_pay_options)).perform(scrollTo()).check(matches(isDisplayed()));

		//common amenities text is displayed
		onView(withId(R.id.common_amenities_text)).perform(scrollTo()).check(matches(isDisplayed()));

		//is displayed after scrolling down
		onView(withText("View Room")).perform(scrollTo());
		ScreenActions.delay(2);
		onView(withId(R.id.resort_fees_text)).check(matches(isDisplayed()));

		//renovation notice shows up in the end
		onView(withId(R.id.renovation_container)).perform(scrollTo()).check(matches(isDisplayed()));
		screenshot("Hotel_details_bottom_scrolled");
	}
}
