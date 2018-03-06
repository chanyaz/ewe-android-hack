package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelCheckoutScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.test.stepdefs.phone.HomeScreenSteps;
import com.expedia.bookings.test.support.User;

import static android.support.test.espresso.action.ViewActions.click;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;

public class HotelSwPTest extends PhoneTestCase {

	@Test
	public void testUserWithSwpToggleOffSignsOut() throws Throwable {
		goToCheckout(true);

		HotelCheckoutScreen.clickSignOut();
		Common.delay(1);
		Common.pressBack();

		assertViewIsDisplayed(R.id.widget_hotel_detail);
	}

	@Test
	public void testUserWithSwpToggleOnSignsOut() throws Throwable {
		goToCheckout(false);

		HotelCheckoutScreen.clickSignOut();
		Common.pressBack();

		assertViewIsDisplayed(R.id.widget_hotel_search);
	}

	private static void goToCheckout(boolean clickSwP) throws Throwable {
		LaunchScreen.waitForLOBHeaderToBeDisplayed();
		HomeScreenSteps.switchToTab("Account");
		HomeScreenSteps.logInToTheApp(new User("goldstatus@mobiata.com", "password", "expedia"));
		LaunchScreen.shopButton().perform(click());
		LaunchScreen.hotelsLaunchButton().perform(click());
		if (clickSwP) {
			SearchScreen.doGenericHotelSearchWithSwp();
		}
		else {
			SearchScreen.doGenericHotelSearch();
		}
		HotelResultsScreen.selectHotel("happypath");
		HotelInfoSiteScreen.bookFirstRoom();
	}
}
