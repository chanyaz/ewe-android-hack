package com.expedia.bookings.test.ui.phone.tests.ui;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchActionBar;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

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
