package com.expedia.bookings.test.phone.newhotels;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;

import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.checkout;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.doSearch;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.enterCVV;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.pickRoom;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectHotel;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectRoom;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.slideToPurchase;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.verifyPriceChange;

public class HotelCheckoutErrorsTest extends HotelTestCase {

	public void testPriceChange() throws Throwable {
		doSearch();
		selectHotel("hotel_price_change");
		selectRoom();
		//Create Trip Price Change
		verifyPriceChange("Price changed from $2,394.88");
		Espresso.pressBack();
		pickRoom("hotel_price_change_checkout");
		checkout();
		slideToPurchase();
		enterCVV();
		//Checkout Price Change
		verifyPriceChange("Price changed from $675.81");
	}

	public void testInvalidCardDetails() throws Throwable {
		doSearch();
		selectHotel("error_checkout_card");
		selectRoom();
		checkout();
		slideToPurchase();
		enterCVV();
		Common.delay(2);
		screenshot("Hotel_Checkout_Error");
		ErrorScreen.clickOnEditPayment();
		// Traveler Info Edit Screen
		assertViewIsDisplayed(R.id.section_billing_info);
	}

	public void testInvalidTravellerInfo() throws Throwable {
		doSearch();
		selectHotel("error_checkout_traveller_info");
		selectRoom();
		checkout();
		slideToPurchase();
		enterCVV();
		Common.delay(2);
		screenshot("Hotel_Checkout_Error");
		ErrorScreen.clickOnEditTravellerInfo();
		// Traveler Info Edit Screen
		assertViewIsDisplayed(R.id.edit_first_name);
	}

	public void testUnknownCheckoutError() throws Throwable {
		doSearch();
		selectHotel("error_checkout_unknown");
		selectRoom();
		checkout();
		slideToPurchase();
		enterCVV();
		Common.delay(2);
		screenshot("Hotel_Checkout_Error");
		ErrorScreen.clickOnRetry();
		// Checkout Summary Screen
		assertViewIsDisplayed(R.id.summary_container);
	}
}
