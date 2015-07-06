package com.expedia.bookings.test.ui.phone.tests.flights;

import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.HotelsUserData;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.withCompoundDrawable;
import static com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen.clickCheckoutButton;
import static com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen.clickLogOutButton;
import static com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen.logInButton;
import static com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen.logOutButton;
import static org.hamcrest.core.IsNot.not;

public class FlightCheckoutUserInfoTests extends PhoneTestCase {

	private static final String TAG = FlightCheckoutUserInfoTests.class.getSimpleName();
	HotelsUserData mUser;

	public void testCheckFlights() throws Exception {
		// Setup
		mUser = new HotelsUserData(getInstrumentation());
		ScreenActions.enterLog(TAG, "Launching flights application");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.enterArrivalAirport("LAS");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickPassengerSelectionButton();
		FlightsSearchScreen.incrementAdultsButton();
		ScreenActions.enterLog(TAG, "Click search button");
		FlightsSearchScreen.clickSearchButton();
		ScreenActions.enterLog(TAG, "Flight search results loaded");
		FlightsSearchResultsScreen.clickListItem(2);
		FlightLegScreen.clickSelectFlightButton();
		clickCheckoutButton();

		// Check
		verifyNameMustMatchIdWarning();
		verifyRulesAndRestrictionsButton();
		verifyMissingTravelerInformationAlerts();
		verifyNameMustMatchIdWarningWithInfoEntered();
		verifyNameMustMatchIdWarningSecondTraveler();
		verifyMissingCardInfoAlerts();
		verifyLoginButtonNotAppearing();
	}

	private void verifyNameMustMatchIdWarning() {
		// Warning should appear on opening traveler details
		// and close when the user taps the screen.
		ScreenActions.enterLog(TAG, "Start testing name must match id warning in user info");
		ScreenActions.delay(2);
		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(0);
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		FlightsTravelerInfoScreen.nameMustMatchTextView().perform(click());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(not(isCompletelyDisplayed())));
		Espresso.pressBack();

