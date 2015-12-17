package com.expedia.bookings.test.phone.newhotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;

import org.hamcrest.CoreMatchers;

import android.support.test.espresso.action.ViewActions;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class HotelResultsCarouselTest extends HotelTestCase {

	public void testHotelResultsCarousel() throws Throwable {
		HotelScreen.doGenericSearch();
		HotelScreen.hotelResultsList().perform(ViewActions.swipeUp());
		Common.delay(2);
		HotelScreen.mapFab().perform(ViewActions.click());
		Common.delay(1);
		HotelScreen.hotelResultsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(
				isDisplayed(), withText("San Francisco, CA (SFO-San Francisco Intl.)")))));
		assertViewWithTextIsDisplayedAtPosition(0, R.id.hotel_preview_text, "happypath");
		assertViewIsDisplayedAtPosition(0, R.id.hotel_preview_star_rating);
		assertViewIsDisplayedAtPosition(0, R.id.hotel_guest_rating);
		HotelScreen.hotelCarousel().perform(ViewActions.swipeLeft());
		Common.delay(2);
		assertViewWithTextIsDisplayedAtPosition(1, R.id.hotel_preview_text, "hotel_price_change");
		assertViewIsDisplayedAtPosition(1, R.id.hotel_preview_star_rating);
		assertViewIsDisplayedAtPosition(1, R.id.hotel_guest_rating);
	}

	private void assertViewIsDisplayedAtPosition(int position, int id) {
		HotelScreen.hotelCarousel().check(
				RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
						CoreMatchers.allOf(withId(id), isDisplayed()))));
	}

	private void assertViewWithTextIsDisplayedAtPosition(int position, int id, String text) {
		HotelScreen.hotelCarousel().check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), isDisplayed(), withText(text)))));
	}
}
