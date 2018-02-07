package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class PackageFlightPresenterTest extends PackageTestCase {

	@Test
	public void testFlightBundleSlidingTransition() throws Throwable {
		PackageScreen.searchPackage();

		//select hotel
		HotelResultsScreen.selectHotel("Package Happy Path");
		Common.delay(1);
		PackageScreen.hotelDetailsToolbar().perform(ViewActions.waitForViewToDisplay());
		PackageScreen.hotelDetailsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Package Happy Path")))));
		HotelInfoSiteScreen.bookFirstRoom();

		//select outbound
		assertSlidingBundleWidgetHotelSelected();
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());

		//select inbound
		assertSlidingBundleWidgetOutboundSelected();
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
	}

	private static void assertSlidingBundleWidgetHotelSelected() {
		clickViewBundle();
		assertHotelSelection();

		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Select flight to Detroit")))));
		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Flight to San Francisco")))));
		PackageScreen.bundlePriceWidget().perform(click());
	}

	private static void assertSlidingBundleWidgetOutboundSelected() {
		clickViewBundle();
		assertHotelSelection();
		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Flight to (DTW) Detroit")))));

		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Select flight to San Francisco")))));
		PackageScreen.bundlePriceWidget().perform(click());
	}

	private static void assertHotelSelection() {
		PackageScreen.bundlePriceWidget().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Trip to Detroit, MI")))));
		PackageScreen.hotelInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Package Happy Path")))));
	}

	private static void clickViewBundle() {
		PackageScreen.bundlePriceWidget().perform(waitForViewToDisplay());
		PackageScreen.bundlePriceWidget().perform(click());
		Common.delay(1);
	}
}
