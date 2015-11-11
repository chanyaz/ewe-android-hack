package com.expedia.bookings.test.phone.newhotels;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.test.espresso.HotelTestCase;

import static com.expedia.bookings.test.phone.newhotels.HotelScreen.checkout;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.doGenericSearch;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.enterCVV;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.pickRoom;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectHotel;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectRoom;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.slideToPurchase;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.verifyPriceChange;

public class HotelPriceChangeTest extends HotelTestCase {

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

}
