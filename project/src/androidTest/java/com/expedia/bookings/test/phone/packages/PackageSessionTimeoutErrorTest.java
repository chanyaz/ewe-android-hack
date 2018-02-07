package com.expedia.bookings.test.phone.packages;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class PackageSessionTimeoutErrorTest extends PackageTestCase {
	@Test
	public void testPackageSessionTimeOut() throws Throwable {
		PackageScreen.searchPackage();
		Common.delay(1);
		Common.delay(1);
		HotelResultsScreen.selectHotel("Package Happy Path");
		Common.delay(1);
		HotelInfoSiteScreen.clickStickySelectRoom();
		Common.delay(1);
		HotelInfoSiteScreen.bookRoomType("session_expired");
		Common.delay(1);

		PackageScreen.assertErrorScreen("Search Again", "Still there? Your session has expired. Please try your search again.");
		onView(withId(R.id.error_action_button)).perform(click());
		SearchScreen.searchButton().check(matches(isDisplayed()));
	}
}
