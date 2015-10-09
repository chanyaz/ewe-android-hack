package com.expedia.bookings.test.ui.tablet.tests.flights;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.LogIn;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelsUserData;
import com.expedia.bookings.test.espresso.TabletTestCase;

import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 5/29/14.
 */
public class FlightCheckoutUserInfoTests extends TabletTestCase {

	HotelsUserData mUser;

	private static final String TAG = FlightCheckoutUserInfoTests.class.getSimpleName();

	public void testCheckFlights() throws Exception {
		// Test setup
		mUser = new HotelsUserData();
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
		// Validation
		verifyNameMustMatchIdWarning();
		verifyRulesAndRestrictionsButton();
		verifyMissingTravelerInformationAlerts();
		verifyNameMustMatchIdWarningWithInfoEntered();
		verifyMissingCardInfoAlerts();
		verifyLoginButtonNotAppearing();
	}

	private void verifyNameMustMatchIdWarning() {
		ScreenActions.enterLog(TAG, "Start testing name must match id warning in user info");
		ScreenActions.delay(1);
		// Warning should appear in empty traveler details
		// and remain when screen is clicked or user starts typing.
		Checkout.clickOnEmptyTravelerDetails();
		Checkout.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		Checkout.nameMustMatchTextView().perform(click());
		Checkout.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		Checkout.enterFirstName("foo");
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		Common.pressBack();
	}

	private void verifyNameMustMatchIdWarningWithInfoEntered() {
		ScreenActions.enterLog(TAG, "Start testing name must match id warning with info already entered");
		ScreenActions.delay(1);
		// Warning should appear and persist on populated traveler details screen.
		Checkout.clickOnTravelerDetails();
		Checkout.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		Checkout.nameMustMatchTextView().perform(click());
		Checkout.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		Common.pressBack();
	}

	private void verifyRulesAndRestrictionsButton() {
		Checkout.clickLegalTextView();
		EspressoUtils.assertViewWithTextIsDisplayed("Privacy Policy");
		EspressoUtils.assertViewWithTextIsDisplayed("Terms and Conditions");
		EspressoUtils.assertViewWithTextIsDisplayed("Rules and Restrictions");
		Common.pressBack();
		Common.enterLog(TAG, "Rules and Restriction button on checkout screen works");
	}

	private void verifyMissingTravelerInformationAlerts() {
		Checkout.clickOnEmptyTravelerDetails();
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();

		Common.checkErrorIconDisplayed(Checkout.firstName());
		Common.checkErrorIconDisplayed(Checkout.lastName());
		Common.checkErrorIconDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());
		Common.enterLog(TAG, "all fields show error icon when empty and 'DONE' is pressed");

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
		Common.enterLog(TAG, "all field but first, middle name and birth date fields show error when 'Done' is pressed.");

		Checkout.enterLastName("Auto");
		Common.closeSoftKeyboard(Checkout.lastName());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());
		Common.enterLog(TAG, "all field but first,last, middle name and birth date fields show error when 'Done' is pressed.");

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
		Common.enterLog(TAG, "email address with no '@' or TLD is found invalid.");

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
		Common.enterLog(TAG, "email address with no website or TLD is found invalid.");

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
		Common.enterLog(TAG, "email address with no TLD is found invalid.");

		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconNotDisplayed(Checkout.emailAddress());
		Common.enterLog(TAG, "just phone number field show error when 'Done' is pressed.");

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
		Common.enterLog(TAG, "phone number must be at least 3 chars long");

		Checkout.clickRedressNumberButton();
		Checkout.enterRedressNumber("12345678");
		Checkout.redressNumber().check(matches(withText("1234567")));
		Checkout.clickOnDone();
		Common.enterLog(TAG, "redress EditText allows a max of 7 chars, numbers only");

		Common.checkDisplayed(Checkout.loginButton());
		Common.enterLog(TAG, "After all traveler info was entered, the test was able to return to the checkout screen");
	}

	private void verifyMissingCardInfoAlerts() {
		Checkout.clickOnEnterPaymentInformation();

		String twentyChars = "12345123451234512345";
		Checkout.enterCreditCardNumber(twentyChars.substring(0, 12));
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.clickOnDone();
		Common.checkErrorIconDisplayed(Checkout.expirationDate());
		Common.checkErrorIconDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconDisplayed(Checkout.address1());
		Common.checkErrorIconDisplayed(Checkout.addressCity());
		Common.enterLog(TAG, "12 chars is too short for CC edittext");

		String nineteenChars = twentyChars.substring(0, 19);
		Checkout.creditCardNumber().perform(clearText());
		Checkout.enterCreditCardNumber(twentyChars);
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.creditCardNumber().check(matches(withText(nineteenChars)));
		Checkout.creditCardNumber().perform(clearText());
		Common.enterLog(TAG, "CC edittext has a max capacity of 19 chars");

		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.clickOnDone();
		Common.checkErrorIconDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconDisplayed(Checkout.address1());
		Common.checkErrorIconDisplayed(Checkout.addressCity());
		Common.enterLog(TAG, "After entering CC number correctly, the CC edit text no longer has error icon");

		Checkout.setExpirationDate(2020, 12);
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconDisplayed(Checkout.address1());
		Common.checkErrorIconDisplayed(Checkout.addressCity());
		Common.enterLog(TAG, "After entering expiration date, that field no longer has error icon");

		Checkout.enterNameOnCard("Mobiata Auto");
		Common.closeSoftKeyboard(Checkout.nameOnCard());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconNotDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconDisplayed(Checkout.address1());
		Common.checkErrorIconDisplayed(Checkout.addressCity());
		Common.enterLog(TAG, "After entering cardholder name, that edit text no longer has error icon");

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
		Common.enterLog(TAG, "postal code has error icon");

		Checkout.enterPostalCode("53704");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();

		Common.checkDisplayed(Checkout.loginButton());
		Common.enterLog(TAG, "After all card info was entered, the test was able to return to the checkout screen");
	}

	private void verifyLoginButtonNotAppearing() throws Exception {
		Checkout.clickLoginButton();
		LogIn.enterPassword(mUser.password);
		LogIn.clickLoginExpediaButton();
		Common.pressBack();
		Results.clickBookFlight();

		EspressoUtils.assertViewWithTextIsDisplayed(mUser.email);
		Checkout.clickLogOutButton();
		Checkout.clickLogOutString();
		Common.enterLog(TAG, "Was able to log in, and the email used is now visible from the checkout screen");

		Common.checkDisplayed(Checkout.loginButton());
		Common.enterLog(TAG, "Log out button was visible and able to be clicked. Log in button now visible on checkout screen");
	}
}
