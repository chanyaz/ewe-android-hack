package com.expedia.bookings.test.phone.hotels;

import java.util.Random;

import org.joda.time.LocalDate;

import android.app.Activity;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.tablet.pagemodels.Checkout;

public class HotelCreditCardUtil {

	public static class HotelCreditCardTestData {
		public String name;
		public String[] prefixes;
		public int length;
		public int drawableId;

		public HotelCreditCardTestData name(String name) {
			this.name = name;
			return this;
		}

		public HotelCreditCardTestData prefixes(String... prefixes) {
			this.prefixes = prefixes;
			return this;
		}

		public HotelCreditCardTestData length(int length) {
			this.length = length;
			return this;
		}

		public HotelCreditCardTestData drawableId(int drawableId) {
			this.drawableId = drawableId;
			return this;
		}
	}

	public static void driveHotelCreditCardTest(Activity activity, HotelCreditCardTestData data) throws Throwable {
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("SFO");
		HotelsSearchScreen.clickSuggestionWithName(activity, "San Francisco, CA (SFO-San Francisco Intl.)");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickHotelWithName("happypath");
		HotelsDetailsScreen.clickSelectButton();
		HotelsRoomsRatesScreen.selectRoomItem(0);
		HotelsCheckoutScreen.clickCheckoutButton();

		HotelsCheckoutScreen.clickGuestDetails();
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");
		CommonTravelerInformationScreen.clickDoneButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();

		Random rand = new Random();
		int randomNumber;

		String creditCardNumber;

		for (int i = 0; i < data.prefixes.length; i++) {
			//random credit card numbers per card type
			creditCardNumber = data.prefixes[i];
			for (int k = creditCardNumber.length(); k < data.length; k++) {
				randomNumber = rand.nextInt(10);
				creditCardNumber += randomNumber;
			}

			CardInfoScreen.typeTextCreditCardEditText(creditCardNumber);
			Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
			CardInfoScreen.clickOnExpirationDateButton();
			CardInfoScreen.clickMonthUpButton();
			CardInfoScreen.clickYearUpButton();
			CardInfoScreen.clickSetButton();
			CardInfoScreen.typeTextPostalCode("94015");
			CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");


			/*
			* Case 1: verify cards working, test credit card logo displayed
			*/
			EspressoUtils.assertContainsImageDrawable(R.id.display_credit_card_brand_icon_white,
				data.drawableId);
			CardInfoScreen.clickOnDoneButton();
			HotelsCheckoutScreen.slideToCheckout();

			/*
			* Case 2: check cvv sub prompt text view
			* For Amex cards:"See front of the card" and for other cards: "See back of card"
			*/

			if (data.name.equals("Amex")) {
				EspressoUtils.assertViewWithTextIsDisplayed(
					activity.getString(R.string.See_front_of_card));
			}
			else {
				EspressoUtils
					.assertViewWithTextIsDisplayed(activity.getString(R.string.See_back_of_card));
			}

			/*
			* Case 3: Security Code will show the cardholders name Firstname Lastname as: F. Lastname
			*/

			EspressoUtils.assertContains(CVVEntryScreen.cvvSignatureText(), "M. Auto");

			//go back for next test data
			Common.pressBack();
			Checkout.clickCreditCardSection();
			CommonPaymentMethodScreen.clickOnAddNewCardTextView();
		}
	}
}
