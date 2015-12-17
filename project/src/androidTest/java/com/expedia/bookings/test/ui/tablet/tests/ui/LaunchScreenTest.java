package com.expedia.bookings.test.ui.tablet.tests.ui;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.Itin;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by dmadan on 1/13/15.
 */
public class LaunchScreenTest extends TabletTestCase {

	private static final String TAG = LaunchScreenTest.class.getName();

	/*
	*  #164 eb_tp test for launcher screen general UI elements in tablet.
	*/

	public void testGeneralUIElements() {

		EspressoUtils.assertViewIsDisplayed(R.id.fake_waypoint_edit_text);
		Common.enterLog(TAG, "Enter your destination edit box on Launch screen is displayed");

		EspressoUtils.assertViewIsDisplayed(android.R.id.home);
		Common.enterLog(TAG, "Expedia logo on Launch screen is displayed");

		Itin.clickTripsMenuButton();
		EspressoUtils.assertViewIsDisplayed(R.id.login_button);
		Common.pressBack();
		Common.enterLog(TAG, "Trips button on Launch screen is displayed and works");

	}

	public void testNoFlightSupportVietnamPOS() {
		setPOS(PointOfSaleId.VIETNAM);
		searchFlights();
		onView(allOf(withText(mRes.getString(R.string.invalid_flights_pos)), isDescendantOfA(withId(R.id.search_error_container)))).check(matches(isDisplayed()));
	}

	public void testNoFlightSupportIndiaPOS() {
		setPOS(PointOfSaleId.INDIA);
		searchFlights();
		onView(allOf(withText(mRes.getString(R.string.invalid_flights_pos)), isDescendantOfA(withId(R.id.search_error_container)))).check(matches(isDisplayed()));
	}

	// for these POS we show the web link to book the flights on tablets
	public void testNoFlightSupportKoreaPOS() {
		setPOS(PointOfSaleId.SOUTH_KOREA);
		searchFlights();
		onView(allOf(withText("We're sorry, but we don't support flights for your region yet. However, they are available on our website."), isDescendantOfA(withId(R.id.search_error_container)))).check(matches(isDisplayed()));
	}

	public void testNoFlightSupportIndonesiaPOS() {
		setPOS(PointOfSaleId.INDONESIA);
		searchFlights();
		onView(allOf(withText("We're sorry, but we don't support flights for your region yet. However, they are available on our website."), isDescendantOfA(withId(R.id.search_error_container)))).check(matches(isDisplayed()));
	}

	public void testNoFlightSupportTaiwanPOS() {
		setPOS(PointOfSaleId.TAIWAN);
		searchFlights();
		onView(allOf(withText("We're sorry, but we don't support flights for your region yet. However, they are available on our website."), isDescendantOfA(withId(R.id.search_error_container)))).check(matches(isDisplayed()));
	}

	public void testNoFlightSupportPhilippinesPOS() {
		setPOS(PointOfSaleId.PHILIPPINES);
		searchFlights();
		onView(allOf(withText("We're sorry, but we don't support flights for your region yet. However, they are available on our website."), isDescendantOfA(withId(R.id.search_error_container)))).check(matches(isDisplayed()));
	}

	public void searchFlights() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
	}
}
