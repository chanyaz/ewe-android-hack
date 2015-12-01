package com.expedia.bookings.test.phone.newhotels;

import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.checkout;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.doGenericSearch;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.enterCVV;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectHotel;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectRoom;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.slideToPurchase;

public class HotelTripAlreadyBookedError extends HotelTestCase {

	public void testTripAlreadyBookedError() throws Throwable {
		doGenericSearch();
		selectHotel("error_checkout_trip_already_booked");
		selectRoom();
		checkout(true);
		slideToPurchase();
		enterCVV();
		HotelScreen.waitForErrorDisplayed();
		ErrorScreen.clickOnItinerary();
		// Itinerary screen
		onView(withText("Your Trips")).perform(ViewActions.waitForViewToDisplay());
		assertViewWithTextIsDisplayed("Your Trips");
	}
}
