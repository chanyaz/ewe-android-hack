package com.expedia.bookings.test.ui.phone.tests.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.test.component.cars.CarViewModel;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.utils.CarTestCase;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.squareup.phrase.Phrase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;

public class CarSearchErrorTests extends CarTestCase {

	public void testSearchErrorProductNotAvailable() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		CarViewModel.pickupLocation().perform(typeText("KTM"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "Kathmandu, Nepal");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		CarViewModel.searchButton().perform(click());

		screenshot("Car Search No Product");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.error_car_search_message));

		// Make sure the button works
		CarViewModel.searchErrorWidgetButton().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.toolbar_search_cars));

		// Make sure the back button works
		CarViewModel.searchButton().perform(click());
		Common.pressBack();
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.toolbar_search_cars));

		// Make sure the toolbar button works
		CarViewModel.searchButton().perform(click());
		CarViewModel.searchErrorToolbarBack().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.toolbar_search_cars));
	}

	public void testSearchErrorInvalidInput() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		CarViewModel.pickupLocation().perform(typeText("DTW"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "Detroit, MI");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), startDateTime.toLocalDate());
		CarViewModel.searchButton().perform(click());

		screenshot("Car Search Invalid Input");
		EspressoUtils.assertViewWithTextIsDisplayed(Phrase.from(getActivity(), R.string.error_server_TEMPLATE)
			.put("brand", BuildConfig.brand).format().toString());
	}
}
