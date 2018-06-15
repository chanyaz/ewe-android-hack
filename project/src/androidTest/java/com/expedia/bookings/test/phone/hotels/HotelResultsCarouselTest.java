package com.expedia.bookings.test.phone.hotels;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.action.ViewActions;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelRate;
import com.expedia.bookings.hotel.widget.adapter.HotelMapCarouselAdapter;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.widget.HotelCarouselRecycler;

import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class HotelResultsCarouselTest {

	@Rule
	public PlaygroundRule mRule = new PlaygroundRule(R.layout.test_carousel_widget);

	@Before
	public void setUp() throws Throwable {
		final HotelCarouselRecycler hotelCarouselRecycler = (HotelCarouselRecycler) mRule.getRoot();
		mRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				hotelCarouselRecycler
					.setAdapter(new HotelMapCarouselAdapter(getMockHotelList(), false));
			}
		});
	}

	public List<Hotel> getMockHotelList() {
		ArrayList<Hotel> hotels = new ArrayList<>();
		for (int i = 1; i < 100; i++) {
			hotels.add(makeHotel(i));
		}
		return hotels;
	}

	public Hotel makeHotel(int index) {
		Hotel hotel = new Hotel();
		hotel.localizedName = "happy " + index;
		hotel.hotelId = "happy " + index;
		hotel.lowRateInfo = new HotelRate();
		hotel.distanceUnit = "Miles";
		hotel.lowRateInfo.currencyCode = "USD";
		hotel.hotelStarRating = index % 5;
		hotel.hotelGuestRating = index % 5;
		return hotel;
	}

	@Test
	public void testHotelResultsCarousel() throws Throwable {
		assertViewWithTextIsDisplayedAtPosition(0, R.id.hotel_preview_text, "happy 1");
		assertViewIsDisplayedAtPosition(0, R.id.hotel_preview_star_rating);
		assertViewIsDisplayedAtPosition(0, R.id.hotel_guest_rating);
		HotelResultsScreen.hotelCarousel().perform(ViewActions.swipeLeft());
		Common.delay(2);
		assertViewWithTextIsDisplayedAtPosition(1, R.id.hotel_preview_text, "happy 2");
		assertViewIsDisplayedAtPosition(1, R.id.hotel_preview_star_rating);
		assertViewIsDisplayedAtPosition(1, R.id.hotel_guest_rating);
	}

	private void assertViewIsDisplayedAtPosition(int position, int id) {
		HotelResultsScreen.hotelCarousel().check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), isDisplayed()))));
	}

	private void assertViewWithTextIsDisplayedAtPosition(int position, int id, String text) {
		HotelResultsScreen.hotelCarousel().check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), isDisplayed(), withText(text)))));
	}
}
