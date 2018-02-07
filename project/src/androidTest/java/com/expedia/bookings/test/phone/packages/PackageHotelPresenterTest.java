package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.not;

public class PackageHotelPresenterTest extends PackageTestCase {

	@Test
	public void testHotelBundleSlidingTransition() throws Throwable {
		PackageScreen.searchPackage();

		// results to bundle overview
		assertSlidingBundleWidgetNoSelection(true);

		//details to bundle overview
		HotelResultsScreen.selectHotel("Package Happy Path");
		PackageScreen.hotelDetailsToolbar().perform(ViewActions.waitForViewToDisplay());
		PackageScreen.hotelDetailsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Package Happy Path")))));
		PackageScreen.bundlePriceWidget().check(matches(not(isDisplayed())));
	}

	private static void assertSlidingBundleWidgetNoSelection(boolean isFromResults) {
		PackageScreen.bundlePriceWidget().perform(waitForViewToDisplay());
		PackageScreen.bundlePriceWidget().perform(click());
		PackageScreen.bundlePriceWidget().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Trip to Detroit, MI")))));
		PackageScreen.hotelInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Select hotel in Detroit")))));
		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Flight to San Francisco")))));
		PackageScreen.bundlePriceWidget().perform(click());

		if (isFromResults) {
			PackageScreen.resultsHeader().check(matches(isDisplayed()));
		}
		else {
			PackageScreen.hotelDetailsToolbar().check(matches(hasDescendant(
				CoreMatchers.allOf(isDisplayed(), withText("Package Happy Path")))));
		}
	}
}
