package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.TestValues;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;

public class PackageBackNavigationTest extends PackageTestCase {

	@Test
	public void testPackageBackNavigation() throws Throwable {
		PackageScreen.searchPackage();
		HotelScreen.selectHotel("Package Happy Path");
		PackageScreen.selectFirstRoom();
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		PackageScreen.selectFlight(0);

		//back to inbound flight results
		Common.pressBack();
		onView(withId(R.id.all_flights_header)).perform(waitForViewToDisplay());
		PackageScreen.flightsToolbar()
			.check(matches(hasDescendant(allOf(isDisplayed(), withText("Select return flight")))));
		PackageScreen.flightsToolbarSearchMenu().check(doesNotExist());
		PackageScreen.checkFlightToolBarMenuItemsVisibility(true);
		assertInboundFlightResultBundlePrice("$2,105.95");

		Common.pressBack();
		onView(allOf(withId(R.id.bundle_total_price), withText("$4,211.90"))).perform(waitForViewToDisplay());

		//back to outbound flight overview
		Common.pressBack();
		assertOutboundFlightBundlePrice("$1,932");
		PackageScreen.flightsToolbar()
			.check(matches(hasDescendant(allOf(isDisplayed(), withText("Flight to Detroit, MI")))));
		PackageScreen.checkFlightToolBarMenuItemsVisibility(false);


		//back to outbound flight results
		Common.pressBack();
		onView(withId(R.id.all_flights_header)).perform(waitForViewToDisplay());
		PackageScreen.flightsToolbar()
			.check(matches(hasDescendant(allOf(isDisplayed(), withText("Select flight to Detroit, MI")))));
		PackageScreen.checkFlightToolBarMenuItemsVisibility(true);
		assertOutboundFlightResultBundlePrice("$1,931.69");

		Common.pressBack();
		onView(allOf(withId(R.id.bundle_total_price), withText("$2,054.67"))).perform(waitForViewToDisplay());

		//back to hotel infosite
		Common.pressBack();
		PackageScreen.hotelDetailsToolbar().perform(waitForViewToDisplay());
		PackageScreen.hotelDetailsToolbar()
			.check(matches(hasDescendant(allOf(isDisplayed(), withText("Package Happy Path")))));

		//back to hotel results
		Common.pressBack();
		assertHotelSRP();

		Common.pressBack();
		onView(allOf(withId(R.id.bundle_total_price), withText("$0.00"))).perform(waitForViewToDisplay());

		//back to search
		Common.pressBack();
		SearchScreen.searchButton().perform(waitForViewToDisplay());
	}

	@Test
	public void testPackageBackNavigationAfterSelection() throws Throwable {
		PackageScreen.doPackageSearch();
		PackageScreen.outboundFlightInfoRowContainer().perform(click());

		Common.pressBack();

		PackageScreen.errorDialog(
			"You are about to start your search over. If you want to modify your hotel or flight, tap the 'Edit' button on the top right.")
			.perform(waitForViewToDisplay());
		onView(withId(android.R.id.button1)).perform(click());
		SearchScreen.searchButton().perform(waitForViewToDisplay());
		SearchScreen.searchButton().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.hotel_name,
			"Package Happy Path");
		Common.pressBack();
		onView(allOf(withId(R.id.widget_bundle_overview))).perform(ViewActions.waitForViewToDisplay());
		onView(allOf(withId(R.id.widget_bundle_overview))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.travel_info_view_text), hasSibling(withText("Flight to Detroit")))).check(matches(isDisplayed()));
		Common.pressBack();
		onView(withId(R.id.origin_card)).perform(click());
		SearchScreen.searchEditText().perform(typeText(TestValues.TYPE_TEXT_DTW));
		SearchScreen.selectLocation(TestValues.ORIGIN_LOCATION_DTW);
		//Delay from the auto advance anim
		Common.delay(1);
		onView(withId(R.id.destination_card)).perform(click());
		SearchScreen.searchEditText().perform(ViewActions.waitForViewToDisplay());
		SearchScreen.searchEditText().perform(typeText(TestValues.TYPE_TEXT_SFO));
		SearchScreen.selectLocation(TestValues.DESTINATION_LOCATION_SFO);
		SearchScreen.searchButton().perform(click());
		Common.pressBack();
		onView(allOf(withId(R.id.checkout_toolbar), hasDescendant(withText("Trip to San Francisco, CA")))).check(matches(isDisplayed()));

	}

	private void assertOutboundFlightBundlePrice(String price) {
		onView(allOf(withId(R.id.bundle_price_label), withText("Bundle Total"))).perform(waitForViewToDisplay());
		onView(allOf(withId(R.id.bundle_price), withText(price + "/person"))).perform(waitForViewToDisplay());
	}

	private void assertOutboundFlightResultBundlePrice(String price) {
		onView(allOf(withId(R.id.bundle_total_price), withText(price))).perform(waitForViewToDisplay());
	}

	private void assertInboundFlightResultBundlePrice(String price) {
		onView(allOf(withId(R.id.bundle_total_price), withText(price))).perform(waitForViewToDisplay());
	}

	private void assertHotelSRP() {
		PackageScreen.hotelResultsToolbar().perform(waitForViewToDisplay());
		HotelScreen.hotelResultsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Hotels in Detroit, MI")))));
		EspressoUtils
			.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.hotel_name,
				"Package Happy Path");
		EspressoUtils
			.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.strike_through_price,
				"$538");
		EspressoUtils
			.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.price_per_night, "$526");
		EspressoUtils
			.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.unreal_deal_message,
				"Book this and save $110 (22%)");
	}

}
