package com.expedia.bookings.test.tablet.tests.flights;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.tablet.pagemodels.Common;
import com.expedia.bookings.test.tablet.pagemodels.Launch;
import com.expedia.bookings.test.tablet.pagemodels.LogIn;
import com.expedia.bookings.test.tablet.pagemodels.Results;
import com.expedia.bookings.test.tablet.pagemodels.Search;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TabletTestCase;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 5/29/14.
 */
public class FlightCheckoutUserInfoTests extends TabletTestCase {

	HotelsUserData mUser;

	public void testCheckFlights() throws Exception {
		mUser = new HotelsUserData(getInstrumentation());
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Search.clickOriginButton();
		Search.typeInOriginEditText("San Francisco, CA");
		Search.clickSuggestion("San Francisco, CA");
		Search.clickSelectFlightDates();
		int randomOffset = 20 + (int) (Math.random() * 100);
		LocalDate startDate = LocalDate.now().plusDays(randomOffset);
		Search.clickDate(startDate, null);
		Search.clickSearchPopupDone();
		Results.swipeUpFlightList();
		Results.clickFlightAtIndex(1);
		Results.clickAddFlight();
		Results.clickBookFlight();
		verifyRulesAndRestrictionsButton();
		verifyMissingTravelerInformationAlerts();
		verifyMissingCardInfoAlerts();
		verifyLoginButtonNotAppearing();
	}

	private void verifyRulesAndRestrictionsButton() {
		Checkout.clickLegalTextView();
		EspressoUtils.assertViewWithTextIsDisplayed("Privacy Policy");
		EspressoUtils.assertViewWithTextIsDisplayed("Terms and Conditions");
		EspressoUtils.assertViewWithTextIsDisplayed("Rules and Restrictions");
		Common.pressBack();
	}

	private void verifyMissingTravelerInformationAlerts() {
		Checkout.clickOnEmptyTravelerDetails();
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();

		//all fields show error icon when empty and 'DONE' is pressed
		Common.checkErrorIconDisplayed(Checkout.firstName());
		Common.checkErrorIconDisplayed(Checkout.lastName());
		Common.checkErrorIconDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());

		//all field but first, middle name and birth date fields show error when 'Done' is pressed.
		Checkout.enterDateOfBirth(1970, 1, 1);
		Checkout.enterFirstName("Mobiata");
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());

		//all field but first,last, middle name and birth date fields show error when 'Done' is pressed.
		Checkout.enterLastName("Auto");
		Common.closeSoftKeyboard(Checkout.lastName());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());

		//email address with no '@' or TLD is found invalid
		Checkout.enterEmailAddress("aaa");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());
		Checkout.emailAddress().perform(clearText());

		//email address with no website or TLD is found invalid
		Checkout.enterEmailAddress("aaa@");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());
		Checkout.emailAddress().perform(clearText());

		//email address with no TLD is found invalid
		Checkout.enterEmailAddress("aaa@aaa");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());
		Checkout.emailAddress().perform(clearText());

		//just phone number field show error when 'Done' is pressed.
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconNotDisplayed(Checkout.emailAddress());

		//phone number must be at least 3 chars long"
		Checkout.enterPhoneNumber("11");
		Common.closeSoftKeyboard(Checkout.phoneNumber());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconNotDisplayed(Checkout.emailAddress());
		Checkout.phoneNumber().perform(clearText());
		Checkout.enterPhoneNumber("111");
		Common.closeSoftKeyboard(Checkout.phoneNumber());
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconNotDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconNotDisplayed(Checkout.emailAddress());

		//redress EditText allows a max of 7 chars, numbers only
		Checkout.clickRedressNumberButton();
		Checkout.enterRedressNumber("12345678");
		Checkout.redressNumber().check(matches(withText("1234567")));
		Checkout.clickOnDone();

		//After all traveler info was entered, the test was able to return to the checkout screen
		Common.checkDisplayed(Checkout.loginButton());
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
		Common.checkErrorIconDisplayed(Checkout.address1());
		Common.checkErrorIconDisplayed(Checkout.addressCity());

		//CC edittext has a max capacity of 19 chars
		String nineteenChars = twentyChars.substring(0, 19);
		Checkout.creditCardNumber().perform(clearText());
		Checkout.enterCreditCardNumber(twentyChars);
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.creditCardNumber().check(matches(withText(nineteenChars)));
		Checkout.creditCardNumber().perform(clearText());

		//After entering CC number correctly, the CC edit text no longer has error icon
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.clickOnDone();
		Common.checkErrorIconDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconDisplayed(Checkout.address1());
		Common.checkErrorIconDisplayed(Checkout.addressCity());

		//After entering expiration date, that field no longer has error icon
		Checkout.setExpirationDate(2020, 12);
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconDisplayed(Checkout.address1());
		Common.checkErrorIconDisplayed(Checkout.addressCity());

		//After entering cardholder name, that edit text no longer has error icon
		Checkout.enterNameOnCard("Mobiata Auto");
		Common.closeSoftKeyboard(Checkout.nameOnCard());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconNotDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconDisplayed(Checkout.address1());
		Common.checkErrorIconDisplayed(Checkout.addressCity());

		//postal code has error icon"
		Checkout.enterAddress1("123 Main St.");
		Checkout.enterCity("Madison");
		Common.closeSoftKeyboard(Checkout.addressCity());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconNotDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconNotDisplayed(Checkout.address1());
		Common.checkErrorIconNotDisplayed(Checkout.addressCity());
		Common.checkErrorIconDisplayed(Checkout.postalCode());

		Checkout.enterPostalCode("53704");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();

		//After all card info was entered, the test was able to return to the checkout screen
		Common.checkDisplayed(Checkout.loginButton());
	}

	private void verifyLoginButtonNotAppearing() throws Exception {
		Checkout.clickLoginButton();
		Common.checkDisplayed(LogIn.loginFacebookButton());

		//Log in button isn't shown until an email address is entered
		Common.checkNotDisplayed(LogIn.loginExpediaButton());
		LogIn.enterUserName(mUser.getLoginEmail());

		//Facebook button is no longer shown after email address is entered
		Common.checkNotDisplayed(LogIn.loginFacebookButton());

		//Log in button is shown after email address is entered
		Common.checkDisplayed(LogIn.loginExpediaButton());
		LogIn.enterPassword(mUser.getLoginPassword());
		LogIn.clickLoginExpediaButton();
		Common.pressBack();
		Results.clickBookFlight();

		//Was able to log in, and the email used is now visible from the checkout screen
		EspressoUtils.assertViewWithTextIsDisplayed(mUser.getLoginEmail());
		Checkout.clickLogOutButton();
		Checkout.clickLogOutString();

		//Log out button was visible and able to be clicked. Log in button now visible on checkout screen
		Common.checkDisplayed(Checkout.loginButton());
	}
}
