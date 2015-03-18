package com.expedia.bookings.test.ui.phone.tests.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.cars.CarViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CarCreateTripErrorTests extends PhoneTestCase {

	private static final String CATEGORY = "Economy";

	public void testCarCreateTripWithPriceChange() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchCars();
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();

		screenshot("Car Search");
		CarViewModel.pickupLocation().perform(typeText("SFO"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), startDateTime.toLocalDate());
		CarViewModel.searchButton().perform(click());

		screenshot("Car Results");
		CarViewModel.selectCarCategory(CATEGORY);
		selectCarOffer(0);

		screenshot("Car Checkout With Price Change");
		EspressoUtils.assertViewWithTextIsDisplayed("Price changed from $32.50");
	}

	public void testCarCreateTripFailure() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchCars();
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();

		screenshot("Car Search");
		CarViewModel.pickupLocation().perform(typeText("SFO"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), startDateTime.toLocalDate());
		CarViewModel.searchButton().perform(click());

		screenshot("Car Results");
		CarViewModel.selectCarCategory(CATEGORY);
		selectCarOffer(1);

		screenshot("Car Create Trip Failure Dialog");
		CarViewModel.alertDialog().check(matches(isDisplayed()));
		CarViewModel.alertDialogMessage().check(matches(withText(R.string.oops)));
		CarViewModel.alertDialogNeutralButton().perform(click());

		screenshot("Car Search");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.dates_and_location));
	}

	private void selectCarOffer(int carOfferNum) throws Throwable {
		screenshot("Car Offers");
		//Selecting an already expanded offer opens google maps
		if (carOfferNum != 0) {
			CarViewModel.expandCarOffer(carOfferNum);
		}
		CarViewModel.selectCarOffer();
		ScreenActions.delay(1);
	}

}
