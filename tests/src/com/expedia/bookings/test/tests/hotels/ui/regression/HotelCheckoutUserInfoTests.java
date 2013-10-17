package com.expedia.bookings.test.tests.hotels.ui.regression;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;

public class HotelCheckoutUserInfoTests extends CustomActivityInstrumentationTestCase<SearchActivity> {

	private static final String TAG = HotelCheckoutUserInfoTests.class.getSimpleName();

	public HotelCheckoutUserInfoTests() {
		super(SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mDriver.delay();
		mDriver.launchScreen().openMenuDropDown();
		mDriver.launchScreen().pressSettings();
		mDriver.settingsScreen().setSpoofBookings();
		mDriver.goBack();
		mDriver.delay();
	}

	public void testCheckHotels() throws Exception {
		final int numberOfHotels = 3;
		for (int i = 0; i < numberOfHotels; i++) {
			mDriver.launchScreen().launchHotels();
			mDriver.delay();
			mUser.setHotelCityToRandomUSCity();
			mDriver.hotelsSearchScreen().clickSearchEditText();
			mDriver.hotelsSearchScreen().clickToClearSearchEditText();
			mDriver.enterLog(TAG, "Hotel search city is: " + mUser.getHotelSearchCity());
			mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
			mDriver.hotelsSearchScreen().clickOnGuestsButton();
			mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
			mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
			mDriver.hotelsSearchScreen().selectHotelFromList(1);
			mDriver.delay();
			mDriver.hotelsDetailsScreen().clickSelectButton();
			mDriver.waitForStringToBeGone(mDriver.hotelsRoomsRatesScreen().findingAvailableRooms());
			mDriver.hotelsRoomsRatesScreen().selectRoom(0);
			mDriver.waitForStringToBeGone(mDriver.hotelsCheckoutScreen().calculatingTaxesAndFees());
			// A dialog pops up for a hotel, just skip that iteration.
			if (!handleDialogPopupPresent()) {
				mDriver.enterLog(TAG, "Hotel name is: "
						+ mDriver.hotelsCheckoutScreen().hotelNameView().getText().toString());
				mDriver.hotelsCheckoutScreen().clickCheckoutButton();
				mDriver.delay(3);
				verifyEnterCouponButton();
				verifyRulesAndRestrictionsButton();
				verifyMissingTravelerInformationAlerts();
				verifyMissingCardInfoAlerts();
				verifyLoginButtonNotAppearing();
				mDriver.hotelsCheckoutScreen().slideToCheckout();
				doBookingAndReturnToLaunchScreen();
				mDriver.launchScreen().openMenuDropDown();
				mDriver.launchScreen().pressSettings();
				mDriver.settingsScreen().clickToClearPrivateData();
				mDriver.delay(1);
				mDriver.settingsScreen().clickOKString();
				mDriver.delay(1);
				mDriver.settingsScreen().clickOKString();
				mDriver.delay(1);
				mDriver.goBack();
				mDriver.delay(1);
			}
			else {
				if (!handleDialogPopupPresent()) {
					mDriver.goBack();
				}
				mDriver.goBack();
				mDriver.goBack();
				continue;
			}
		}
	}

	private void verifyMissingTravelerInformationAlerts() {
		mDriver.enterLog(TAG, "Starting testing of traveler info screen response when fields are left empty");
		mDriver.hotelsCheckoutScreen().clickAddTravelerButton();
		mDriver.travelerInformationScreen().clickEnterANewTraveler();
		mDriver.travelerInformationScreen().clickDoneButton();
		mDriver.enterLog(TAG, "Verifying all fields show error icon when empty and 'DONE' is pressed");
		assertEquals(errorIcon(), firstNameETBitmap());
		assertEquals(null, middleNameETBitmap());
		assertEquals(errorIcon(), lastNameETBitmap());
		assertEquals(errorIcon(), phoneNumberETBitmap());
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.travelerInformationScreen().enterFirstName(mUser.getFirstName());
		mDriver.travelerInformationScreen().clickDoneButton();
		mDriver.enterLog(TAG, "Verifying all field but first and middle name fields show error when 'Done' is pressed.");
		mDriver.enterLog(TAG, "The text in first name field is: "
				+ mDriver.travelerInformationScreen().firstNameEditText().getText().toString());
		assertEquals(null, firstNameETBitmap());
		assertEquals(null, middleNameETBitmap());
		assertEquals(errorIcon(), lastNameETBitmap());
		assertEquals(errorIcon(), phoneNumberETBitmap());
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.travelerInformationScreen().enterLastName(mUser.getLastName());
		mDriver.travelerInformationScreen().clickDoneButton();
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
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.travelerInformationScreen().enterPhoneNumber(mUser.getPhoneNumber());
		mDriver.travelerInformationScreen().clickDoneButton();
		mDriver.enterLog(TAG,
				"Verifying email address edit text shows error when 'Done' is pressed and it is empty.");
		mDriver.enterLog(TAG, "The text in first name field is: "
				+ mDriver.travelerInformationScreen().firstNameEditText().getText().toString());
		mDriver.enterLog(TAG, "The text in last name field is: "
				+ mDriver.travelerInformationScreen().lastNameEditText().getText().toString());
		mDriver.enterLog(TAG, "The text in phone name field is: "
				+ mDriver.travelerInformationScreen().phoneNumberEditText().getText().toString());
		assertEquals(null, firstNameETBitmap());
		assertEquals(null, middleNameETBitmap());
		assertEquals(null, lastNameETBitmap());
		assertEquals(null, phoneNumberETBitmap());
		assertEquals(errorIcon(), emailAddressETBitmap());
		mDriver.travelerInformationScreen().enterEmailAddress(mUser.getLoginEmail());
		mDriver.travelerInformationScreen().clickDoneButton();
		mDriver.delay();
		assertTrue(mDriver.hotelsCheckoutScreen().logInButton().isShown());
		mDriver.enterLog(TAG, "After all traveler info was entered, the test was able to return to the checkout screen");
	}

	private void verifyMissingCardInfoAlerts() {
		mDriver.hotelsCheckoutScreen().clickSelectPaymentButton();
		mDriver.cardInfoScreen().clickOnDoneButton();
		mDriver.delay(1);
		assertEquals(errorIcon(), creditCardETBitmap());
		assertEquals(null, postalCodeETBitmap());
		assertEquals(errorIcon(), nameOnCardETBitmap());
		assertEquals(errorIcon(), expirationDateTextViewBitmap());
		mDriver.enterLog(
				TAG,
				"Credit card edit text, name on card edit text, and expiration date text view all display error icon when data isn't entered.");
		mDriver.cardInfoScreen().clickOnExpirationDateButton();
		mDriver.cardInfoScreen().clickSetButton();
		mDriver.cardInfoScreen().clickOnDoneButton();
		assertEquals(errorIcon(), creditCardETBitmap());
		assertEquals(null, postalCodeETBitmap());
		assertEquals(errorIcon(), nameOnCardETBitmap());
		assertEquals(null, expirationDateTextViewBitmap());
		mDriver.enterLog(TAG,
				"Credit card edit text, name on card edit text both display error icon when data isn't entered.");
		mDriver.cardInfoScreen().typeTextCreditCardEditText(mUser.getCreditCardNumber());
		mDriver.cardInfoScreen().clickOnDoneButton();
		assertEquals(null, creditCardETBitmap());
		assertEquals(null, postalCodeETBitmap());
		assertEquals(errorIcon(), nameOnCardETBitmap());
		assertEquals(null, expirationDateTextViewBitmap());
		mDriver.enterLog(TAG, "Name on card edit text displays error icon when data isn't entered.");
		mDriver.cardInfoScreen().typeTextNameOnCardEditText(mUser.getFirstName() + mUser.getLastName());
		mDriver.cardInfoScreen().clickOnDoneButton();
		mDriver.delay(1);
		assertTrue(mDriver.hotelsCheckoutScreen().logInButton().isShown());
		mDriver.enterLog(TAG, "After all card info was added, the test was able to return to the checkout screen");
	}

	private void verifyLoginButtonNotAppearing() throws Exception {
		mDriver.hotelsCheckoutScreen().clickLogInButton();
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
		mDriver.hotelsCheckoutScreen().clickLogOutButton();
		assertFalse(mDriver.searchText(mUser.getLoginEmail(), 1, false, true));
		mDriver.enterLog(TAG,
				"Log out button was visible and able to be clicked. Email address no longer visible on checkout screen");
	}

	private void verifyEnterCouponButton() {
		mDriver.hotelsCheckoutScreen().clickOnEnterCouponButton();
		mDriver.delay(1);
		assertTrue(mDriver.searchText(mDriver.hotelsCheckoutScreen().enterCouponCode()));
		assertTrue(mDriver.hotelsCheckoutScreen().couponCodeEditText().isShown());
		mDriver.enterLog(TAG, "After clicking coupon button, the proper header text and the edit text are shown");
		mDriver.goBack();
		if (mDriver.searchText(mDriver.hotelsCheckoutScreen().enterCouponCode(), 1, false, true)) {
			mDriver.goBack();
		}
	}

	private void verifyRulesAndRestrictionsButton() {
		mDriver.hotelsCheckoutScreen().clickOnLegalInfoButton();
		assertTrue(mDriver.searchText(mDriver.hotelsTermsAndConditionsScreen().bestPriceGuarantee()));
		assertTrue(mDriver.searchText(mDriver.hotelsTermsAndConditionsScreen().privacyPolicy()));
		assertTrue(mDriver.searchText(mDriver.hotelsTermsAndConditionsScreen().termsAndConditions()));
		assertTrue(mDriver.searchText(mDriver.hotelsTermsAndConditionsScreen().cancellationPolicy()));
		mDriver.enterLog(TAG, "After clicking rules & restrictions button, expected strings are displayed.");
		mDriver.goBack();
	}

	private void doBookingAndReturnToLaunchScreen() throws Exception {
		mDriver.cvvEntryScreen().parseAndEnterCVV("111");
		mDriver.cvvEntryScreen().clickBookButton();
		mDriver.waitForStringToBeGone(mDriver.cvvEntryScreen().booking());
		assertTrue(mDriver.searchText(mDriver.hotelsConfirmationScreen().bookingComplete()));
		mDriver.hotelsConfirmationScreen().clickDoneButton();
		mDriver.delay();
		mDriver.tripsScreen().swipeToLaunchScreen();
		mDriver.enterLog(TAG, "Back at launch screen.");
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

	private boolean handleDialogPopupPresent() {
		String okString = mRes.getString(R.string.ok);
		if (mDriver.searchText(okString, 1, false, true)) {
			mDriver.enterLog(TAG, "A dialog appeared. Pressing the OK button.");
			mDriver.clickOnText(okString);
			return true;
		}
		return false;
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
