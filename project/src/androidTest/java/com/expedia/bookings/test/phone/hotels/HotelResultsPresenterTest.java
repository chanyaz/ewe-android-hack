package com.expedia.bookings.test.phone.hotels;

import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.Test;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;

public class HotelResultsPresenterTest extends HotelTestCase {

	@Test
	public void testSearchResults() throws Throwable {

		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);

		SearchScreen.searchEditText().perform(typeText("SFO"));
		SearchScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		SearchScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		SearchScreen.searchButton().perform(click());

		// Happy Path : First Item.
		assertViewIsDisplayedAtPosition(2, R.id.hotel_name);
		assertViewIsDisplayedAtPosition(2, R.id.price_per_night);
		assertViewIsDisplayedAtPosition(2, R.id.background);
		assertViewIsDisplayedAtPosition(2, R.id.star_rating_bar);
		assertViewIsDisplayedAtPosition(2, R.id.guest_rating);
		assertViewNotDisplayedAtPosition(2, R.id.discount_percentage);
		assertViewNotDisplayedAtPosition(2, R.id.strike_through_price);

		//test Top amenities
		assertViewWithTextIsDisplayedAtPosition(2, R.id.top_amenity, "Sponsored");
		assertViewWithTextIsDisplayedAtPosition(3, R.id.top_amenity, "Free cancellation");

		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(4));
		assertViewWithTextIsDisplayedAtPosition(4, R.id.top_amenity, "Book Now, Pay Later");

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
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(10));
		assertViewWithTextIsDisplayedAtPosition(10, R.id.urgency_message, "Sold Out");
	}

	@Test
	public void testFilterBtn() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);

		SearchScreen.searchEditText().perform(typeText("SFO"));
		SearchScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		SearchScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		SearchScreen.searchButton().perform(click());
		HotelScreen.mapFab().perform(click());
		onView(withId(R.id.filter_btn)).perform(click());
		onView(allOf(withId(R.id.hotel_filter_rating_four), isDescendantOfA(withId(R.id.hotel_filter_view)))).perform(click());
		onView(allOf(withId(R.id.hotel_filter_rating_two), isDescendantOfA(withId(R.id.hotel_filter_view)))).perform(click());
		pressBack();
		assertViewWithTextIsDisplayed(R.id.filter_count_text, "2");
		onView(allOf(withId(R.id.filter_text), isDescendantOfA(withId(R.id.filter_btn)))).check(matches(isDisplayed()));
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
