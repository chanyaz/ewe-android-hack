package com.expedia.bookings.test.phone.newhotels;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.doGenericSearch;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectHotel;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.pickRoom;


public class HotelCreateTripTest extends HotelTestCase {

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

}
