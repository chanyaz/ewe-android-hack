package com.expedia.bookings.test.phone.newhotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import static com.expedia.bookings.test.phone.newhotels.HotelScreen.doGenericSearch;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.pickRoom;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectHotel;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.selectRoom;

public class HotelEmailOptInOutTest extends HotelTestCase {

	public void goToCheckout(String room) throws Throwable {
		doGenericSearch();
		selectHotel("hotel_email_opt_in");
		pickRoom(room);
		CheckoutViewModel.waitForCheckout();
		CheckoutViewModel.clickDone();
		CheckoutViewModel.clickDriverInfo();
	}

	//test ALWAYS
	public void testEmailOptInAlways() throws Throwable {
		doGenericSearch();
		selectHotel("hotel_email_opt_in");
		selectRoom();
		CheckoutViewModel.waitForCheckout();
		CheckoutViewModel.clickDone();
		CheckoutViewModel.clickDriverInfo();
		Common.delay(1);
		EspressoUtils.assertViewIsNotDisplayed(R.id.merchandise_guest_opt_checkbox);
	}

	//test CONSENT_TO_OPT_IN
	public void testEmailOptInt() throws Throwable {
		goToCheckout("CONSENT_TO_OPT_IN");
		Common.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.merchandise_guest_opt_checkbox,
			"I want to receive emails from Expedia with travel deals, special offers, and other information.");

	}

	//test CONSENT_TO_OPT_OUT
	public void testEmailOptOut() throws Throwable {
		goToCheckout("CONSENT_TO_OPT_OUT");
		Common.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.merchandise_guest_opt_checkbox,
			"I do not want to receive emails from Expedia with travel deals, special offers, and other information.");
	}

}
