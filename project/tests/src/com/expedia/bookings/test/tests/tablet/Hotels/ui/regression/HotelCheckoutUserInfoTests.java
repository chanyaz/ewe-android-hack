package com.expedia.bookings.test.tests.tablet.Hotels.ui.regression;

import com.expedia.bookings.test.tests.pageModels.tablet.Checkout;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.utils.TabletTestCase;
import com.google.android.apps.common.testing.ui.espresso.Espresso;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 6/10/14.
 */
public class HotelCheckoutUserInfoTests extends TabletTestCase {

	public void testCheckHotels() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Results.swipeUpHotelList();
		Results.clickHotelWithName("happy_path");
		Results.clickAddHotel();
		Results.clickBookHotel();

		verifyRulesAndRestrictionsButton();
		verifyMissingTravelerInformationAlerts();
		verifyMissingCardInfoAlerts();
	}

	private void verifyRulesAndRestrictionsButton() {
		Checkout.clickLegalTextView();
		Common.checkDisplayed(Checkout.bestPriceGuarantee());
		Common.checkDisplayed(Checkout.cancellationPolicy());
		Common.checkDisplayed(Checkout.termsConditions());
		Common.checkDisplayed(Checkout.privacyPolicy());
		Espresso.pressBack();
	}

	private void verifyMissingTravelerInformationAlerts() {
		//Starting testing of traveler info screen response when fields are left empty
		Checkout.clickOnTravelerDetails();
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();

		//all fields show error icon when empty and 'DONE' is pressed
		Common.checkErrorIconDisplayed(Checkout.firstName());
		Common.checkErrorIconDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());

		//all field but first name show error when 'Done' is pressed
		Checkout.enterFirstName("Mobiata");
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());

		//all field but first,last, middle name fields show error when 'Done' is pressed
		Checkout.enterLastName("Auto");
		Common.closeSoftKeyboard(Checkout.lastName());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());

		//phone number must be at least 3 chars long
		Checkout.enterPhoneNumber("11");
		Common.closeSoftKeyboard(Checkout.phoneNumber());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());
		Checkout.phoneNumber().perform(clearText());
		Checkout.enterPhoneNumber("111");
		Common.closeSoftKeyboard(Checkout.phoneNumber());
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconNotDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());

		//email address with no '@' or TLD is found invalid
		Checkout.enterEmailAddress("aaa");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconNotDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());
		Checkout.emailAddress().perform(clearText());

		//email address with no website or TLD is found invalid
		Checkout.enterEmailAddress("aaa@");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconNotDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());
		Checkout.emailAddress().perform(clearText());

		//email address with no TLD is found invalid
		Checkout.enterEmailAddress("aaa@aaa");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconNotDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());
		Checkout.emailAddress().perform(clearText());

		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconNotDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconNotDisplayed(Checkout.emailAddress());
		Checkout.clickOnDone();
	}

	private void verifyMissingCardInfoAlerts() {
		Checkout.clickOnEnterPaymentInformation();

		//12 chars is too short for CC edittext
		String twentyChars = "12345123451234512345";
		Checkout.enterCreditCardNumber(twentyChars.substring(0, 12));
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.clickOnDone();
		Common.checkErrorIconDisplayed(Checkout.expirationDate());
		Common.checkErrorIconDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconNotDisplayed(Checkout.postalCode());

		//CC edit text has a max capacity of 19 chars
		String nineteenChars = twentyChars.substring(0, 19);
		Checkout.creditCardNumber().perform(clearText());
		Checkout.enterCreditCardNumber(twentyChars);
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.creditCardNumber().check(matches(withText(nineteenChars)));
		Checkout.creditCardNumber().perform(clearText());

		//After entering CC number, the CC edit text no longer has error icon
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.clickOnDone();
		Common.checkErrorIconDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconNotDisplayed(Checkout.postalCode());

		//After entering expiration date, that field no longer has error icon
		Checkout.setExpirationDate(2020, 12);
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconDisplayed(Checkout.nameOnCard());

		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("53704");
		Common.closeSoftKeyboard(Checkout.nameOnCard());
		Common.checkErrorIconNotDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconNotDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconNotDisplayed(Checkout.postalCode());
		Checkout.clickOnDone();
	}
}
