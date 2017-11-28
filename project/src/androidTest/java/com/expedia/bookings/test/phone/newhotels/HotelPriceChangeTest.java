package com.expedia.bookings.test.phone.newhotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;
import com.expedia.bookings.test.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;

import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;

public class HotelPriceChangeTest extends HotelTestCase {

	@Test
	public void testPriceChangeGuestUser() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("hotel_price_change");
		HotelInfoSiteScreen.clickStickySelectRoom();
		HotelInfoSiteScreen.bookRoomType("hotel_price_change_checkout");
		HotelScreen.checkout(true);
		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		assertViewIsDisplayed(R.id.price_change_text);
	}
}
