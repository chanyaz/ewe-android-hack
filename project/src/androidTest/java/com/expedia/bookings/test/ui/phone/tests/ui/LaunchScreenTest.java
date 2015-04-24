package com.expedia.bookings.test.ui.phone.tests.ui;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.component.lx.LXViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by dmadan on 11/17/14.
 */
public class LaunchScreenTest extends PhoneTestCase {

	private static final String TAG = LaunchScreenTest.class.getName();

	/*
	*  #164 eb_tp test for launcher screen general UI elements in phone.
	*/

	@Override
	public void runTest() throws Throwable {
		String testMethodName = getClass().getMethod(getName(), (Class[]) null).toString();
		if (testMethodName.contains("UnitedKingdom")) {
			setPOS(PointOfSaleId.UNITED_KINGDOM);
		}
		else if (testMethodName.contains("Germany")) {
			setPOS(PointOfSaleId.GERMANY);
		}
		else if (testMethodName.contains("Canada")) {
			setPOS(PointOfSaleId.CANADA);
		}
		else if (testMethodName.contains("Japan")) {
			setPOS(PointOfSaleId.JAPAN);
		}
		else if (testMethodName.contains("France")) {
			setPOS(PointOfSaleId.FRANCE);
		}
		else if (testMethodName.contains("USA")) {
			setPOS(PointOfSaleId.UNITED_STATES);
		}
		super.runTest();
	}

	public void testGeneralUIElements() throws Throwable {
		setPOS(PointOfSaleId.UNITED_STATES);

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
		LXViewModel.searchButtonInSRPToolbar().perform(click());
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

	public void testNoFlightSupportVietnamPOS() throws Throwable {
		setPOS(PointOfSaleId.VIETNAM);
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_flights_pos));
		Common.pressBack();
	}

	public void testNoFlightSupportIndiaPOS() throws Throwable {
		setPOS(PointOfSaleId.INDIA);
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_flights_pos));
	}

	public void testDropDownFlightSupportKoreaPOS() throws Throwable {
		setPOS(PointOfSaleId.SOUTH_KOREA);
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewIsDisplayed(R.id.departure_airport_spinner);
	}

	public void testDropDownFlightSupportIndonesiaPOS() throws Throwable {
		setPOS(PointOfSaleId.INDONESIA);
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewIsDisplayed(R.id.departure_airport_spinner);
	}

	public void testDropDownFlightSupportTaiwanPOS() throws Throwable {
		setPOS(PointOfSaleId.TAIWAN);
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewIsDisplayed(R.id.departure_airport_spinner);
	}

	public void testDropDownFlightSupportPhilippinesPOS() throws Throwable {
		setPOS(PointOfSaleId.PHILIPPINES);
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewIsDisplayed(R.id.departure_airport_spinner);
	}

	public void testNoCarLXSupportUnitedKingdomPOS() throws Throwable {
		LaunchScreen.checkCarsButtonNotDisplayed();
		LaunchScreen.checkLXButtonNotDisplayed();
	}

	public void testNoCarLXSupportGermanyPOS() throws Throwable {
		LaunchScreen.checkCarsButtonNotDisplayed();
		LaunchScreen.checkLXButtonNotDisplayed();
	}

	public void testNoCarLXSupportCanadaPOS() throws Throwable {
		LaunchScreen.checkCarsButtonNotDisplayed();
		LaunchScreen.checkLXButtonNotDisplayed();
	}

	public void testNoCarLXSupportJapanPOS() throws Throwable {
		LaunchScreen.checkCarsButtonNotDisplayed();
		LaunchScreen.checkLXButtonNotDisplayed();
	}

	public void testNoCarLXSupportFrancePOS() throws Throwable {
		LaunchScreen.checkCarsButtonNotDisplayed();
		LaunchScreen.checkLXButtonNotDisplayed();
	}

	public void testCarLXSupportUSAPOS() throws Throwable {
		// Let's recheck cars for US POS
		onView(allOf(withId(R.id.cars_button), isDescendantOfA(withId(R.id.double_row_lob_selector)))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.activities_button), isDescendantOfA(withId(R.id.double_row_lob_selector)))).check(matches(isDisplayed()));
	}

	@Override
	protected void tearDown() throws Exception {
		setPOS(PointOfSaleId.UNITED_STATES);
		super.tearDown();
	}
}
