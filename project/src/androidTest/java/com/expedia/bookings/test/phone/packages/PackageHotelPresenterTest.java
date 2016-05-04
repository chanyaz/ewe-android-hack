package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;

import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.hotels.HotelScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;

public class PackageHotelPresenterTest extends PackageTestCase {

	public void testHotelBundleSlidingTransition() throws Throwable {
		PackageScreen.searchPackage();

		// results to bundle overview
		assertBundleOverview();

		//details to bundle overview
		HotelScreen.selectHotel("Package Happy Path");
		PackageScreen.hotelDetailsToolbar().perform(ViewActions.waitForViewToDisplay());
		PackageScreen.hotelDetailsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Package Happy Path")))));
		assertBundleOverview();
	}

	private static void assertBundleOverview() {
		PackageScreen.hotelPriceWidget().perform(click());
		PackageScreen.hotelInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Select hotel in Detroit")))));

		PackageScreen.hotelPriceWidget().perform(waitForViewToDisplay());
		PackageScreen.hotelPriceWidget().perform(click());
	}
}
