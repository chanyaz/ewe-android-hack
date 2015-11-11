package com.expedia.bookings.test.phone.newhotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.checkout;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.doGenericSearch;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.enterCVV;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectHotel;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectRoom;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.slideToPurchase;

public class HotelSessionTimeoutErrorTest extends HotelTestCase {

	public void testSessionTimeoutError() throws Throwable {
		doGenericSearch();
		selectHotel("error_checkout_session_timeout");
		selectRoom();
		checkout(true);
		slideToPurchase();
		enterCVV();
		HotelScreen.waitForErrorDisplayed();
		ErrorScreen.clickOnSearchAgain();
		// Search Screen
		onView(withId(R.id.search_container)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.search_container);
	}
}
