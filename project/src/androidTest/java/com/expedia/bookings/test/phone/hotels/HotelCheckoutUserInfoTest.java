package com.expedia.bookings.test.phone.hotels;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelsUserData;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.mobiata.android.Log;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
import static org.hamcrest.Matchers.not;

public class HotelCheckoutUserInfoTest extends PhoneTestCase {

	private static final String TAG = HotelCheckoutUserInfoTest.class.getSimpleName();
	HotelsUserData mUser;

	public void testCheckHotels() throws Exception {
		mUser = new HotelsUserData();
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		Log.v(TAG, "Setting hotel search city to: " + "New York, NY");
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		HotelsSearchScreen.clickListItem(1);
		HotelsDetailsScreen.clickSelectButton();
		try {
			onView(withText("OK")).perform(click());
		}
		catch (Exception e) {
			Log.v(TAG, "Not available right now");
		}
		Log.v(TAG, "Selecting first room listed for this hotel.");
		HotelsRoomsRatesScreen.selectRoomItem(0);
		HotelsCheckoutScreen.clickCheckoutButton();
		verifyRulesAndRestrictionsButton();
		verifyMissingTravelerInformationAlerts();
		verifyMissingCardInfoAlerts();
		verifyLoginButtonNotAppearing();
		verifyCreditCardCleared();
	}

	private void verifyRulesAndRestrictionsButton() {
		CommonCheckoutScreen.clickOnLegalInfoButton();
		EspressoUtils.assertViewWithTextIsDisplayed("Privacy Policy");
		EspressoUtils.assertViewWithTextIsDisplayed("Terms and Conditions");
		EspressoUtils.assertViewWithTextIsDisplayed("Best Price Guarantee");
		EspressoUtils.assertViewWithTextIsDisplayed("Cancellation Policy");
		Espresso.pressBack();
	}

	private void verifyMissingTravelerInformationAlerts() {
		HotelsCheckoutScreen.clickGuestDetails();
		CommonTravelerInformationScreen.clickDoneButton();
		Log.v(TAG, "Verifying all fields show error icon when empty and 'DONE' is pressed");
		CommonTravelerInformationScreen.firstNameEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CommonTravelerInformationScreen.lastNameEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CommonTravelerInformationScreen.middleNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CommonTravelerInformationScreen.phoneNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CommonTravelerInformationScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		CommonTravelerInformationScreen.firstNameEditText().perform(click());
		CommonTravelerInformationScreen.enterFirstName("Expedia");
		CommonTravelerInformationScreen.clickDoneButton();
		Log.v(TAG, "Verifying all field but first and middle name fields show error when 'Done' is pressed.");
		CommonTravelerInformationScreen.firstNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CommonTravelerInformationScreen.lastNameEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CommonTravelerInformationScreen.middleNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CommonTravelerInformationScreen.phoneNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CommonTravelerInformationScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		CommonTravelerInformationScreen.enterLastName("Mobile");
		CommonTravelerInformationScreen.clickDoneButton();
		Log.v(TAG,
			"Verifying all field but first, middle, last name fields show error when 'Done' is pressed and they are empty.");
		CommonTravelerInformationScreen.firstNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CommonTravelerInformationScreen.lastNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CommonTravelerInformationScreen.middleNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CommonTravelerInformationScreen.phoneNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CommonTravelerInformationScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		CommonTravelerInformationScreen.enterPhoneNumber(mUser.phoneNumber);
		CommonTravelerInformationScreen.clickDoneButton();
		Log.v(TAG, "Verifying email address edit text shows error when 'Done' is pressed and it is empty.");
		CommonTravelerInformationScreen.firstNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CommonTravelerInformationScreen.lastNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CommonTravelerInformationScreen.middleNameEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CommonTravelerInformationScreen.phoneNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CommonTravelerInformationScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		CommonTravelerInformationScreen.enterEmailAddress("expedia@mobiata.com");
		CommonTravelerInformationScreen.clickDoneButton();
		HotelsCheckoutScreen.logInButton().check(matches(isDisplayed()));
		Log.v(TAG, "After all traveler info was entered, the test was able to return to the checkout screen");
	}

	private void verifyMissingCardInfoAlerts() {
		HotelsCheckoutScreen.clickSelectPaymentButton();
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.creditCardNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.expirationDateButton().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.postalCodeEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		Log.v(TAG, "CC, name on card, expiration date, email address views all have error icon");

		CardInfoScreen.typeTextCreditCardEditText(mUser.creditCardNumber);
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.expirationDateButton().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.postalCodeEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		Log.v(TAG, "Name on card edit text,expiration date, email address displays error icon when data isn't entered.");

		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		Log.v(TAG, "Clicking on set button");
		CardInfoScreen.clickSetButton();
		Log.v(TAG, "Clicked on set button");
		CardInfoScreen.clickOnDoneButton();
		Log.v(TAG, "Clicked on Done button");
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.expirationDateButton().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.postalCodeEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		Log.v(TAG, "name on card edit text both display error icon when data isn't entered.");

		CardInfoScreen.typeTextNameOnCardEditText(mUser.firstName + mUser.lastName);
		CardInfoScreen.clickOnDoneButton();
		HotelsCheckoutScreen.logInButton().check(matches(isDisplayed()));
		Log.v(TAG, "After all card info was added, the test was able to return to the checkout screen");
	}

	private void verifyLoginButtonNotAppearing() throws Exception {
		HotelsCheckoutScreen.clickLogInButton();
		Common.delay(1);
		LogInScreen.typeTextEmailEditText(mUser.email);
		LogInScreen.typeTextPasswordEditText(mUser.password);
		LogInScreen.clickOnLoginButton();
		Espresso.pressBack();
		HotelsCheckoutScreen.clickCheckoutButton();
		onView(withId(R.id.account_logout_container)).perform(scrollTo());
		EspressoUtils.assertViewWithTextIsDisplayed(mUser.email);
		Log.v(TAG, "Was able to log in, and the email used is now visible from the checkout screen");
		HotelsCheckoutScreen.clickLogOutButton();
		onView(withText(mRes.getString(R.string.sign_out))).perform(click());
		try {
			onView(withText(R.string.ok)).perform(click());
		}
		catch (Exception e) {
			//
		}
		HotelsCheckoutScreen.logInButton().check(matches(isDisplayed()));
		Log.v(TAG, "Log out button was visible and clickable. Email address no longer visible on checkout screen");
		Common.pressBack();
		Common.pressBack();
	}

	private void verifyCreditCardCleared() {
		HotelsRoomsRatesScreen.selectRoomItem(0);
		HotelsCheckoutScreen.clickCheckoutButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "");
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.creditCardNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		Common.pressBack();
		Common.pressBack();
	}

}
