package com.expedia.bookings.test.ui.phone.tests.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.cars.CarViewModel;
import com.expedia.bookings.test.ui.utils.CarTestCase;
import com.expedia.bookings.test.ui.utils.EspressoUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CarCreateTripErrorTests extends CarTestCase {

	private static final String CATEGORY = "Economy";

	public void testCarCreateTripWithPriceChange() throws Throwable {
		screenshot("Car Search");
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		CarViewModel.pickupLocation().perform(typeText("SFO"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), startDateTime.toLocalDate());
		CarViewModel.searchButton().perform(click());

		screenshot("Car Results");
		CarViewModel.selectCarCategory(CATEGORY);
		screenshot("Car Offers");
		CarViewModel.selectCarOffer(0);

		screenshot("Car Checkout With Price Change");
		EspressoUtils.assertViewWithTextIsDisplayed("Price changed from $32.50");
	}

	public void testCarCreateTripFailure() throws Throwable {
		screenshot("Car Search");
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		CarViewModel.pickupLocation().perform(typeText("SFO"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), startDateTime.toLocalDate());
		CarViewModel.searchButton().perform(click());

		screenshot("Car Results");
		CarViewModel.selectCarCategory(CATEGORY);
		screenshot("Car Offers");
		CarViewModel.selectCarOffer(1);

		screenshot("Car Create Trip Failure Dialog");
		CarViewModel.alertDialog().check(matches(isDisplayed()));
		CarViewModel.alertDialogMessage().check(matches(withText(R.string.error_server)));
		CarViewModel.alertDialogPositiveButton().perform(click());

		screenshot("Car Search");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.toolbar_search_cars));
	}

}