		// Warning should still be present on subsequent details entry
		// and close when the user starts typing.
		ScreenActions.delay(2);
		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(0);
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		FlightsTravelerInfoScreen.enterFirstName("foo");
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(not(isCompletelyDisplayed())));
		Espresso.pressBack();
	}

	private void verifyNameMustMatchIdWarningWithInfoEntered() {
		// Warning behavior should persist after traveler info has been entered and saved.
		ScreenActions.enterLog(TAG, "Start testing name must match id warning with info already entered");
		ScreenActions.delay(2);
		FlightsTravelerInfoScreen.clickPopulatedTravelerDetails(0);
		FlightsTravelerInfoScreen.clickEditTravelerInfo();
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		FlightsTravelerInfoScreen.nameMustMatchTextView().perform(click());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(not(isCompletelyDisplayed())));
		Espresso.pressBack();
		Espresso.pressBack();
	}

	private void verifyNameMustMatchIdWarningSecondTraveler() {
		// Warning behavior should persist upon entry of additional travelers' info.
		ScreenActions.enterLog(TAG, "Start testing name must match id warning for a second traveler's info");
		ScreenActions.delay(2);
		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(2);
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		FlightsTravelerInfoScreen.nameMustMatchTextView().perform(click());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(not(isCompletelyDisplayed())));
		Espresso.pressBack();

		// Warning should disappear when user starts filling out traveler info.
		ScreenActions.delay(2);
		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(2);
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		FlightsTravelerInfoScreen.enterFirstName("Flight");
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(not(isCompletelyDisplayed())));
		FlightsTravelerInfoScreen.enterLastName("Bookings");
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.lastNameEditText());
		FlightsTravelerInfoScreen.clickBirthDateButton();
		try {
			FlightsTravelerInfoScreen.clickDoneString();
		}
		catch (Exception e) {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(not(isCompletelyDisplayed())));
		FlightsTravelerInfoScreen.clickNextButton();
		FlightsTravelerInfoScreen.clickDoneButton();

		// Warning should appear when populated second traveler details are clicked.
		FlightsTravelerInfoScreen.clickPopulatedTravelerDetails(2);
		FlightsTravelerInfoScreen.clickEditTravelerInfo();
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		FlightsTravelerInfoScreen.nameMustMatchTextView().perform(click());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(not(isCompletelyDisplayed())));
		Espresso.pressBack();
		Espresso.pressBack();
	}

	private void verifyRulesAndRestrictionsButton() {
		ScreenActions.delay(2);
		CommonCheckoutScreen.flightsLegalTextView().perform(scrollTo());
		CommonCheckoutScreen.flightsLegalTextView().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed("Privacy Policy");
		EspressoUtils.assertViewWithTextIsDisplayed("Terms and Conditions");
		EspressoUtils.assertViewWithTextIsDisplayed("Rules and Restrictions");
		Espresso.pressBack();
	}

	private void verifyMissingTravelerInformationAlerts() {
		ScreenActions.enterLog(TAG, "Starting testing of traveler info screen response when fields are left empty");
		ScreenActions.delay(2);
		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(0);
		FlightsTravelerInfoScreen.clickNextButton();
		ScreenActions.enterLog(TAG, "Verifying all fields show error icon when empty and 'DONE' is pressed");
		FlightsTravelerInfoScreen.firstNameEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.lastNameEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.birthDateSpinnerButton().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.middleNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.clickBirthDateButton();
		try {
			FlightsTravelerInfoScreen.clickDoneString();
		}
		catch (Exception e) {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		FlightsTravelerInfoScreen.enterFirstName("Expedia");
		FlightsTravelerInfoScreen.clickNextButton();

		ScreenActions.enterLog(TAG, "Verifying all field but first and middle name fields show error when 'Done' is pressed.");
		FlightsTravelerInfoScreen.firstNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.lastNameEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.middleNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.enterLastName("Mobile");
		FlightsTravelerInfoScreen.clickNextButton();

		ScreenActions.enterLog(TAG, "Verifying all field but first,last and middle name fields show error when 'Done' is pressed.");
		FlightsTravelerInfoScreen.firstNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.lastNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.middleNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		ScreenActions.enterLog(TAG, "Verifying that phone number must be at least 3 chars long");
		FlightsTravelerInfoScreen.enterPhoneNumber("95");
		FlightsTravelerInfoScreen.clickNextButton();
		FlightsTravelerInfoScreen.firstNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.lastNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.middleNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.phoneNumberEditText().perform(clearText());
		FlightsTravelerInfoScreen.enterPhoneNumber("951");
		FlightsTravelerInfoScreen.firstNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.lastNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.middleNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().perform(clearText());
		FlightsTravelerInfoScreen.enterPhoneNumber("9510000000");
		FlightsTravelerInfoScreen.clickNextButton();

		//Verify that the redress EditText allows a max of 7 chars, numbers only
		FlightsTravelerInfoScreen.typeRedressText("12345678");
		FlightsTravelerInfoScreen.redressEditText().check(matches(withText("1234567")));
		ScreenActions.enterLog(TAG, "Asserted that redress EditText has a max capacity of 7 chars");
		FlightsTravelerInfoScreen.clickDoneButton();
		logInButton().check(matches(isDisplayed()));
		ScreenActions.enterLog(TAG, "After all traveler info was entered, the test was able to return to the checkout screen");
	}

	private void verifyMissingCardInfoAlerts() {
		ScreenActions.delay(2);
		onView(withText("Payment details")).perform(click());
		CardInfoScreen.clickNextButton();

		BillingAddressScreen.addressLineOneEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		BillingAddressScreen.addressLineTwoEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.cityEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		BillingAddressScreen.stateEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		BillingAddressScreen.postalCodeEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		BillingAddressScreen.typeTextAddressLineOne(mUser.getAddressLine1());
		BillingAddressScreen.addressLineOneEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.addressLineTwoEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.cityEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		BillingAddressScreen.stateEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		BillingAddressScreen.postalCodeEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		BillingAddressScreen.typeTextState(mUser.getAddressStateCode());
		BillingAddressScreen.addressLineOneEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.addressLineTwoEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.cityEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		BillingAddressScreen.stateEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.postalCodeEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		BillingAddressScreen.typeTextCity(mUser.getAddressCity());
		BillingAddressScreen.addressLineOneEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.addressLineTwoEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.cityEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.stateEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.postalCodeEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		BillingAddressScreen.typeTextPostalCode(mUser.getAddressPostalCode());
		BillingAddressScreen.addressLineOneEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.addressLineTwoEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.cityEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.stateEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.postalCodeEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));

		BillingAddressScreen.clickNextButton();
		CardInfoScreen.clickOnDoneButton();

		CardInfoScreen.creditCardNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.expirationDateButton().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		ScreenActions.enterLog(TAG, "CC, name on card, expiration date, email address views all have error icon");

		CardInfoScreen.typeTextCreditCardEditText(mUser.getCreditCardNumber().substring(0, 12));
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.expirationDateButton().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.creditCardNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		ScreenActions.enterLog(TAG, "Successfully asserted that 12 chars is too short for CC edittext");
		CardInfoScreen.creditCardNumberEditText().perform(clearText());

		String twentyChars = "12345123451234512345";
		String nineteenChars = twentyChars.substring(0, 19);
		CardInfoScreen.typeTextCreditCardEditText(twentyChars);
		CardInfoScreen.creditCardNumberEditText().check(matches(withText(nineteenChars)));
		ScreenActions.enterLog(TAG, "Successfully asserted that the CC edittext has a max capacity of 19 chars");
		CardInfoScreen.creditCardNumberEditText().perform(clearText());

		CardInfoScreen.typeTextCreditCardEditText(mUser.getCreditCardNumber());
		CardInfoScreen.expirationDateButton().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		ScreenActions.enterLog(TAG, "After entering CC number, the CC edit text no longer has error icon");

		/* test cc expiration date validation only if the current month is not January
		* there's no way to enter month in the past in the month of January
		 */

		if (LocalDate.now().getMonthOfYear() != 1) {
			CardInfoScreen.clickOnExpirationDateButton();
			CardInfoScreen.clickMonthDownButton();
			CardInfoScreen.clickSetButton();
			CardInfoScreen.clickOnDoneButton();
			CardInfoScreen.expirationDateButton().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
			CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
			CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
			CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
			ScreenActions.enterLog(TAG, "Successfully asserted that the expiration date cannot be in the past!");
		}
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.expirationDateButton().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		ScreenActions.enterLog(TAG, "After entering expiration date, that field no longer has error icon");

		CardInfoScreen.typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
		CardInfoScreen.expirationDateButton().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		ScreenActions.enterLog(TAG, "After entering cardholder name, that edit text no longer has error icon");

		CardInfoScreen.typeTextEmailEditText("deepanshumadan");
		CardInfoScreen.expirationDateButton().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.clickOnDoneButton();
		ScreenActions.enterLog(TAG, "Successfully asserted that an email address with no '@' or TLD is found invalid");
		CardInfoScreen.emailEditText().perform(clearText());

		CardInfoScreen.typeTextEmailEditText("deepanshumadan@");
		CardInfoScreen.expirationDateButton().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.clickOnDoneButton();
		ScreenActions.enterLog(TAG, "Successfully asserted that an email address with no website or TLD is found invalid");
		CardInfoScreen.emailEditText().perform(clearText());

		CardInfoScreen.typeTextEmailEditText("deepanshumadan@expedia.");
		CardInfoScreen.expirationDateButton().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.clickOnDoneButton();
		ScreenActions.enterLog(TAG, "Successfully asserted that an email address with no TLD is found invalid");
		CardInfoScreen.emailEditText().perform(clearText());

		CardInfoScreen.typeTextEmailEditText(mUser.getLoginEmail());
		CardInfoScreen.expirationDateButton().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.emailEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		ScreenActions.enterLog(TAG, "After entering email address, that edit text no longer has error icon");
		CardInfoScreen.clickOnDoneButton();

		logInButton().perform(scrollTo());
		logInButton().check(matches(isDisplayed()));
		ScreenActions.enterLog(TAG, "After all card info was added, the test was able to return to the checkout screen");
	}

	private void verifyLoginButtonNotAppearing() throws Exception {
		Common.pressBack();
		clickCheckoutButton();
		logInButton().perform(scrollTo(), click());
		ScreenActions.delay(2);

		LogInScreen.facebookButton().check(matches(isDisplayed()));
		LogInScreen.logInButton().check(matches(not(isDisplayed())));
		ScreenActions.enterLog(TAG, "Log in button isn't shown until an email address is entered");
		LogInScreen.typeTextEmailEditText(mUser.getLoginEmail());
		LogInScreen.facebookButton().check(matches(not(isDisplayed())));
		ScreenActions.enterLog(TAG, "Facebook button is no longer shown after email address is entered");
		LogInScreen.clickOnLoginButton();
		LogInScreen.logInButton().check(matches(isDisplayed()));
		ScreenActions.enterLog(TAG, "Log in button is shown after email address is entered");
		LogInScreen.typeTextPasswordEditText(mUser.getLoginPassword());
		LogInScreen.clickOnLoginButton();
		Espresso.pressBack();
		clickCheckoutButton();
		ScreenActions.delay(2);

		logOutButton().perform(scrollTo());
		EspressoUtils.assertViewWithTextIsDisplayed(mUser.getLoginEmail());
		ScreenActions.enterLog(TAG, "Was able to log in, and the email used is now visible from the checkout screen");
		clickLogOutButton();
		onView(withText(mRes.getString(R.string.sign_out))).perform(click());
		logInButton().check(matches(isDisplayed()));
		ScreenActions.enterLog(TAG, "Log out button was visible and able to be clicked. Email address no longer visible on checkout screen");
	}
}
