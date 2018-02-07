package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.hotels.ErrorScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelCheckoutScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;

public class HotelTripAlreadyBookedError extends HotelTestCase {

	@Test
	public void testTripAlreadyBookedError() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("error_checkout_trip_already_booked");
		Common.delay(1);
		HotelInfoSiteScreen.bookFirstRoom();
		HotelCheckoutScreen.checkout(true);
		CheckoutScreen.performSlideToPurchase(false);
		HotelCheckoutScreen.enterCVVAndBook();
		HotelCheckoutScreen.waitForErrorDisplayed();
		ErrorScreen.clickOnItinerary();
		// Itinerary screen
		onView(withText("Your Trips")).perform(ViewActions.waitForViewToDisplay());
		assertViewWithTextIsDisplayed("Your Trips");
	}
}
