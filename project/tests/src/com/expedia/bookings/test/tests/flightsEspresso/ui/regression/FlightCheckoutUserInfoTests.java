package com.expedia.bookings.test.tests.flightsEspresso.ui.regression;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.BillingAddressScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LogInScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightLegScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.mobiata.android.util.SettingUtils;

import static com.expedia.bookings.test.utilsEspresso.CustomMatchers.withCompoundDrawable;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by dmadan on 5/8/14.
 */
public class FlightCheckoutUserInfoTests extends ActivityInstrumentationTestCase2<LaunchActivity> {
	public FlightCheckoutUserInfoTests() {
		super(LaunchActivity.class);
	}

	private static final String TAG = FlightCheckoutUserInfoTests.class.getSimpleName();
	Context mContext;
	Resources mRes;
	HotelsUserData mUser;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mRes = mContext.getResources();
		mUser = new HotelsUserData(getInstrumentation());
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		SettingUtils.save(mContext, R.id.preference_suppress_flight_booking_checkbox, "true");
		getActivity();
	}

	public void testCheckFlights() throws Exception {
		ScreenActions.enterLog(TAG, "Launching flights application");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.enterArrivalAirport("LAS");
		FlightsSearchScreen.clickSelectDepartureButton();
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		LocalDate mStartDate = new LocalDate(year, month, 5);
		LocalDate mEndDate = new LocalDate(year, month, 1);
		FlightsSearchScreen.clickDate(mStartDate, mEndDate);
		ScreenActions.enterLog(TAG, "Click search button");
		FlightsSearchScreen.clickSearchButton();
		ScreenActions.enterLog(TAG, "Flight search results loaded");
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		FlightsCheckoutScreen.clickCheckoutButton();
		verifyRulesAndRestrictionsButton();
		verifyMissingTravelerInformationAlerts();
		verifyMissingCardInfoAlerts();
		verifyLoginButtonNotAppearing();
	}

	private void verifyRulesAndRestrictionsButton() {
		CommonCheckoutScreen.flightsLegalTextView().perform(click());
		EspressoUtils.assertTrue("Privacy Policy");
		EspressoUtils.assertTrue("Terms and Conditions");
		EspressoUtils.assertTrue("Rules and Restrictions");
		Espresso.pressBack();
	}

	private void verifyMissingTravelerInformationAlerts() {
		ScreenActions.enterLog(TAG, "Starting testing of traveler info screen response when fields are left empty");
		ScreenActions.delay(1);
		FlightsTravelerInfoScreen.clickTravelerDetails();
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
		FlightsCheckoutScreen.logInButton().check(matches(isDisplayed()));
		ScreenActions.enterLog(TAG, "After all traveler info was entered, the test was able to return to the checkout screen");
	}

	private void verifyMissingCardInfoAlerts() {
		ScreenActions.delay(1);
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

		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthDownButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.expirationDateButton().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		ScreenActions.enterLog(TAG, "Successfully asserted that the expiration date cannot be in the past!");

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

		FlightsCheckoutScreen.logInButton().check(matches(isDisplayed()));
		ScreenActions.enterLog(TAG, "After all card info was added, the test was able to return to the checkout screen");
	}

	private void verifyLoginButtonNotAppearing() throws Exception {
		FlightsCheckoutScreen.logInButton().perform(scrollTo(), click());
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
		FlightsCheckoutScreen.clickCheckoutButton();
		EspressoUtils.assertTrue(mUser.getLoginEmail());
		ScreenActions.enterLog(TAG, "Was able to log in, and the email used is now visible from the checkout screen");
		FlightsCheckoutScreen.clickLogOutButton();
		onView(withText("Log Out")).perform(click());
		FlightsCheckoutScreen.logInButton().check(matches(isDisplayed()));
		ScreenActions.enterLog(TAG, "Log out button was visible and able to be clicked. Email address no longer visible on checkout screen");
	}
}
