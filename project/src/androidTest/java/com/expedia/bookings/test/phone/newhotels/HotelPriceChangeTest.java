package com.expedia.bookings.test.phone.newhotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;
import com.expedia.bookings.test.pagemodels.hotels.HotelCheckoutScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;

import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;

public class HotelPriceChangeTest extends HotelTestCase {

	@Test
	public void testPriceChangeGuestUser() throws Throwable {
		SearchScreenActions.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("hotel_price_change");
		HotelInfoSiteScreen.clickStickySelectRoom();
		HotelInfoSiteScreen.bookRoomType("hotel_price_change_checkout");
		HotelCheckoutScreen.checkout(true);
		CheckoutScreen.performSlideToPurchase(false);
		HotelCheckoutScreen.enterCVVAndBook();
		assertViewIsDisplayed(R.id.price_change_text);
	}
}
