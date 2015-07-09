package com.expedia.bookings.test.phone.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.CarTestCase;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.squareup.phrase.Phrase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CarCreateTripErrorTests extends CarTestCase {

	private static final String CATEGORY = "Economy";

	public void testCarCreateTripWithPriceChange() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		CarScreen.pickupLocation().perform(typeText("SFO"));
		CarScreen.selectPickupLocation("San Francisco, CA");
		CarScreen.selectDateButton().perform(click());
		CarScreen.selectDates(startDateTime.toLocalDate(), startDateTime.toLocalDate());
		CarScreen.searchButton().perform(click());

		CarScreen.selectCarCategory(CATEGORY);
		screenshot("Car Offers");
		CarScreen.selectCarOffer(0);

		screenshot("Car Checkout With Price Change");
		EspressoUtils.assertViewWithTextIsDisplayed("Price changed from $32.50");
	}

	public void testCarCreateTripFailure() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		CarScreen.pickupLocation().perform(typeText("SFO"));
		CarScreen.selectPickupLocation("San Francisco, CA");
		CarScreen.selectDateButton().perform(click());
		CarScreen.selectDates(startDateTime.toLocalDate(), startDateTime.toLocalDate());
		CarScreen.searchButton().perform(click());

		CarScreen.selectCarCategory(CATEGORY);
		screenshot("Car Offers");
		CarScreen.selectCarOffer(1);

		screenshot("Car Create Trip Failure Dialog");
		CarScreen.alertDialog().check(matches(isDisplayed()));
		CarScreen.alertDialogMessage().check(matches(withText(
			Phrase.from(getActivity(), R.string.error_server_TEMPLATE).put("brand",
				BuildConfig.brand).format().toString())));
		CarScreen.alertDialogPositiveButton().perform(click());

		screenshot("Car Search");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.toolbar_search_cars));
	}

	public void testCarCreateTripExpiredProduct() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		CarScreen.pickupLocation().perform(typeText("SFO"));
		CarScreen.selectPickupLocation("San Francisco, CA");
		CarScreen.selectDateButton().perform(click());
		CarScreen.selectDates(startDateTime.toLocalDate(), startDateTime.toLocalDate());
		CarScreen.searchButton().perform(click());

		CarScreen.selectCarCategory(CATEGORY);
		screenshot("Car Offers");
		CarScreen.selectCarOffer(3);

		screenshot("Car Create Trip Expired Product Dialog");
		CarScreen.alertDialog().check(matches(isDisplayed()));
		CarScreen.alertDialogMessage().check(matches(withText(R.string.error_cars_product_expired)));
		CarScreen.alertDialogPositiveButton().perform(click());

		screenshot("Car Search");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.toolbar_search_cars));
	}

}
