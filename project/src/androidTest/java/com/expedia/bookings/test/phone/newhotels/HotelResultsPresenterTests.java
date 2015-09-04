package com.expedia.bookings.test.phone.newhotels;

import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.IsNot.not;

public class HotelResultsPresenterTests extends HotelTestCase {

	@Test
	public void testSearchResults() throws Throwable {
		doSearch();

		// Happy Path : First Item.
		assertViewIsDisplayedAtPosition(1, R.id.hotel_name_text_view);
		assertViewIsDisplayedAtPosition(1, R.id.price_per_night);
		assertViewIsDisplayedAtPosition(1, R.id.background);
		assertViewIsDisplayedAtPosition(1, R.id.hotel_rating_bar);
		assertViewIsDisplayedAtPosition(1, R.id.guest_rating_percentage);
		assertViewNotDisplayedAtPosition(1, R.id.discount_percentage);
		assertViewNotDisplayedAtPosition(1, R.id.strike_through_price);
	}

	private void doSearch() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA");
		HotelScreen.selectDateButton().perform(click());
		HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		HotelScreen.searchButton().perform(click());
		HotelScreen.waitForResultsDisplayed();
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
}
