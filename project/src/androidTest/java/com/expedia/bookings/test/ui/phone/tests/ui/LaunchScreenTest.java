package com.expedia.bookings.test.ui.phone.tests.ui;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 11/17/14.
 */
public class LaunchScreenTest extends PhoneTestCase {

	private static final String TAG = LaunchScreenTest.class.getName();

	/*
	*  #164 eb_tp test for launcher screen general UI elements in phone.
	*/

	public void testGeneralUIElements() {
		setPOS(PointOfSaleId.UNITED_STATES);

		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.hotels_button, mRes.getString(R.string.nav_hotels));
		LaunchScreen.launchHotels();
		EspressoUtils.assertViewIsDisplayed(R.id.hotel_list_container);
		Common.pressBack();
		Common.enterLog(TAG, "Hotels button on Launch screen is displayed and works");

		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.flights_button, mRes.getString(R.string.nav_flights));
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewIsDisplayed(R.id.departure_airport_edit_text);
		Common.closeSoftKeyboard(FlightsSearchScreen.arrivalEditText());
		Common.pressBack();
		Common.enterLog(TAG, "Flights button on Launch screen is displayed and works");

		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.cars_button, mRes.getString(R.string.nav_cars));
		LaunchScreen.launchCars();
		EspressoUtils.assertViewIsDisplayed(R.id.pickup_location);
		Common.closeSoftKeyboard(onView(withId(R.id.pickup_location)));
		Common.pressBack();
		Common.enterLog(TAG, "Cars button on Launch screen is displayed and works");

		LaunchScreen.tripsButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.login_button);
		Common.enterLog(TAG, "Trips button on Launch screen is displayed and works");

		LaunchScreen.shopButton().perform(click());
		Common.enterLog(TAG, "Shop button on Launch screen is displayed ");
	}

	public void testNoFlightSupportVietnamPOS() {
		setPOS(PointOfSaleId.VIETNAM);
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_flights_pos));
		Common.pressBack();
	}

	public void testNoFlightSupportIndiaPOS() {
		setPOS(PointOfSaleId.INDIA);
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_flights_pos));
	}

	public void testNoFlightSupportKoreaPOS() {
		setPOS(PointOfSaleId.SOUTH_KOREA);
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewIsDisplayed(R.id.departure_airport_spinner);
	}

	public void testNoFlightSupportIndonesiaPOS() {
		setPOS(PointOfSaleId.INDONESIA);
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewIsDisplayed(R.id.departure_airport_spinner);
	}

	public void testNoFlightSupportTaiwanPOS() {
		setPOS(PointOfSaleId.TAIWAN);
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewIsDisplayed(R.id.departure_airport_spinner);
	}

	public void testNoFlightSupportPhilippinesPOS() {
		setPOS(PointOfSaleId.PHILIPPINES);
		LaunchScreen.launchFlights();
		EspressoUtils.assertViewIsDisplayed(R.id.departure_airport_spinner);
	}
}
