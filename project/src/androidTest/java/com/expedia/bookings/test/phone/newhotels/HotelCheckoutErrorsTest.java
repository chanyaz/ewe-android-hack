package com.expedia.bookings.test.phone.newhotels;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;

import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;
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
		verifyPriceChange("Price dropped from $2,394.88");
		Espresso.pressBack();
		pickRoom("hotel_price_change_checkout");
		checkout(false);
		slideToPurchase();
		enterCVV();
		//Checkout Price Change
		verifyPriceChange("Price changed from $740.77");
	}

	public void testInvalidCardDetails() throws Throwable {
		moveToCheckout("error_checkout_card");
		ErrorScreen.clickOnEditPayment();
		// Card Details Edit Screen
		assertViewIsDisplayed(R.id.section_billing_info);
	}

	public void testInvalidTravellerInfo() throws Throwable {
		moveToCheckout("error_checkout_traveller_info");
		ErrorScreen.clickOnEditTravellerInfo();
		// Traveler Info Edit Screen
		assertViewIsDisplayed(R.id.edit_first_name);
	}

	public void testTripAlreadyBookedError() throws Throwable {
		moveToCheckout("error_checkout_trip_already_booked");
		ErrorScreen.clickOnItinerary();
		Common.delay(2);
		// Itinerary screen
		assertViewWithTextIsDisplayed("Your Trips");
	}

	public void testUnknownCheckoutError() throws Throwable {
		moveToCheckout("error_checkout_unknown");
		ErrorScreen.clickOnRetry();
		Common.delay(1);
		assertViewIsDisplayed(R.id.slide_to_purchase_widget);
	}

	public void testSessionTimeoutError() throws Throwable {
		moveToCheckout("error_checkout_session_timeout");
		ErrorScreen.clickOnSearchAgain();
		// Search Screen
		assertViewIsDisplayed(R.id.search_container);
	}

	public void testPaymentFailedError() throws Throwable {
		moveToCheckout("error_checkout_card_limit_exceeded");
		ErrorScreen.clickOnEditPayment();
		// Card Details Edit Screen
		assertViewIsDisplayed(R.id.section_billing_info);
	}

	private void moveToCheckout(String hotelName) throws Throwable {
		doSearch();
		selectHotel(hotelName);
		selectRoom();
		checkout(true);
		slideToPurchase();
		enterCVV();
		Common.delay(2);
	}
}
