package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static com.expedia.bookings.test.phone.hotels.HotelScreen.pickRoom;

public class HotelEmailOptInOutTest extends HotelTestCase {

	private void goToCheckout(String room) throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("hotel_email_opt_in");
		Common.delay(1);
		pickRoom(room);
		CheckoutViewModel.waitForCheckout();
		CheckoutViewModel.clickDone();
		CheckoutViewModel.clickTravelerInfo();
	}

	//test ALWAYS
	@Test
	public void testEmailOptInAlways() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("hotel_email_opt_in");
		Common.delay(1);
		HotelScreen.selectRoom();
		CheckoutViewModel.waitForCheckout();
		CheckoutViewModel.clickDone();
		CheckoutViewModel.clickTravelerInfo();
		Common.delay(1);
		EspressoUtils.assertViewIsNotDisplayed(R.id.merchandise_guest_opt_checkbox);
	}

	//test CONSENT_TO_OPT_IN
	@Test
	public void testEmailOptInt() throws Throwable {
		goToCheckout("CONSENT_TO_OPT_IN");
		Common.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.merchandise_guest_opt_checkbox,
			"I want to receive emails from Expedia with travel deals, special offers, and other information.");

	}

	//test CONSENT_TO_OPT_OUT
	@Test
	public void testEmailOptOut() throws Throwable {
		goToCheckout("CONSENT_TO_OPT_OUT");
		Common.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.merchandise_guest_opt_checkbox,
			"I do not want to receive emails from Expedia with travel deals, special offers, and other information.");
	}

}
