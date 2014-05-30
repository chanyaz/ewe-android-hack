package com.expedia.bookings.test.tests.tablet.Flights.ui.regression;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModels.tablet.Checkout;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.LogIn;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.mobiata.android.util.SettingUtils;

import static com.expedia.bookings.test.tests.pageModels.tablet.Common.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;

/**
 * Created by dmadan on 5/29/14.
 */
public class FlightCheckoutUserInfoTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	public FlightCheckoutUserInfoTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = FlightCheckoutUserInfoTests.class.getSimpleName();
	Context mContext;
	SharedPreferences mPrefs;
	Resources mRes;
	HotelsUserData mUser;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mRes = mContext.getResources();
		mUser = new HotelsUserData(getInstrumentation());
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		getActivity();
	}

	public void testCheckFlights() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Results.clickOriginButton();
		Results.typeInOriginEditText("San Francisco, CA");
		Results.clickSuggestion("San Francisco, CA");
		Results.clickSelectFlightDates();
		int randomOffset = 20 + (int) (Math.random() * 100);
		LocalDate startDate = LocalDate.now().plusDays(randomOffset);
		Results.clickDate(startDate, null);
		Results.clickSearchNow();
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
		EspressoUtils.assertTrue("Privacy Policy");
		EspressoUtils.assertTrue("Terms and Conditions");
		EspressoUtils.assertTrue("Rules and Restrictions");
		pressBack();
	}

	private void verifyMissingTravelerInformationAlerts() {
		ScreenActions.enterLog(TAG, "Starting testing of traveler info screen response when fields are left empty");
		Checkout.clickOnTravelerDetails();
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();
		ScreenActions.enterLog(TAG, "Verifying all fields show error icon when empty and 'DONE' is pressed");
		Common.checkErrorIconDisplayed(Checkout.firstName());
		Common.checkErrorIconDisplayed(Checkout.lastName());
		Common.checkErrorIconDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());

		Checkout.enterDateOfBirth(1970, 1, 1);
		Checkout.enterFirstName("Mobiata");
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();
		ScreenActions.enterLog(TAG, "Verifying all field but first, middle name and birth date fields show error when 'Done' is pressed.");
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());

		Checkout.enterLastName("Auto");
		Common.closeSoftKeyboard(Checkout.lastName());
		Checkout.clickOnDone();
		ScreenActions.enterLog(TAG, "Verifying all field but first,last, middle name and birth date fields show error when 'Done' is pressed.");
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());

		Checkout.enterEmailAddress("aaa");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());
		ScreenActions.enterLog(TAG, "Successfully asserted that an email address with no '@' or TLD is found invalid");
		Checkout.emailAddress().perform(clearText());

		Checkout.enterEmailAddress("aaa@");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());
		ScreenActions.enterLog(TAG, "Successfully asserted that an email address with no website or TLD is found invalid");
		Checkout.emailAddress().perform(clearText());

		Checkout.enterEmailAddress("aaa@aaa");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconDisplayed(Checkout.emailAddress());
		ScreenActions.enterLog(TAG, "Successfully asserted that an email address with no TLD is found invalid");
		Checkout.emailAddress().perform(clearText());

		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();
		ScreenActions.enterLog(TAG, "Verifying now just phone number field show error when 'Done' is pressed.");
		Common.checkErrorIconNotDisplayed(Checkout.firstName());
		Common.checkErrorIconNotDisplayed(Checkout.lastName());
		Common.checkErrorIconNotDisplayed(Checkout.dateOfBirth());
		Common.checkErrorIconNotDisplayed(Checkout.middleName());
		Common.checkErrorIconDisplayed(Checkout.phoneNumber());
		Common.checkErrorIconNotDisplayed(Checkout.emailAddress());

		ScreenActions.enterLog(TAG, "Verifying that phone number must be at least 3 chars long");
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

		//Verify that the redress EditText allows a max of 7 chars, numbers only
		Checkout.clickRedressNumberButton();
		Checkout.enterRedressNumber("12345678");
		EspressoUtils.getValues("redressText", R.id.edit_redress_number);
		String redressText = mPrefs.getString("redressText", "");
		assertEquals("1234567", redressText);
		ScreenActions.enterLog(TAG, "Asserted that redress EditText has a max capacity of 7 chars");
		Checkout.clickOnDone();
		Common.checkDisplayed(Checkout.loginButton());
		ScreenActions.enterLog(TAG, "After all traveler info was entered, the test was able to return to the checkout screen");
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
		Common.checkErrorIconNotDisplayed(Checkout.address1());
		Common.checkErrorIconNotDisplayed(Checkout.addressCity());
		ScreenActions.enterLog(TAG, "Successfully asserted that 12 chars is too short for CC edittext");

		String nineteenChars = twentyChars.substring(0, 19);
		Checkout.creditCardNumber().perform(clearText());
		Checkout.enterCreditCardNumber(twentyChars);
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		EspressoUtils.getValues("Credit Card Number", R.id.edit_creditcard_number);
		String currentCCText = mPrefs.getString("Credit Card Number", "");
		assertEquals(nineteenChars, currentCCText);
		ScreenActions.enterLog(TAG, "Successfully asserted that the CC edittext has a max capacity of 19 chars");
		Checkout.creditCardNumber().perform(clearText());
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.clickOnDone();
		Common.checkErrorIconDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconDisplayed(Checkout.address1());
		Common.checkErrorIconDisplayed(Checkout.addressCity());
		ScreenActions.enterLog(TAG, "After entering CC number, the CC edit text no longer has error icon");

		Checkout.setExpirationDate(2020, 12);
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconDisplayed(Checkout.address1());
		Common.checkErrorIconDisplayed(Checkout.addressCity());
		ScreenActions.enterLog(TAG, "After entering expiration date, that field no longer has error icon");

		Checkout.enterNameOnCard("Mobiata Auto");
		Common.closeSoftKeyboard(Checkout.nameOnCard());
		Checkout.clickOnDone();
		Common.checkErrorIconNotDisplayed(Checkout.expirationDate());
		Common.checkErrorIconNotDisplayed(Checkout.creditCardNumber());
		Common.checkErrorIconNotDisplayed(Checkout.nameOnCard());
		Common.checkErrorIconDisplayed(Checkout.address1());
		Common.checkErrorIconDisplayed(Checkout.addressCity());
		ScreenActions.enterLog(TAG, "After entering cardholder name, that edit text no longer has error icon");

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
		ScreenActions.enterLog(TAG, "postal code has error icon");

		Checkout.enterPostalCode("53704");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();
		Common.checkDisplayed(Checkout.loginButton());
		ScreenActions.enterLog(TAG, "After all card info was added, the test was able to return to the checkout screen");
	}

	private void verifyLoginButtonNotAppearing() throws Exception {
		Checkout.clickLoginButton();
		Common.checkDisplayed(LogIn.loginFacebookButton());
		Common.checkNotDisplayed(LogIn.loginExpediaButton());
		ScreenActions.enterLog(TAG, "Log in button isn't shown until an email address is entered");
		LogIn.enterUserName(mUser.getLoginEmail());
		Common.checkNotDisplayed(LogIn.loginFacebookButton());
		ScreenActions.enterLog(TAG, "Facebook button is no longer shown after email address is entered");
		Common.checkDisplayed(LogIn.loginExpediaButton());
		ScreenActions.enterLog(TAG, "Log in button is shown after email address is entered");
		LogIn.enterPassword(mUser.getLoginPassword());
		LogIn.clickLoginExpediaButton();
		pressBack();
		Results.clickBookFlight();
		EspressoUtils.assertTrue(mUser.getLoginEmail());
		ScreenActions.enterLog(TAG, "Was able to log in, and the email used is now visible from the checkout screen");
		Checkout.clickLogOutButton();
		Checkout.clickLogOutString();
		Common.checkDisplayed(Checkout.loginButton());
		ScreenActions.enterLog(TAG, "Log out button was visible and able to be clicked. Log in button now visible on checkout screen");
	}
}
