package com.expedia.bookings.test.phone.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.test.tablet.pagemodels.Common;
import com.expedia.bookings.test.espresso.CarTestCase;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.squareup.phrase.Phrase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;

public class CarSearchErrorTest extends CarTestCase {

	public void testSearchErrorProductNotAvailable() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		CarScreen.pickupLocation().perform(typeText("KTM"));
		CarScreen.selectPickupLocation("Kathmandu, Nepal");
		CarScreen.selectDateButton().perform(click());
		CarScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		CarScreen.searchButton().perform(click());

		screenshot("Car Search No Product");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.error_car_search_message));

		// Make sure the button works
		CarScreen.searchErrorWidgetButton().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.toolbar_search_cars));

		// Make sure the back button works
		CarScreen.searchButton().perform(click());
		Common.pressBack();
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.toolbar_search_cars));

		// Make sure the toolbar button works
		CarScreen.searchButton().perform(click());
		CarScreen.searchErrorToolbarBack().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.toolbar_search_cars));
	}

	public void testSearchErrorInvalidInput() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		CarScreen.pickupLocation().perform(typeText("DTW"));
		CarScreen.selectPickupLocation("Detroit, MI");
		CarScreen.selectDateButton().perform(click());
		CarScreen.selectDates(startDateTime.toLocalDate(), startDateTime.toLocalDate());
		CarScreen.searchButton().perform(click());

		screenshot("Car Search Invalid Input");
		EspressoUtils.assertViewWithTextIsDisplayed(Phrase.from(getActivity(), R.string.error_server_TEMPLATE)
			.put("brand", BuildConfig.brand).format().toString());
	}
}
