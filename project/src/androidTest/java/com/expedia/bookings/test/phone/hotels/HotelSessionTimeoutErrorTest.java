package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.R;
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
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;

public class HotelSessionTimeoutErrorTest extends HotelTestCase {

	@Test
	public void testSessionTimeoutError() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("error_checkout_session_timeout");
		Common.delay(1);
		HotelInfoSiteScreen.bookFirstRoom();
		HotelCheckoutScreen.checkout(true);
		CheckoutScreen.performSlideToPurchase(false);
		HotelCheckoutScreen.enterCVVAndBook();
		HotelCheckoutScreen.waitForErrorDisplayed();
		ErrorScreen.clickOnSearchAgain();
		// Search Screen
		onView(withId(R.id.search_container)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.search_container);
	}
}
