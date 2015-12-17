package com.expedia.bookings.test.ui.tablet.tests.ui;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

import static android.support.test.espresso.action.ViewActions.clearText;

/**
 * Created by dmadan on 8/19/14.
 */
public class InvalidDataErrorTest extends TabletTestCase {

	/*
	 * This test needs to be run against Production, so make sure
	 * that your config.json has the server name variable as Production
	 */

	private static final String[] BAD_CREDIT_CARDS = {
		"378734493671001", // AMEX
		"30569309025905",  // Diners Club
		"6011000990139425",// Discover
	};

	private static final String[] BAD_PHONE_NUMBERS = {
		"111",
		"951-",
	};

	private void getToCheckout() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Results.swipeUpHotelList();
		Results.clickHotelAtIndex(10);
		HotelDetails.clickAddHotel();
		Results.clickBookHotel();
	}

	public void testInvalidCreditCards() throws Throwable {
		getToCheckout();

		//setup traveler info
		Checkout.clickOnEmptyTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());

		//checkout using invalid CC
		for (int i = 0; i < BAD_CREDIT_CARDS.length; i++) {
			Checkout.creditCardNumber().perform(clearText());
			Checkout.enterCreditCardNumber(BAD_CREDIT_CARDS[i]);
			Common.closeSoftKeyboard(Checkout.creditCardNumber());
			Checkout.clickOnDone();
			Checkout.slideToPurchase();
			Checkout.enterCvv("1111");
			Checkout.clickBookButton();

			//error popup
			EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.e3_error_checkout_payment_failed));
			Checkout.clickOKButton();
		}

		//complete checkout using valid CC after error popup
		Checkout.creditCardNumber().perform(clearText());
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.clickOnDone();
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();
		Checkout.clickDoneBooking();
	}

	public void testInvalidPhoneNumbers() throws Throwable {
		getToCheckout();

		//setup payment info
		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();

		Checkout.clickOnEmptyTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());

		//checkout using invalid phone number
		for (int i = 0; i < BAD_PHONE_NUMBERS.length; i++) {
			Checkout.phoneNumber().perform(clearText());
			Checkout.enterPhoneNumber(BAD_PHONE_NUMBERS[i]);
			Checkout.clickOnDone();
			Checkout.slideToPurchase();
			Checkout.enterCvv("1111");
			Checkout.clickBookButton();

			//error popup
			EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.ean_error_invalid_phone_number));
			Checkout.clickOKButton();
		}

		//complete checkout using valid phone after error popup
		Checkout.phoneNumber().perform(clearText());
		Checkout.enterPhoneNumber("1112223333");
		Checkout.clickOnDone();
		Checkout.slideToPurchase();
		Checkout.enterCvv("1111");
		Checkout.clickBookButton();
		Checkout.clickDoneBooking();
	}

	public void testInvalidCardHolderName() throws Throwable {
		getToCheckout();

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
		Checkout.enterNameOnCard("Expedia");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();

		//error popup
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.error_name_on_card_mismatch));
		Checkout.clickOKButton();

		//back to payment details
		Checkout.nameOnCard().perform(clearText());
		Checkout.enterNameOnCard("Mobiata Auto");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();
		Checkout.clickDoneBooking();
	}
}
