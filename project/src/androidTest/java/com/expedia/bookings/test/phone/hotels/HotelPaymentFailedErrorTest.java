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

public class HotelPaymentFailedErrorTest extends HotelTestCase {

	@Test
	public void testPaymentFailedError() throws Throwable {
		SearchScreenActions.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("error_checkout_card_limit_exceeded");
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
	public void testPaymentFailedErrorWithBack() throws Throwable {
		SearchScreenActions.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("error_checkout_card_limit_exceeded");
		Common.delay(1);
		HotelInfoSiteScreen.bookFirstRoom();
		HotelCheckoutScreen.checkout(true);
		CheckoutScreen.performSlideToPurchase(false);
		HotelCheckoutScreen.enterCVVAndBook();
		HotelCheckoutScreen.waitForErrorDisplayed();
		Common.pressBack();

		onView(withId(R.id.section_billing_info)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.section_billing_info);
	}

	@Test
	public void testPaymentFailedErrorWithToolbarBack() throws Throwable {
		SearchScreenActions.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("error_checkout_card_limit_exceeded");
		Common.delay(1);
		HotelInfoSiteScreen.bookFirstRoom();
		HotelCheckoutScreen.checkout(true);
		CheckoutScreen.performSlideToPurchase(false);
		HotelCheckoutScreen.enterCVVAndBook();
		HotelCheckoutScreen.waitForErrorDisplayed();
		ErrorScreen.clickToolbarBack();

		onView(withId(R.id.section_billing_info)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.section_billing_info);
	}


}
