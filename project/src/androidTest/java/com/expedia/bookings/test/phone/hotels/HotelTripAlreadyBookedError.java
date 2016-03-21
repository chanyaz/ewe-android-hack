package com.expedia.bookings.test.phone.hotels;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;
import static com.expedia.bookings.test.phone.hotels.HotelScreen.doGenericSearch;

public class HotelTripAlreadyBookedError extends HotelTestCase {

	public void testTripAlreadyBookedError() throws Throwable {
		doGenericSearch();
		HotelScreen.selectHotel("error_checkout_trip_already_booked");
		Common.delay(1);
		HotelScreen.selectRoom();
		HotelScreen.checkout(true);
		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		HotelScreen.waitForErrorDisplayed();
		ErrorScreen.clickOnItinerary();
		// Itinerary screen
		onView(withText("Your Trips")).perform(ViewActions.waitForViewToDisplay());
		assertViewWithTextIsDisplayed("Your Trips");
	}
}
