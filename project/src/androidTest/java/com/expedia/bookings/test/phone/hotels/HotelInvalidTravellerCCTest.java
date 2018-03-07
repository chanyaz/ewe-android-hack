package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;
import com.expedia.bookings.test.pagemodels.hotels.ErrorScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelCheckoutScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;

public class HotelInvalidTravellerCCTest extends HotelTestCase {

	@Test
	public void testInvalidCardDetails() throws Throwable {
		SearchScreenActions.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("error_checkout_card");
		Common.delay(1);
		HotelInfoSiteScreen.bookFirstRoom();
		HotelCheckoutScreen.checkout(true);
		CheckoutScreen.performSlideToPurchase(false);
		HotelCheckoutScreen.enterCVVAndBook();
		HotelCheckoutScreen.waitForErrorDisplayed();

		ErrorScreen.clickOnEditPayment();
		// Card Details Edit Screen
		onView(withId(R.id.section_billing_info)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.section_billing_info);
	}

	@Test
	public void testInvalidTravellerInfo() throws Throwable {
		SearchScreenActions.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("error_checkout_traveller_info");
		Common.delay(1);
		HotelInfoSiteScreen.bookFirstRoom();
		HotelCheckoutScreen.checkout(true);
		CheckoutScreen.performSlideToPurchase(false);
		HotelCheckoutScreen.enterCVVAndBook();
		HotelCheckoutScreen.waitForErrorDisplayed();

		ErrorScreen.clickOnEditTravellerInfo();
		// Traveler Info Edit Screen
		onView(withId(R.id.edit_first_name)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.edit_first_name);
	}
}
