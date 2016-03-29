package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class PackageHotelDetailsTest extends PackageTestCase {

	public void testHideStandaloneHotelPricing() throws Throwable {
		PackageScreen.searchPackage();
		Common.delay(1);

		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(3);

		EspressoUtils.assertViewWithSiblingIsNotDisplayed(R.id.discount_percentage, R.id.air_attach_swp_image_details);
		EspressoUtils.assertViewIsNotDisplayed(R.id.hotel_price_container);
		EspressoUtils.assertViewIsDisplayed(R.id.search_dates_info);

		HotelScreen.selectRoomButton().perform(click());
		onView(allOf(withId(R.id.daily_price_per_night), withText("+$0"))).check(
			matches(isDisplayed()));
		onView(allOf(withId(R.id.per_night),
			hasSibling(allOf(withId(R.id.daily_price_per_night), withText("+$0"))))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.per_night),
			hasSibling(allOf(withId(R.id.daily_price_per_night), withText("-$3.21"))))).check(
			matches(isDisplayed()));
	}

	public void testHotelDetailsToolbarText() throws Throwable {
		PackageScreen.searchPackageFor(2, 1);
		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(1);

		PackageScreen.hotelDetailsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Package Happy Path")))));

		onView(allOf(withId(R.id.hotel_search_info), withText("1 Room, 3 Guests")))
			.check(matches(isDisplayed()));
	}
}
