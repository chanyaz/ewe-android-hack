package com.expedia.bookings.test.phone.tests.ui;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.tablet.pagemodels.Common;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.PhoneTestCase;

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
		"94000000000001",  // Carte Blanche
		"30569309025905",  // Diners Club
		"6011000990139425",// Discover
	};

	private void getToCheckout() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestion(getActivity(), "New York, NY");
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
			SettingsScreen.clickOKString();
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
		HotelsCheckoutScreen.clickAddTravelerButton();
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
			EspressoUtils.clear(CardInfoScreen.creditCardNumberEditText());
			CardInfoScreen.typeTextCreditCardEditText(BAD_CREDIT_CARDS[i]);
			Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
			CardInfoScreen.clickOnDoneButton();
			HotelsCheckoutScreen.slideToCheckout();
			CVVEntryScreen.parseAndEnterCVV("1111");
			CVVEntryScreen.clickBookButton();

			//error popup
			EspressoUtils.assertViewWithTextIsDisplayed("The credit card number you entered is invalid, please try again.");
			CVVEntryScreen.clickOkButton();
		}

		//complete checkout using valid CC after error popup
		EspressoUtils.clear(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnDoneButton();
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("1111");
		CVVEntryScreen.clickBookButton();
		HotelsConfirmationScreen.clickDoneButton();
	}
}
