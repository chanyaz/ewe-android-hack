package com.expedia.bookings.test.phone.newhotels;

import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

public class HotelResultsPresenterTest extends HotelTestCase {

	public void testSearchResults() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA");
		HotelScreen.selectDateButton().perform(click());
		HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		HotelScreen.searchButton().perform(click());

		// Happy Path : First Item.
		assertViewIsDisplayedAtPosition(2, R.id.hotel_name_text_view);
		assertViewIsDisplayedAtPosition(2, R.id.price_per_night);
		assertViewIsDisplayedAtPosition(2, R.id.background);
		assertViewIsDisplayedAtPosition(2, R.id.hotel_rating_bar);
		assertViewIsDisplayedAtPosition(2, R.id.guest_rating);
		assertViewNotDisplayedAtPosition(2, R.id.discount_percentage);
		assertViewNotDisplayedAtPosition(2, R.id.strike_through_price);

		//test Top amenities
		assertViewWithTextIsDisplayedAtPosition(2, R.id.top_amenity_title, "Sponsored");
		assertViewWithTextIsDisplayedAtPosition(3, R.id.top_amenity_title, "Free Cancellation");

		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(4));
		assertViewWithTextIsDisplayedAtPosition(4, R.id.top_amenity_title, "Book Now, Pay Later");

		//test VIP message
		assertViewWithTextIsDisplayedAtPosition(4, R.id.vip_message, "+VIP");

		//test urgency messages
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(2));
		assertViewWithTextIsDisplayedAtPosition(2, R.id.urgency_message, "4 Rooms Left");
		assertViewWithTextIsDisplayedAtPosition(3, R.id.urgency_message, "Tonight Only!");
		assertViewWithTextIsDisplayedAtPosition(4, R.id.urgency_message, "Mobile Exclusive");
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(5));
		assertViewNotDisplayedAtPosition(5, R.id.urgency_message_layout);

		//test air attach
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(9));
		assertViewIsDisplayedAtPosition(9, R.id.air_attach_layout);
		assertViewWithTextIsDisplayedAtPosition(9, R.id.air_attach_discount, "-12%");

	}

	private void assertViewNotDisplayedAtPosition(int position, int id) {
		HotelScreen.hotelResultsList().check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), not(isDisplayed())))));
	}

	private void assertViewIsDisplayedAtPosition(int position, int id) {
		HotelScreen.hotelResultsList().check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), isDisplayed()))));
	}

	private void assertViewWithTextIsDisplayedAtPosition(int position, int id, String text) {
		HotelScreen.hotelResultsList().check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), isDisplayed(), withText(text)))));
	}
}
