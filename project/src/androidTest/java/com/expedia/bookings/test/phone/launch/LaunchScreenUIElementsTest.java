package com.expedia.bookings.test.phone.launch;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.cars.CarScreen;
import com.expedia.bookings.test.phone.lx.LXScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.espresso.Common;
import com.mobiata.android.Log;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class LaunchScreenUIElementsTest extends PhoneTestCase {
	private static final String TAG = LaunchScreenUIElementsTest.class.getName();

	@Override
	public void runTest() throws Throwable {
		Common.setPOS(PointOfSaleId.UNITED_STATES);
		String testMethodName = getClass().getMethod(getName(), (Class[]) null).toString();
		if (testMethodName.contains("testLaunchButtonsForGroundTransport")) {
			AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppSplitGTandActivities,
				AbacusUtils.DefaultVariate.BUCKETED.ordinal());
		}
		super.runTest();
	}

	public void testGeneralUIElements() throws Throwable {
		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.hotels_button, mRes.getString(R.string.nav_hotels));
		LaunchScreen.launchHotels();
		screenshot("POS_US_Hotels_Launch");
		EspressoUtils.assertViewIsDisplayed(R.id.hotel_list_container);
		Common.pressBack();
		Log.v(TAG, "Hotels button on Launch screen is displayed and works");

		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.flights_button, mRes.getString(R.string.nav_flights));
		LaunchScreen.launchFlights();
		screenshot("POS_US_Flights_Launch");
		EspressoUtils.assertViewIsDisplayed(R.id.departure_airport_edit_text);
		Common.closeSoftKeyboard(FlightsSearchScreen.arrivalEditText());
		Common.delay(2);
		FlightsSearchScreen.actionBarUp().perform(click());
		Log.v(TAG, "Flights button on Launch screen is displayed and works");

		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.cars_button, mRes.getString(R.string.nav_cars));
		LaunchScreen.launchCars();
		screenshot("POS_US_Cars_Launch");
		EspressoUtils.assertViewIsDisplayed(R.id.pickup_location);
		Common.closeSoftKeyboard(onView(withId(R.id.pickup_location)));
		CarScreen.searchWidgetToolbarBack().perform(click());
		Log.v(TAG, "Cars button on Launch screen is displayed and works");

		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.activities_button, mRes.getString(R.string.nav_lx));
		LaunchScreen.launchActivities();
		screenshot("POS_US_Activities_Launch");
		LXScreen.searchButtonInSRPToolbar().perform(click());
		screenshot("POS_US_Activities_Launch_Edit_Search");
		EspressoUtils.assertViewIsDisplayed(R.id.search_location);
		Common.closeSoftKeyboard(onView(withId(R.id.search_location)));
		LXScreen.searchWidgetToolbarNavigation().perform(click());
		LXScreen.resultsPresenterToolbarNavigation().perform(click());
		Log.v(TAG, "LX button on Launch screen is displayed and works");

		LaunchScreen.tripsButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.login_button);
		Log.v(TAG, "Trips button on Launch screen is displayed and works");

		LaunchScreen.shopButton().perform(click());
		Log.v(TAG, "Shop button on Launch screen is displayed ");
	}

	public void testLaunchButtonsForGroundTransport() throws Throwable {
		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.activities_button, mRes.getString(R.string.nav_lx));
		LaunchScreen.launchActivities();
		screenshot("POS_US_Activities_Launch");
		LXScreen.searchButtonInSRPToolbar().perform(click());
		screenshot("POS_US_Activities_Launch_Edit_Search");
		EspressoUtils.assertViewIsDisplayed(R.id.search_location);
		Common.closeSoftKeyboard(onView(withId(R.id.search_location)));
		LXScreen.searchWidgetToolbarNavigation().perform(click());
		LXScreen.resultsPresenterToolbarNavigation().perform(click());
		Log.v(TAG, "LX button on Launch screen is displayed and works");

		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.transport_button, mRes.getString(R.string.nav_transport));
		LaunchScreen.launchGroundTransport();
		screenshot("POS_US_GT_Launch");
		LXScreen.searchButtonInSRPToolbar().perform(click());
		screenshot("POS_US_GT_Launch_Edit_Search");
		EspressoUtils.assertViewIsDisplayed(R.id.search_location);
		Log.v(TAG, "GT button on Launch screen is displayed and works");
	}

	@Override
	protected void tearDown() throws Exception {
		Common.setPOS(PointOfSaleId.UNITED_STATES);
		super.tearDown();
	}

}
