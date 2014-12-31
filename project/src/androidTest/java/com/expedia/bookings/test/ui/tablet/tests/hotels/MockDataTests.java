package com.expedia.bookings.test.ui.tablet.tests.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.TabletTestCase;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

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
		EspressoUtils.assertContains(onView(withId(R.id.sold_out_text_view)), mRes.getString(R.string.tablet_sold_out_summary_text_hotel));

		//click on Select another hotel
		onView(withId(R.id.select_new_item_button)).perform(click());
		Results.clickHotelWithName("happy_path");
	}
}
