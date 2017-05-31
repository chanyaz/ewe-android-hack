package com.expedia.bookings.test.phone.newhotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.phone.hotels.HotelScreen.pickRoom;

public class HotelPriceChangeTest extends HotelTestCase {

	@Test
	public void testPriceChangeGuestUser() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("hotel_price_change");
		HotelScreen.clickSelectRoom();
		pickRoom("hotel_price_change_checkout");
		HotelScreen.checkout(true);
		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		assertViewIsDisplayed(R.id.price_change_text);
	}
}
