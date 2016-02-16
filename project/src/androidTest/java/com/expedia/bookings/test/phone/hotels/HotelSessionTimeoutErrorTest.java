package com.expedia.bookings.test.phone.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.phone.hotels.HotelScreen.doGenericSearch;

public class HotelSessionTimeoutErrorTest extends HotelTestCase {

	public void testSessionTimeoutError() throws Throwable {
		doGenericSearch();
		HotelScreen.selectHotel("error_checkout_session_timeout");
		Common.delay(1);
		HotelScreen.selectRoom();
		HotelScreen.checkout(true);
		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		HotelScreen.waitForErrorDisplayed();
		ErrorScreen.clickOnSearchAgain();
		// Search Screen
		onView(withId(R.id.search_container)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.search_container);
	}
}
