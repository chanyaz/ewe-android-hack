package com.expedia.bookings.test.phone.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.TripsScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;

public class HotelSwPTest extends PhoneTestCase {

	boolean isUserBucketedSearchScreenTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelsSearchScreenTest);

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

		assertViewIsDisplayed(
			isUserBucketedSearchScreenTest ? R.id.widget_hotel_search_v2 : R.id.widget_hotel_search_v1);
	}

	private void goToCheckout(boolean clickSwP) throws Throwable {
		LaunchScreen.tripsButton().perform(click());
		TripsScreen.clickOnLogInButton();
		HotelScreen.signIn("goldstatus@mobiata.com");
		LaunchScreen.shopButton().perform(click());
		LaunchScreen.launchHotels();
		HotelScreen.doSearchWithSwPVisible(clickSwP);
		HotelScreen.selectHotel("happypath");
		HotelScreen.selectRoom();
	}

}
