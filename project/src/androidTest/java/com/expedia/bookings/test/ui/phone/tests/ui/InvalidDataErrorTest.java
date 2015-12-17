package com.expedia.bookings.test.ui.phone.tests.ui;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import static android.support.test.espresso.action.ViewActions.clearText;

/**
 * Created by dmadan on 8/20/14.
 */
public class InvalidDataErrorTest extends PhoneTestCase {
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
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickListItem(10);
		HotelsDetailsScreen.clickSelectButton();
		HotelsRoomsRatesScreen.selectRoomItem(0);
		try {
			SettingsScreen.clickOkString();
		}
		catch (Exception e) {
			//No popup
		}
		HotelsCheckoutScreen.clickCheckoutButton();
	}

	public void testInvalidCreditCards() throws Throwable {
		getToCheckout();

		//setup traveler info
		HotelsCheckoutScreen.clickGuestDetails();
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");
		CommonTravelerInformationScreen.clickDoneButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextPostalCode("94015");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");

		//checkout using invalid CC
		for (int i = 0; i < BAD_CREDIT_CARDS.length; i++) {
			CardInfoScreen.creditCardNumberEditText().perform(clearText());
			CardInfoScreen.typeTextCreditCardEditText(BAD_CREDIT_CARDS[i]);
			Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
			CardInfoScreen.clickOnDoneButton();
			HotelsCheckoutScreen.slideToCheckout();
			CVVEntryScreen.parseAndEnterCVV("1111");
			CVVEntryScreen.clickBookButton();

			//error popup
			EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.e3_error_checkout_payment_failed));
			CVVEntryScreen.clickOkButton();
		}

		//complete checkout using valid CC after error popup
		CardInfoScreen.creditCardNumberEditText().perform(clearText());
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnDoneButton();
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("1111");
		CVVEntryScreen.clickBookButton();
		HotelsConfirmationScreen.clickDoneButton();
	}

	public void testInvalidPhoneNumbers() throws Throwable {
		getToCheckout();

		//setup payment info
		HotelsCheckoutScreen.clickSelectPaymentButton();
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextPostalCode("94015");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		HotelsConfirmationScreen.clickDoneButton();

		HotelsCheckoutScreen.clickGuestDetails();
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");

		//checkout using invalid phone number
		for (int i = 0; i < BAD_PHONE_NUMBERS.length; i++) {
			CommonTravelerInformationScreen.phoneNumberEditText().perform(clearText());
			CommonTravelerInformationScreen.enterPhoneNumber(BAD_PHONE_NUMBERS[i]);
			CommonTravelerInformationScreen.clickDoneButton();
			HotelsCheckoutScreen.slideToCheckout();
			CVVEntryScreen.parseAndEnterCVV("1111");
			CVVEntryScreen.clickBookButton();

			//error popup
			EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.ean_error_invalid_phone_number));
			CVVEntryScreen.clickOkButton();
		}

		//complete checkout using valid phone after error popup
		CommonTravelerInformationScreen.phoneNumberEditText().perform(clearText());
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.clickDoneButton();
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("1111");
		CVVEntryScreen.clickBookButton();
		HotelsConfirmationScreen.clickDoneButton();
	}

	public void testInvalidCardHolderName() throws Throwable {
		getToCheckout();

		HotelsCheckoutScreen.clickGuestDetails();
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");
		CommonTravelerInformationScreen.clickDoneButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextPostalCode("94015");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata");
		CardInfoScreen.clickOnDoneButton();
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("1111");
		CVVEntryScreen.clickBookButton();

		//error popup
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.error_name_on_card_mismatch));
		CVVEntryScreen.clickOkButton();

		//back to payment details
		CardInfoScreen.nameOnCardEditText().perform(clearText());
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.clickOnDoneButton();

		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("1111");
		CVVEntryScreen.clickBookButton();
		HotelsConfirmationScreen.clickDoneButton();
	}
}
