package com.expedia.bookings.test.tests.flights.ui.regression;

import org.joda.time.LocalDate;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.test.utils.FlightsTestDriver;

public class FlightCheckoutUserInfoTests extends CustomActivityInstrumentationTestCase<LaunchActivity> {

	private static final String TAG = FlightCheckoutUserInfoTests.class.getSimpleName();
	FlightsTestDriver mDriver;

	public FlightCheckoutUserInfoTests() {
		super(LaunchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mDriver.delay();
		mDriver.launchScreen().openMenuDropDown();
		mDriver.launchScreen().pressSettings();
		mDriver.settingsScreen().setSpoofBookings();
		mDriver.goBack();
		mDriver.delay();
	}

	public void testCheckFlights() throws Exception {
		mDriver.launchScreen().launchFlights();
		mDriver.delay();
		mUser.setAirportsToRandomUSAirports();
		mDriver.flightsSearchScreen().clickDepartureAirportField();
		mDriver.enterLog(TAG, "Set departure airport: " + mUser.getDepartureAirport());
		mDriver.flightsSearchScreen().enterDepartureAirport(mUser.getDepartureAirport());
		mDriver.enterLog(TAG, "Set arrival airport: " + mUser.getArrivalAirport());
		mDriver.flightsSearchScreen().enterArrivalAirport(mUser.getArrivalAirport());
		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		int dateOffset = 1;
		mDriver.enterLog(TAG, "Selecting date with offset from current date: " + dateOffset);
		mDriver.flightsSearchScreen().clickDate(dateOffset);
		mDriver.enterLog(TAG, "Click search button");
		mDriver.flightsSearchScreen().clickSearchButton();
		mDriver.waitForStringToBeGone(mDriver.flightsSearchLoading().getLoadingFlightsString());
		mDriver.flightsSearchResultsScreen().selectFlightFromList(0);
		mDriver.delay();
		mDriver.flightLegScreen().clickSelectFlightButton();
		mDriver.waitForStringToBeGone(mDriver.flightLegScreen().checkingForPriceChangesString());
		mDriver.delay();
		mDriver.flightsCheckoutScreen().clickCheckoutButton();
		mDriver.delay(10);
		verifyRulesAndRestrictionsButton();
		verifyMissingTravelerInformationAlerts();
		verifyMissingCardInfoAlerts();
		verifyLoginButtonNotAppearing();

	}

	private void verifyMissingTravelerInformationAlerts() {
		mDriver.enterLog(TAG, "Starting testing of traveler info screen response when fields are left empty");
		mDriver.flightsCheckoutScreen().clickTravelerDetailsButton();
		mDriver.travelerInformationScreen().clickNextButton();
		mDriver.enterLog(TAG, "Verifying all fields show error icon when empty and 'DONE' is pressed");
		assertEquals(errorIcon(), firstNameETBitmap());
		assertEquals(null, middleNameETBitmap());
		assertEquals(errorIcon(), lastNameETBitmap());
		assertEquals(errorIcon(), phoneNumberETBitmap());
		assertEquals(errorIcon(), dateOfBirthTextViewBitmap());
		mDriver.travelerInformationScreen().clickBirthDateButton();
		mDriver.delay();
		mDriver.travelerInformationScreen().clickSetButton();
		mDriver.delay();
		assertEquals(errorIcon(), firstNameETBitmap());
		assertEquals(null, middleNameETBitmap());
		assertEquals(errorIcon(), lastNameETBitmap());
		assertEquals(errorIcon(), phoneNumberETBitmap());
		assertEquals(null, dateOfBirthTextViewBitmap());
		mDriver.travelerInformationScreen().enterFirstName(mUser.getFirstName());
		mDriver.travelerInformationScreen().clickNextButton();
		mDriver.enterLog(TAG, "Verifying all field but first and middle name fields show error when 'Done' is pressed.");
		mDriver.enterLog(TAG, "The text in first name field is: "
				+ mDriver.travelerInformationScreen().firstNameEditText().getText().toString());
		assertEquals(null, firstNameETBitmap());
		assertEquals(null, middleNameETBitmap());
		assertEquals(errorIcon(), lastNameETBitmap());
		assertEquals(errorIcon(), phoneNumberETBitmap());
		mDriver.travelerInformationScreen().enterLastName(mUser.getLastName());
		mDriver.travelerInformationScreen().clickNextButton();
		mDriver.enterLog(TAG,
				"Verifying all field but first, middle, last name fields show error when 'Done' is pressed and they are empty.");
		mDriver.enterLog(TAG, "The text in first name field is: "
				+ mDriver.travelerInformationScreen().firstNameEditText().getText().toString());
		mDriver.enterLog(TAG, "The text in last name field is: "
				+ mDriver.travelerInformationScreen().lastNameEditText().getText().toString());
		assertEquals(null, firstNameETBitmap());
		assertEquals(null, middleNameETBitmap());
		assertEquals(null, lastNameETBitmap());
		assertEquals(errorIcon(), phoneNumberETBitmap());

		mDriver.enterLog(TAG, "Verifying that phone number must be at least 3 chars long");
		mDriver.travelerInformationScreen().enterPhoneNumber(mUser.getPhoneNumber().substring(0, 2));
		mDriver.travelerInformationScreen().clickNextButton();
		assertEquals(null, firstNameETBitmap());
		assertEquals(null, middleNameETBitmap());
		assertEquals(null, lastNameETBitmap());
		assertEquals(errorIcon(), phoneNumberETBitmap());
		mDriver.travelerInformationScreen().clearEditText(mDriver.travelerInformationScreen().phoneNumberEditText());
		mDriver.travelerInformationScreen().enterPhoneNumber(mUser.getPhoneNumber().substring(0, 3));
		assertEquals(null, firstNameETBitmap());
		assertEquals(null, middleNameETBitmap());
		assertEquals(null, lastNameETBitmap());
		assertEquals(null, phoneNumberETBitmap());
		mDriver.travelerInformationScreen().clearEditText(mDriver.travelerInformationScreen().phoneNumberEditText());
		mDriver.travelerInformationScreen().enterPhoneNumber(mUser.getPhoneNumber());
		mDriver.travelerInformationScreen().clickNextButton();
		mDriver.delay();

		//Verify that the redress EditText allows a max of 7 chars, numbers only
		mDriver.travelerInformationScreen().typeRedressText("12345678");
		String redressText = mDriver.travelerInformationScreen().redressEditText().getText().toString();
		assertEquals("1234567", redressText);
		mDriver.enterLog(TAG,
				"Asserted that redress EditText has a max capacity of 7 chars");

		mDriver.travelerInformationScreen().clickDoneButton();
		mDriver.delay(1);
		assertTrue(mDriver.flightsCheckoutScreen().logInButton().isShown());
		mDriver.enterLog(TAG, "After all traveler info was entered, the test was able to return to the checkout screen");
	}

	private void verifyMissingCardInfoAlerts() {
		mDriver.flightsCheckoutScreen().clickSelectPaymentButton();
		mDriver.cardInfoScreen().clickNextButton();

		assertEquals(errorIcon(), addressLineOneETBitmap());
		assertEquals(null, addressLineTwoETBitmap());
		assertEquals(errorIcon(), cityETBitmap());
		assertEquals(errorIcon(), stateETBitmap());
		assertEquals(errorIcon(), postalCodeETBitmap());

		mDriver.billingAddressScreen().typeTextAddressLineOne(mUser.getAddressLine1());
		assertEquals(null, addressLineOneETBitmap());
		assertEquals(null, addressLineTwoETBitmap());
		assertEquals(errorIcon(), cityETBitmap());
		assertEquals(errorIcon(), stateETBitmap());
		assertEquals(errorIcon(), postalCodeETBitmap());

		mDriver.billingAddressScreen().typeTextState(mUser.getAddressStateCode());
		assertEquals(null, addressLineOneETBitmap());
		assertEquals(null, addressLineTwoETBitmap());
		assertEquals(errorIcon(), cityETBitmap());
		assertEquals(null, stateETBitmap());
		assertEquals(errorIcon(), postalCodeETBitmap());

		mDriver.billingAddressScreen().typeTextCity(mUser.getAddressCity());
		assertEquals(null, addressLineOneETBitmap());
		assertEquals(null, addressLineTwoETBitmap());
		assertEquals(null, cityETBitmap());
		assertEquals(null, stateETBitmap());
		assertEquals(errorIcon(), postalCodeETBitmap());

		mDriver.billingAddressScreen().typeTextPostalCode(mUser.getAddressPostalCode());
		assertEquals(null, addressLineOneETBitmap());
		assertEquals(null, addressLineTwoETBitmap());
		assertEquals(null, cityETBitmap());
		assertEquals(null, stateETBitmap());
		assertEquals(null, postalCodeETBitmap());

		mDriver.billingAddressScreen().clickNextButton();
		mDriver.delay(1);
		mDriver.cardInfoScreen().clickOnDoneButton();

		assertEquals(errorIcon(), creditCardETBitmap());
		assertEquals(errorIcon(), nameOnCardETBitmap());
		assertEquals(errorIcon(), expirationDateTextViewBitmap());
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.enterLog(TAG,
				"CC, name on card, expiration date, email address views all have error icon");

		mDriver.cardInfoScreen().clickOnExpirationDateButton();
		mDriver.cardInfoScreen().clickMonthDownButton();
		mDriver.cardInfoScreen().clickSetButton();
		mDriver.cardInfoScreen().clickOnDoneButton();
		//This check doesn't work for January. If it's January, the user cannot pick a date in the past.
		if (LocalDate.now().getMonthOfYear() != 1) {
			assertEquals(errorIcon(), expirationDateTextViewBitmap());
		}
		assertEquals(errorIcon(), creditCardETBitmap());
		assertEquals(errorIcon(), nameOnCardETBitmap());
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.enterLog(TAG, "Successfully asserted that the expiration date cannot be in the past!");

		mDriver.cardInfoScreen().clickOnExpirationDateButton();
		mDriver.cardInfoScreen().clickYearUpButton();
		mDriver.cardInfoScreen().clickSetButton();
		mDriver.cardInfoScreen().clickOnDoneButton();
		assertEquals(null, expirationDateTextViewBitmap());
		assertEquals(errorIcon(), creditCardETBitmap());
		assertEquals(errorIcon(), nameOnCardETBitmap());
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.enterLog(TAG, "After entering expiration date, that field no longer has error icon");

		mDriver.cardInfoScreen().typeTextCreditCardEditText(mUser.getCreditCardNumber().substring(0, 12));
		mDriver.cardInfoScreen().clickOnDoneButton();
		assertEquals(errorIcon(), creditCardETBitmap());
		assertEquals(null, expirationDateTextViewBitmap());
		assertEquals(errorIcon(), nameOnCardETBitmap());
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.enterLog(TAG, "Successfully asserted that 12 chars is too short for CC edittext");
		mDriver.clearEditText(mDriver.cardInfoScreen().creditCardNumberEditText());

		String twentyChars = "12345123451234512345";
		String nineteenChars = twentyChars.substring(0, 19);
		mDriver.cardInfoScreen().typeTextCreditCardEditText(twentyChars);
		String currentCCText = mDriver.cardInfoScreen().creditCardNumberEditText().getText().toString();
		assertEquals(nineteenChars, currentCCText);
		mDriver.enterLog(TAG, "Successfully asserted that the CC edittext has a max capacity of 19 chars");
		mDriver.clearEditText(mDriver.cardInfoScreen().creditCardNumberEditText());

		mDriver.cardInfoScreen().typeTextCreditCardEditText(mUser.getCreditCardNumber());
		assertEquals(null, creditCardETBitmap());
		assertEquals(null, expirationDateTextViewBitmap());
		assertEquals(errorIcon(), nameOnCardETBitmap());
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.enterLog(TAG, "After entering CC number, the CC edit text no longer has error icon");

		mDriver.cardInfoScreen().typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
		assertEquals(null, creditCardETBitmap());
		assertEquals(null, expirationDateTextViewBitmap());
		assertEquals(null, nameOnCardETBitmap());
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.enterLog(TAG, "After entering cardholder name, that edit text no longer has error icon");

		mDriver.cardInfoScreen().typeTextEmailEditText("kevincarpenter");
		assertEquals(null, creditCardETBitmap());
		assertEquals(null, expirationDateTextViewBitmap());
		assertEquals(null, nameOnCardETBitmap());
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.cardInfoScreen().clickOnDoneButton();
		mDriver.enterLog(TAG, "Successfully asserted that an email address with no '@' or TLD is found invalid");
		mDriver.clearEditText(mDriver.cardInfoScreen().emailEditText());

		mDriver.cardInfoScreen().typeTextEmailEditText("kevincarpenter@");
		assertEquals(null, creditCardETBitmap());
		assertEquals(null, expirationDateTextViewBitmap());
		assertEquals(null, nameOnCardETBitmap());
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.cardInfoScreen().clickOnDoneButton();
		mDriver.enterLog(TAG, "Successfully asserted that an email address with no website or TLD is found invalid");
		mDriver.clearEditText(mDriver.cardInfoScreen().emailEditText());

		mDriver.cardInfoScreen().typeTextEmailEditText("kevincarpenter@expedia.");
		assertEquals(null, creditCardETBitmap());
		assertEquals(null, expirationDateTextViewBitmap());
		assertEquals(null, nameOnCardETBitmap());
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.cardInfoScreen().clickOnDoneButton();
		mDriver.enterLog(TAG, "Successfully asserted that an email address with no TLD is found invalid");
		mDriver.clearEditText(mDriver.cardInfoScreen().emailEditText());

		mDriver.cardInfoScreen().typeTextEmailEditText(mUser.getLoginEmail());
		assertEquals(null, creditCardETBitmap());
		assertEquals(null, expirationDateTextViewBitmap());
		assertEquals(null, nameOnCardETBitmap());
		assertEquals(null, emailAddressETBitmap());
		mDriver.enterLog(TAG, "After entering email address, that edit text no longer has error icon");
		mDriver.cardInfoScreen().clickOnDoneButton();
		mDriver.delay(1);

		assertTrue(mDriver.flightsCheckoutScreen().logInButton().isShown());
		mDriver.enterLog(TAG, "After all card info was added, the test was able to return to the checkout screen");
	}

	private void verifyLoginButtonNotAppearing() throws Exception {
		mDriver.flightsCheckoutScreen().clickLogInButton();
		assertTrue(mDriver.logInScreen().facebookButton().isShown());
		assertTrue(!mDriver.logInScreen().logInButton().isShown());
		mDriver.enterLog(TAG, "Log in button isn't shown until an email address is entered");
		mDriver.logInScreen().typeTextEmailEditText(mUser.getLoginEmail());
		mDriver.delay();
		assertTrue(!mDriver.logInScreen().facebookButton().isShown());
		mDriver.enterLog(TAG, "Facebook button is no longer shown after email address is entered");
		mDriver.logInScreen().clickOnLoginButton();
		mDriver.delay(1);
		assertTrue(mDriver.logInScreen().logInButton().isShown());
		mDriver.enterLog(TAG, "Log in button is shown after email address is entered");
		mDriver.logInScreen().typeTextPasswordEditText(mUser.getLoginPassword());
		mDriver.logInScreen().clickOnLoginButton();
		mDriver.waitForStringToBeGone(mDriver.logInScreen().loggingInDialogString());
		mDriver.delay();
		assertTrue(mDriver.searchText(mUser.getLoginEmail(), 1, false, true));
		mDriver.enterLog(TAG, "Was able to log in, and the email used is now visible from the checkout screen");
		mDriver.flightsCheckoutScreen().clickLogOutButton();
		mDriver.flightsCheckoutScreen().clickLogOutString();
		assertFalse(mDriver.searchText(mUser.getLoginEmail(), 1, false, true));
		mDriver.enterLog(TAG,
				"Log out button was visible and able to be clicked. Email address no longer visible on checkout screen");
	}

	private void verifyRulesAndRestrictionsButton() {
		mDriver.clickOnView(mDriver.flightsCheckoutScreen().flightsLegalTextView());
		assertTrue(mDriver.searchText(mDriver.flightsTermsAndConditionsScreen().privacyPolicy()));
		assertTrue(mDriver.searchText(mDriver.flightsTermsAndConditionsScreen().termsAndConditions()));
		mDriver.enterLog(TAG, "After clicking rules & restrictions button, expected strings are displayed.");
		mDriver.goBack();
	}

	// Helpers

	private Bitmap getBitmapFromDrawable(Drawable d) {
		BitmapDrawable bd = (BitmapDrawable) d;
		return (bd == null ? null : bd.getBitmap());
	}

	// Accessing bitmaps from edit texts

	private Bitmap errorIcon() {
		return getBitmapFromDrawable(mRes.getDrawable(R.drawable.ic_error_blue));
	}

	private Bitmap firstNameETBitmap() {
		return getBitmapFromDrawable(mDriver.travelerInformationScreen().firstNameEditText()
				.getCompoundDrawables()[2]);
	}

	private Bitmap middleNameETBitmap() {
		return getBitmapFromDrawable(mDriver.travelerInformationScreen().middleNameEditText().getCompoundDrawables()[2]);
	}

	private Bitmap lastNameETBitmap() {
		return getBitmapFromDrawable(mDriver.travelerInformationScreen().lastNameEditText().getCompoundDrawables()[2]);
	}

	private Bitmap phoneNumberETBitmap() {
		return getBitmapFromDrawable(mDriver.travelerInformationScreen().phoneNumberEditText().getCompoundDrawables()[2]);
	}

	private Bitmap emailAddressETBitmap() {
		return getBitmapFromDrawable(mDriver.travelerInformationScreen().emailEditText().getCompoundDrawables()[2]);
	}

	private Bitmap creditCardETBitmap() {
		return getBitmapFromDrawable(mDriver.cardInfoScreen().creditCardNumberEditText().getCompoundDrawables()[2]);
	}

	private Bitmap postalCodeETBitmap() {
		return getBitmapFromDrawable(mDriver.cardInfoScreen().postalCodeEditText().getCompoundDrawables()[2]);
	}

	private Bitmap nameOnCardETBitmap() {
		return getBitmapFromDrawable(mDriver.cardInfoScreen().nameOnCardEditText().getCompoundDrawables()[2]);
	}

	private Bitmap expirationDateTextViewBitmap() {
		TextView expirationDate = (TextView) mDriver.cardInfoScreen().expirationDateButton();
		return getBitmapFromDrawable(expirationDate.getCompoundDrawables()[2]);
	}

	private Bitmap dateOfBirthTextViewBitmap() {
		TextView birthDateTextView = (TextView) mDriver.travelerInformationScreen().birthDateSpinnerButton();
		return getBitmapFromDrawable(birthDateTextView.getCompoundDrawables()[2]);
	}

	private Bitmap addressLineOneETBitmap() {
		return getBitmapFromDrawable(mDriver.billingAddressScreen().addressLineOneEditText().getCompoundDrawables()[2]);
	}

	private Bitmap addressLineTwoETBitmap() {
		return getBitmapFromDrawable(mDriver.billingAddressScreen().addressLineTwoEditText().getCompoundDrawables()[2]);
	}

	private Bitmap cityETBitmap() {
		return getBitmapFromDrawable(mDriver.billingAddressScreen().cityEditText().getCompoundDrawables()[2]);
	}

	private Bitmap stateETBitmap() {
		return getBitmapFromDrawable(mDriver.billingAddressScreen().stateEditText().getCompoundDrawables()[2]);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
