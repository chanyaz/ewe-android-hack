package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;

public class HotelInvalidTravellerCCTest extends HotelTestCase {

	@Test
	public void testInvalidCardDetails() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("error_checkout_card");
		Common.delay(1);
		HotelScreen.selectRoom();
		HotelScreen.checkout(true);
		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		HotelScreen.waitForErrorDisplayed();

		ErrorScreen.clickOnEditPayment();
		// Card Details Edit Screen
		onView(withId(R.id.section_billing_info)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.section_billing_info);
	}

	@Test
	public void testInvalidTravellerInfo() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("error_checkout_traveller_info");
		Common.delay(1);

		HotelScreen.selectRoom();
		HotelScreen.checkout(true);
		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		HotelScreen.waitForErrorDisplayed();

		ErrorScreen.clickOnEditTravellerInfo();
		// Traveler Info Edit Screen
		onView(withId(R.id.edit_first_name)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.edit_first_name);
	}
/*
	public void testPriceChange() throws Throwable {
		doGenericSearch();
		selectHotel("hotel_price_change");
		selectRoom();
		//Create Trip Price Change
		verifyPriceChange("Price dropped from $2,394.88");
		Espresso.pressBack();
		pickRoom("hotel_price_change_checkout");
		checkout(false);
		slideToPurchase();
		enterCVVAndBook();
		//Checkout Price Change
		verifyPriceChange("Price changed from $740.77");
	}

	public void testProductKeyExpiry() throws Throwable {
		doGenericSearch();
		selectHotel("error_create_trip");
		pickRoom("error_expired_product_key_createtrip");
		Common.delay(5);
		screenshot("Hotel_Product_Key_Expiry_Error");
		ErrorScreen.clickOnSearchAgain();
		// Search Screen
		HotelScreen.searchButton().check(matches(isDisplayed()));
	}
	*/
}
