package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class PackageBackNavigationTest extends PackageTestCase {

	public void testPackageBackNavigation() throws Throwable {
		PackageScreen.doPackageSearch();

		//back to inbound flight overview
		Common.pressBack();
		assertInboundFlightBundlePrice("$4,212");
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Flight to San Francisco, CA")))));

		//back to inbound flight results
		Common.pressBack();
		onView(withId(R.id.all_flights_header)).check(matches(isDisplayed()));
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Select return flight")))));

		Common.pressBack();

		//back to outbound flight overview
		Common.pressBack();
		assertOutboundFlightBundlePrice("$3,864");
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Flight to Detroit, MI")))));

		//back to outbound flight results
		Common.pressBack();
		onView(withId(R.id.all_flights_header)).check(matches(isDisplayed()));
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Select flight to Detroit, MI")))));

		Common.pressBack();

		//back to hotel infosite
		Common.pressBack();
		PackageScreen.hotelDetailsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Package Happy Path")))));

		//back to hotel results
		Common.pressBack();
		assertHotelSRP();

		Common.pressBack();

		//back to search
		Common.pressBack();
		SearchScreen.searchButton().check(matches(isDisplayed()));
	}

	private void assertOutboundFlightBundlePrice(String price) {
		onView(allOf(withId(R.id.bundle_price_label), withText("Bundle Total"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_price), withText(price + "/person"))).check(matches(isDisplayed()));
	}

	private void assertInboundFlightBundlePrice(String price) {
		onView(allOf(withId(R.id.bundle_price_label), withText("Bundle Total"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_price), withText(price + "/person"))).check(matches(isDisplayed()));
	}

	private void assertHotelSRP() {
		HotelScreen.hotelResultsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Hotels in Detroit, MI")))));
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.hotel_name_text_view,
			"Package Happy Path");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.strike_through_price, "$538");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.price_per_night, "$526");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.unreal_deal_message, "Book this and save $110 (22%)");
	}
}
