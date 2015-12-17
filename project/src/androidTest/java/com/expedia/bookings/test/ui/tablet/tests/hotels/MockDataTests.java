package com.expedia.bookings.test.ui.tablet.tests.hotels;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 9/11/14.
 */
public class MockDataTests extends TabletTestCase {

	public void testRoomNoLongerAvailable() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Results.swipeUpHotelList();
		Results.clickHotelWithName("error_room_unavailable");
		HotelDetails.clickAddHotel();
		Results.clickBookHotel();

		//room sold out message
		EspressoUtils.assertContains(onView(withId(R.id.sold_out_text_view)), mRes.getString(R.string.tablet_sold_out_summary_text_hotel));

		//click on Select another hotel
		onView(withId(R.id.select_new_item_button)).perform(click());
		Results.clickHotelWithName("happypath");
	}

	public void testCheckoutSessionTimeout() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Results.swipeUpHotelList();
		Results.clickHotelWithName("error_checkout_session_timeout");
		HotelDetails.clickAddHotel();
		Results.clickBookHotel();

		Checkout.clickOnEmptyTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();

		//room sold out message
		EspressoUtils.assertContains(onView(withId(R.id.sold_out_text_view)),
			mRes.getString(R.string.tablet_sold_out_summary_text_hotel));

		//click on Select another hotel
		onView(withId(R.id.select_new_item_button)).perform(click());
		Results.clickHotelWithName("happypath");
	}

	public void testPaymentFailedOnCheckout() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Results.swipeUpHotelList();
		Results.clickHotelWithName("error_checkout_card_limit_exceeded");
		HotelDetails.clickAddHotel();
		Results.clickBookHotel();

		Checkout.clickOnEmptyTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();

		// Assert we get a payment failed message
		onView(withId(android.R.id.message)).check(matches(withText(R.string.e3_error_checkout_payment_failed)));
		Checkout.clickOKButton();

		// Check that we're on payment details
		Checkout.creditCardNumber().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
		Checkout.clickOnDone();
	}
}
