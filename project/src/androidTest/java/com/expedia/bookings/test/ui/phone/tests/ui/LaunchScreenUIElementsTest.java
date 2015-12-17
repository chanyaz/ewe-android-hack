package com.expedia.bookings.test.ui.phone.tests.ui;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.lx.LXScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by mswami on 7/14/15.
 */
public class LaunchScreenUIElementsTest extends PhoneTestCase {
	private static final String TAG = LaunchScreenUIElementsTest.class.getName();

	@Override
	public void runTest() throws Throwable {
		setPOS(PointOfSaleId.UNITED_STATES);
		super.runTest();
	}

	public void testGeneralUIElements() throws Throwable {
		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.hotels_button, mRes.getString(R.string.nav_hotels));
		LaunchScreen.launchHotels();
		screenshot("POS_US_Hotels_Launch");
		EspressoUtils.assertViewIsDisplayed(R.id.hotel_list_container);
		Common.pressBack();
		Common.enterLog(TAG, "Hotels button on Launch screen is displayed and works");

		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.flights_button, mRes.getString(R.string.nav_flights));
		LaunchScreen.launchFlights();
		screenshot("POS_US_Flights_Launch");
		EspressoUtils.assertViewIsDisplayed(R.id.departure_airport_edit_text);
		Common.closeSoftKeyboard(FlightsSearchScreen.arrivalEditText());
		ScreenActions.delay(2);
		Common.pressBack();
		Common.pressBack();
		Common.enterLog(TAG, "Flights button on Launch screen is displayed and works");

		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.cars_button, mRes.getString(R.string.nav_cars));
		LaunchScreen.launchCars();
		screenshot("POS_US_Cars_Launch");
		EspressoUtils.assertViewIsDisplayed(R.id.pickup_location);
		Common.closeSoftKeyboard(onView(withId(R.id.pickup_location)));
		Common.pressBack();
		Common.enterLog(TAG, "Cars button on Launch screen is displayed and works");

		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.activities_button, mRes.getString(R.string.nav_lx));
		LaunchScreen.launchActivities();
		screenshot("POS_US_Activities_Launch");
		LXScreen.searchButtonInSRPToolbar().perform(click());
		screenshot("POS_US_Activities_Launch_Edit_Search");
		EspressoUtils.assertViewIsDisplayed(R.id.search_location);
		Common.closeSoftKeyboard(onView(withId(R.id.search_location)));
		Common.pressBack();
		Common.pressBack();
		Common.enterLog(TAG, "LX button on Launch screen is displayed and works");

		LaunchScreen.tripsButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.login_button);
		Common.enterLog(TAG, "Trips button on Launch screen is displayed and works");

		LaunchScreen.shopButton().perform(click());
		Common.enterLog(TAG, "Shop button on Launch screen is displayed ");
	}

	@Override
	protected void tearDown() throws Exception {
		setPOS(PointOfSaleId.UNITED_STATES);
		super.tearDown();
	}

}
