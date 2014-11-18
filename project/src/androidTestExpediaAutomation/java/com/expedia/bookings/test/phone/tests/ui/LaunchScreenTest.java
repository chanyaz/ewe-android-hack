package com.expedia.bookings.test.phone.tests.ui;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchActionBar;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.tablet.pagemodels.Common;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.PhoneTestCase;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 11/17/14.
 */
public class LaunchScreenTest extends PhoneTestCase {

	private static final String TAG = LaunchScreenTest.class.getName();

	/*
	*  #164 eb_tp test for launcher screen general UI elements in phone.
	*/

	public void testGeneralUIElements() {

		EspressoUtils.assertViewIsDisplayed(android.R.id.home);
		Common.enterLog(TAG, "Expedia logo on Launch screen is displayed");

		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.nav_hotels));
		LaunchScreen.launchHotels();
		EspressoUtils.assertViewIsDisplayed(R.id.hotel_list_container);
		LaunchActionBar.clickActionBarHomeIcon();
		Common.enterLog(TAG, "Hotels button on Launch screen is displayed and works");

		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.nav_flights));
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewIsDisplayed(R.id.departure_airport_edit_text);
		LaunchActionBar.clickActionBarHomeIcon();
		Common.enterLog(TAG, "Flights button on Launch screen is displayed and works");

		LaunchScreen.pressTrips();
		EspressoUtils.assertViewIsDisplayed(R.id.login_button);
		Common.enterLog(TAG, "Trips button on Launch screen is displayed and works");

		LaunchScreen.pressShop();
		Common.enterLog(TAG, "Shop button on Launch screen is displayed ");
	}
}
