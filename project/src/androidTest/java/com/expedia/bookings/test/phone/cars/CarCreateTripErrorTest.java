package com.expedia.bookings.test.phone.cars;

import com.expedia.bookings.test.espresso.CarTestCase;

public class CarCreateTripErrorTest extends CarTestCase {

	private static final String CATEGORY = "Economy";

	// 29-Aug-2017 : Disabling car UI tests since car is now a webview
//	@Test
//	public void testCarCreateTripWithPriceChange() throws Throwable {
//		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
//		SearchScreen.doGenericCarSearch();
//
//		CarScreen.selectCarCategory(CATEGORY);
//		screenshot("Car Offers");
//		CarScreen.selectCarOffer(0);
//
//		screenshot("Car Checkout With Price Change");
//		EspressoUtils.assertViewWithTextIsDisplayed("Price changed from $32.50");
//	}
//
//	@Test
//	public void testCarCreateTripFailure() throws Throwable {
//		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
//		SearchScreen.doGenericCarSearch();
//
//		CarScreen.selectCarCategory(CATEGORY);
//		screenshot("Car Offers");
//		CarScreen.selectCarOffer(1);
//
//		screenshot("Car Create Trip Failure Dialog");
//		CarScreen.alertDialog().check(matches(isDisplayed()));
//		CarScreen.alertDialogMessage().check(matches(withText(
//			Phrase.from(getActivity(), R.string.error_server_TEMPLATE).put("brand",
//				BuildConfig.brand).format().toString())));
//		CarScreen.alertDialogPositiveButton().perform(click());
//
//		screenshot("Car Search");
//		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.toolbar_search_cars));
//	}
//
//	@Test
//	public void testCarCreateTripExpiredProduct() throws Throwable {
//		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
//		SearchScreen.doGenericCarSearch();
//
//		CarScreen.selectCarCategory(CATEGORY);
//		screenshot("Car Offers");
//		CarScreen.selectCarOffer(2);
//
//		screenshot("Car Create Trip Expired Product Dialog");
//		CarScreen.alertDialog().check(matches(isDisplayed()));
//		CarScreen.alertDialogMessage().check(matches(withText(R.string.error_cars_product_expired)));
//		CarScreen.alertDialogPositiveButton().perform(click());
//
//		screenshot("Car Search");
//		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.toolbar_search_cars));
//	}

}
