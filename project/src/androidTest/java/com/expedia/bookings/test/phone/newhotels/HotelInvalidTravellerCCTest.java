package com.expedia.bookings.test.phone.newhotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.checkout;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.doGenericSearch;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.enterCVV;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectHotel;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectRoom;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.slideToPurchase;

public class HotelInvalidTravellerCCTest extends HotelTestCase {

	public void testInvalidCardDetails() throws Throwable {
		doGenericSearch();
		selectHotel("error_checkout_card");
		selectRoom();
		checkout(true);
		slideToPurchase();
		enterCVV();
		HotelScreen.waitForErrorDisplayed();

		ErrorScreen.clickOnEditPayment();
		// Card Details Edit Screen
		onView(withId(R.id.section_billing_info)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.section_billing_info);
	}

	public void testInvalidTravellerInfo() throws Throwable {
		doGenericSearch();
		selectHotel("error_checkout_traveller_info");
		selectRoom();
		checkout(true);
		slideToPurchase();
		enterCVV();
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
		enterCVV();
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
