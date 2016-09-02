package com.expedia.bookings.test.phone.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.TripsScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;

public class HotelSwPTest extends PhoneTestCase {

	public void testUserWithSwpToggleOffSignsOut() throws Throwable {
		goToCheckout(true);

		HotelScreen.clickSignOut();
		Common.delay(1);
		Common.pressBack();

		assertViewIsDisplayed(R.id.widget_hotel_detail);
	}

	public void testUserWithSwpToggleOnSignsOut() throws Throwable {
		goToCheckout(false);

		HotelScreen.clickSignOut();
		Common.pressBack();

		assertViewIsDisplayed(R.id.widget_hotel_search);
	}

	private void goToCheckout(boolean clickSwP) throws Throwable {
		NewLaunchScreen.tripsButton().perform(click());
		TripsScreen.clickOnLogInButton();
		HotelScreen.signIn("goldstatus@mobiata.com");
		NewLaunchScreen.shopButton().perform(click());
		NewLaunchScreen.hotelsLaunchButton().perform(click());
		if (clickSwP) {
			SearchScreen.doGenericHotelSearchWithSwp();
		}
		else {
			SearchScreen.doGenericHotelSearch();
		}
		HotelScreen.selectHotel("happypath");
		HotelScreen.selectRoom();
	}

}
