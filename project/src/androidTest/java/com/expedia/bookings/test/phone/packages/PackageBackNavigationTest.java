package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;

public class PackageBackNavigationTest extends PackageTestCase {

	public void testPackageBackNavigation() throws Throwable {
		PackageScreen.searchPackage();
		HotelScreen.selectHotel("Package Happy Path");
		PackageScreen.selectRoom();
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
		onView(allOf(withId(R.id.bundle_total_price), withText("$3,863.38"))).perform(waitForViewToDisplay());

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

	public void testPackageBackNavigationAfterSelection() throws Throwable {
		PackageScreen.doPackageSearch();
		Common.pressBack();

		PackageScreen.errorDialog(
			"You are about to start your search over. If you want to modify your hotel or flight, tap the 'Edit' button on the top right.")
			.perform(waitForViewToDisplay());
		onView(withId(android.R.id.button1)).perform(click());
		SearchScreen.searchButton().perform(waitForViewToDisplay());
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
			.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.hotel_name_text_view,
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
