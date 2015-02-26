package com.expedia.bookings.test.ui.phone.tests.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.cars.CarViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;

public class CarSearchErrorTests extends PhoneTestCase {

	public void testSearchErrorProductNotAvailable() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchCars();
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);

		screenshot("Car_Search");
		CarViewModel.pickupLocation().perform(typeText("KTM"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "Kathmandu, Nepal");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		CarViewModel.searchButton().perform(click());

		screenshot("Car Search No Product");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.error_car_search_message));
	}

	public void testSearchErrorInvalidInput() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchCars();
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();

		screenshot("Car Search");
		CarViewModel.pickupLocation().perform(typeText("DTW"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "Detroit, MI");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), startDateTime.toLocalDate());
		CarViewModel.searchButton().perform(click());

		screenshot("Car Search Invalid Input");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.error_car_search_message));
	}

}
