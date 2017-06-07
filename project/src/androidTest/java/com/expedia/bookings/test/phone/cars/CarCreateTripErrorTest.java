package com.expedia.bookings.test.phone.cars;

import org.joda.time.DateTime;
import org.junit.Test;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.CarTestCase;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.squareup.phrase.Phrase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CarCreateTripErrorTest extends CarTestCase {

	private static final String CATEGORY = "Economy";

	@Test
	public void testCarCreateTripWithPriceChange() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		SearchScreen.doGenericCarSearch();

		CarScreen.selectCarCategory(CATEGORY);
		screenshot("Car Offers");
		CarScreen.selectCarOffer(0);

		screenshot("Car Checkout With Price Change");
		EspressoUtils.assertViewWithTextIsDisplayed("Price changed from $32.50");
	}

	@Test
	public void testCarCreateTripFailure() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		SearchScreen.doGenericCarSearch();

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

	@Test
	public void testCarCreateTripExpiredProduct() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		SearchScreen.doGenericCarSearch();

		CarScreen.selectCarCategory(CATEGORY);
		screenshot("Car Offers");
		CarScreen.selectCarOffer(2);

		screenshot("Car Create Trip Expired Product Dialog");
		CarScreen.alertDialog().check(matches(isDisplayed()));
		CarScreen.alertDialogMessage().check(matches(withText(R.string.error_cars_product_expired)));
		CarScreen.alertDialogPositiveButton().perform(click());

		screenshot("Car Search");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.toolbar_search_cars));
	}

}
