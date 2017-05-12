package com.expedia.bookings.test.phone.newhotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.phone.hotels.HotelScreen.pickRoom;

public class HotelPriceChangeTest extends HotelTestCase {

	@Test
	public void testPriceChangeGuestUser() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("hotel_price_change");
		pickRoom("hotel_price_change_checkout");
		HotelScreen.checkout(true);
		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		assertViewIsDisplayed(R.id.price_change_text);
	}

//	public void testPriceChangeLoggedInUser() throws Throwable {

//		HotelScreen.selectPriceChangeHotel();
//		pickRoom("hotel_price_change_with_user_preferences");
//		CheckoutViewModel.waitForCheckout();
//		signInOnCheckout();
//		Common.delay(1);
//		HotelScreen.checkoutWithPointsOnly();
//		CheckoutViewModel.performSlideToPurchase(false);
//		CheckoutViewModel.scrollToPriceChangeMessage();
//		assertViewIsDisplayed(R.id.price_change_text);
//		CheckoutViewModel.clickPaymentInfo();
//		Common.delay(1);
//		PaymentOptionsScreen.assertTextInEditAmountMatches("665.81");
//		PaymentOptionsScreen.assertTotalDueAmountMatches("675.81");
//		PaymentOptionsScreen.assertRemainingDueMatches("10.00");
//		PaymentOptionsScreen.assertTotalPointsAvailableMatches("38,406,533");
//		PaymentOptionsScreen.assertPointsAppliedMatches("6,658");

//	}



}
