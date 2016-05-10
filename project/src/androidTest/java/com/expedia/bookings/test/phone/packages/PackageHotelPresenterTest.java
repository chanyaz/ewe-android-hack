package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.hotels.HotelScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class PackageHotelPresenterTest extends PackageTestCase {

	public void testHotelBundleSlidingTransition() throws Throwable {
		PackageScreen.searchPackage();

		// results to bundle overview
		assertSlidingBundleWidgetNoSelection();

		//details to bundle overview
		HotelScreen.selectHotel("Package Happy Path");
		PackageScreen.hotelDetailsToolbar().perform(ViewActions.waitForViewToDisplay());
		PackageScreen.hotelDetailsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Package Happy Path")))));
		assertSlidingBundleWidgetNoSelection();
	}

	private static void assertSlidingBundleWidgetNoSelection() {
		PackageScreen.bundlePriceWidget().perform(waitForViewToDisplay());
		PackageScreen.bundlePriceWidget().perform(click());
		Common.delay(1);
		PackageScreen.bundlePriceWidget().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Trip to Detroit, MI")))));
		PackageScreen.hotelInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Select hotel in Detroit")))));

		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Flight to (SFO) San Francisco")))));
		PackageScreen.bundlePriceWidget().perform(click());
	}
}
