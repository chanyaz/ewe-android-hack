package com.expedia.bookings.test.phone.flights;

import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.assertion.ViewAssertions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.FlightTestCase;
import com.expedia.bookings.test.espresso.HotelsUserData;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsTravelerInfoScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen.clickCheckoutButton;
import static com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen.clickLogOutButton;
import static com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen.logInButton;
import static com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen.logOutButton;
import static org.hamcrest.core.IsNot.not;

public class FlightCheckoutUserInfoTest extends FlightTestCase {

	HotelsUserData user = new HotelsUserData();

	private void goToCheckoutTwoAdults() {
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.enterArrivalAirport("LAS");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickPassengerSelectionButton();
		FlightsSearchScreen.incrementAdultsButton();
		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickListItem(2);
		FlightLegScreen.clickSelectFlightButton();
		clickCheckoutButton();
	}

	/**
	 * See passport_needed_oneway.json (isPassportNeeded: true)
	 */
	public void testPassportNeededFromApiResponse() {
		// Above departure/arrival triggers isPassportNeeded=true search response
		// (see: FlightApiRequestDispatcher)
		String departureAirport = "PEN";
		String arrivalAirport = "KUL";

		navigateToCheckoutScreen(departureAirport, arrivalAirport, 8);
		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(0);
		populatedTravelerDetails();
		verifyPassportRequired();
	}

	public void testInternationalFlightRequiresPassport() {
		navigateToCheckoutScreen("SFO", "LHR", 1);
		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(0);
		populatedTravelerDetails();
		verifyPassportRequired();
	}

	private void navigateToCheckoutScreen(String departureAirport, String arrivalAirport, int listItemIndex) {
		FlightsSearchScreen.enterDepartureAirport(departureAirport);
		FlightsSearchScreen.enterArrivalAirport(arrivalAirport);
		FlightsSearchScreen.clickSelectDepartureButton();
		FlightsSearchScreen.clickDate(LocalDate.now().plusDays(3));
		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickListItem(listItemIndex);
		FlightLegScreen.clickSelectFlightButton();
		clickCheckoutButton();
	}

	private void populatedTravelerDetails() {
		FlightsTravelerInfoScreen.enterFirstName("Mobiata");
		FlightsTravelerInfoScreen.enterLastName("Auto");
		FlightsTravelerInfoScreen.enterPhoneNumber("1112223333");
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.phoneNumberEditText());
		FlightsTravelerInfoScreen.clickBirthDateButton();
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}
		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.selectGender("Male");
		Common.delay(1);
		BillingAddressScreen.clickNextButton();
	}

	private void verifyPassportRequired() {
		ViewInteraction passportCountryListView = onView(withId(R.id.edit_passport_country_listview));
		passportCountryListView.check(ViewAssertions.matches(isDisplayed()));
	}

	public void testVerifyNameMustMatchIdWarning() {
		goToCheckoutTwoAdults();

		// Warning should appear on opening traveler details
		// and close when the user taps the screen.
		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(0);
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		FlightsTravelerInfoScreen.nameMustMatchTextView().perform(click());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(not(isCompletelyDisplayed())));
		Espresso.pressBack();

		// Warning should still be present on subsequent details entry
		// and close when the user starts typing.
		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(0);
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		FlightsTravelerInfoScreen.enterFirstName("foo");
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(not(isCompletelyDisplayed())));
		Espresso.pressBack();
	}

	public void testVerifyNameMustMatchIdWarningSecondTraveler() {
		goToCheckoutTwoAdults();

		// Warning behavior should persist upon entry of additional travelers' info.
		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(2);
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		FlightsTravelerInfoScreen.nameMustMatchTextView().perform(click());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(not(isCompletelyDisplayed())));
		Espresso.pressBack();

		// Warning should disappear when user starts filling out traveler info.
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
			FlightsTravelerInfoScreen.done().perform(waitForViewToDisplay());
			FlightsTravelerInfoScreen.clickDoneString();
		}
		catch (Exception e) {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(not(isCompletelyDisplayed())));
		FlightsTravelerInfoScreen.clickNextButton();
		FlightsTravelerInfoScreen.selectGender("Male");
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

	public void testVerifyRulesAndRestrictionsButton() {
		goToCheckoutTwoAdults();

		CommonCheckoutScreen.flightsLegalTextView().perform(waitFor(isDisplayed(), 2, TimeUnit.SECONDS), click());

		EspressoUtils.assertViewWithTextIsDisplayed("Privacy Policy");
		EspressoUtils.assertViewWithTextIsDisplayed("Terms and Conditions");
		EspressoUtils.assertViewWithTextIsDisplayed("Rules and Restrictions");
		Espresso.pressBack();
	}

	public void testVerifyMissingTravelerInformationAlerts() {
		goToCheckoutTwoAdults();

		// Starting testing of traveler info screen response when fields are left empty
		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(0);
		FlightsTravelerInfoScreen.clickNextButton();

		FlightsTravelerInfoScreen.firstNameEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.lastNameEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.birthDateSpinnerButton().check(
			matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.middleNameEditText().check(
			matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		Common.closeSoftKeyboard(CommonTravelerInformationScreen.firstNameEditText());
		FlightsTravelerInfoScreen.clickBirthDateButton();
		try {
			FlightsTravelerInfoScreen.done().perform(waitForViewToDisplay());
			FlightsTravelerInfoScreen.clickDoneString();
		}
		catch (Exception e) {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		FlightsTravelerInfoScreen.enterFirstName("Expedia");
		FlightsTravelerInfoScreen.clickNextButton();

		// Verifying all field but first and middle name fields show error when 'Done' is pressed.
		FlightsTravelerInfoScreen.firstNameEditText()
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.lastNameEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.middleNameEditText().check(
			matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.enterLastName("Mobile");
		FlightsTravelerInfoScreen.clickNextButton();

		// Verifying all field but first,last and middle name fields show error when 'Done' is pressed.
		FlightsTravelerInfoScreen.firstNameEditText()
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.lastNameEditText()
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.middleNameEditText().check(
			matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		// Verifying that phone number must be at least 4 but max 15 chars long
		FlightsTravelerInfoScreen.enterPhoneNumber("951");
		FlightsTravelerInfoScreen.clickNextButton();
		FlightsTravelerInfoScreen.firstNameEditText()
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.lastNameEditText()
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.middleNameEditText().check(
			matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.phoneNumberEditText().perform(clearText());
		FlightsTravelerInfoScreen.enterPhoneNumber("9512");
		FlightsTravelerInfoScreen.firstNameEditText()
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.lastNameEditText()
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.middleNameEditText().check(
			matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().check(
			matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().perform(clearText());
		// Verify that the phoneNumberEditText allows a max of 15 numbers
		FlightsTravelerInfoScreen.enterPhoneNumber("12345678901234567890");
		FlightsTravelerInfoScreen.phoneNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		FlightsTravelerInfoScreen.phoneNumberEditText().perform(clearText());
//		Verify special characters doesn't affect number limit
		FlightsTravelerInfoScreen.enterPhoneNumber("##(123)--(456)--(7890)");
		FlightsTravelerInfoScreen.phoneNumberEditText().check(
			matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		FlightsTravelerInfoScreen.phoneNumberEditText().perform(clearText());
		FlightsTravelerInfoScreen.enterPhoneNumber("9510000000");
		FlightsTravelerInfoScreen.clickNextButton();

		FlightsTravelerInfoScreen.selectGender("Male");
		// Verify that the redress EditText allows a max of 7 chars, numbers only
		FlightsTravelerInfoScreen.typeRedressText("12345678");
		FlightsTravelerInfoScreen.redressEditText().check(matches(withText("1234567")));
		FlightsTravelerInfoScreen.clickDoneButton();
		logInButton().perform(ViewActions.waitForViewToDisplay());



		// TODO - WAS SEPARATE METHOD
		// Warning behavior should persist after traveler info has been entered and saved.
		FlightsTravelerInfoScreen.clickPopulatedTravelerDetails(0);
		FlightsTravelerInfoScreen.clickEditTravelerInfo();
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.firstNameEditText());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(isCompletelyDisplayed()));
		FlightsTravelerInfoScreen.nameMustMatchTextView().perform(click());
		FlightsTravelerInfoScreen.nameMustMatchTextView().check(matches(not(isCompletelyDisplayed())));
		Espresso.pressBack();
		Espresso.pressBack();
	}

	public void testVerifyMissingCardInfoAlerts() {
		goToCheckoutTwoAdults();

		onView(withText("Payment Method")).perform(click());
		CardInfoScreen.clickNextButton();

		int addressSectionParentId = R.id.address_section;
		BillingAddressScreen.addressLineOneEditText(addressSectionParentId).check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		BillingAddressScreen.addressLineTwoEditText(addressSectionParentId)
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.cityEditText(addressSectionParentId).check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		BillingAddressScreen.stateEditText(addressSectionParentId).check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		BillingAddressScreen.postalCodeEditText(addressSectionParentId).check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		BillingAddressScreen.typeTextAddressLineOne(user.address, addressSectionParentId);
		BillingAddressScreen.addressLineOneEditText(addressSectionParentId)
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.addressLineTwoEditText(addressSectionParentId)
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.cityEditText(addressSectionParentId).check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		BillingAddressScreen.stateEditText(addressSectionParentId).check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		BillingAddressScreen.postalCodeEditText(addressSectionParentId).check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		BillingAddressScreen.typeTextState(user.state, addressSectionParentId);
		BillingAddressScreen.addressLineOneEditText(addressSectionParentId)
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.addressLineTwoEditText(addressSectionParentId)
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.cityEditText(addressSectionParentId).check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		BillingAddressScreen.stateEditText(addressSectionParentId).check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.postalCodeEditText(addressSectionParentId).check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		BillingAddressScreen.typeTextCity(user.city, addressSectionParentId);
		BillingAddressScreen.addressLineOneEditText(addressSectionParentId)
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.addressLineTwoEditText(addressSectionParentId)
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.cityEditText(addressSectionParentId).check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.stateEditText(addressSectionParentId).check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.postalCodeEditText(addressSectionParentId).check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));

		BillingAddressScreen.typeTextPostalCode(user.zipcode, addressSectionParentId);
		BillingAddressScreen.addressLineOneEditText(addressSectionParentId)
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.addressLineTwoEditText(addressSectionParentId)
			.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.cityEditText(addressSectionParentId).check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.stateEditText(addressSectionParentId).check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		BillingAddressScreen.postalCodeEditText(addressSectionParentId).check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));

		BillingAddressScreen.clickNextButton();
		CardInfoScreen.clickOnDoneButton();

		CardInfoScreen.creditCardNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.expirationDateButton().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		// CC, name on card, expiration date, email address views all have error icon

		CardInfoScreen.typeTextCreditCardEditText(user.creditCardNumber.substring(0, 12));
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.expirationDateButton().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.creditCardNumberEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		// Successfully asserted that 12 chars is too short for CC edittext
		CardInfoScreen.creditCardNumberEditText().perform(clearText());

		String twentyChars = "12345123451234512345";
		String nineteenChars = twentyChars.substring(0, 19);
		CardInfoScreen.typeTextCreditCardEditText(twentyChars);
		CardInfoScreen.creditCardNumberEditText().check(matches(withText(nineteenChars)));
		// Successfully asserted that the CC edittext has a max capacity of 19 chars
		CardInfoScreen.creditCardNumberEditText().perform(clearText());

		CardInfoScreen.typeTextCreditCardEditText(user.creditCardNumber);
		CardInfoScreen.expirationDateButton().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		// After entering CC number, the CC edit text no longer has error icon

		/* test cc expiration date validation only if the current month is not January
		* there's no way to enter month in the past in the month of January
		 */

		if (LocalDate.now().getMonthOfYear() != 1) {
			CardInfoScreen.clickOnExpirationDateButton();
			CardInfoScreen.clickMonthDownButton();
			CardInfoScreen.clickSetButton();
			CardInfoScreen.clickOnDoneButton();
			CardInfoScreen.expirationDateButton().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
			CardInfoScreen.creditCardNumberEditText()
				.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
			CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
			CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
			// Successfully asserted that the expiration date cannot be in the past!
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
		// After entering expiration date, that field no longer has error icon

		CardInfoScreen.typeTextNameOnCardEditText(user.firstName + " " + user.lastName);
		CardInfoScreen.expirationDateButton().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		// After entering cardholder name, that edit text no longer has error icon

		CardInfoScreen.typeTextEmailEditText("deepanshumadan");
		CardInfoScreen.expirationDateButton().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.clickOnDoneButton();
		// Successfully asserted that an email address with no '@' or TLD is found invalid
		CardInfoScreen.emailEditText().perform(clearText());

		CardInfoScreen.typeTextEmailEditText("deepanshumadan@");
		CardInfoScreen.expirationDateButton().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.clickOnDoneButton();
		// Successfully asserted that an email address with no website or TLD is found invalid
		CardInfoScreen.emailEditText().perform(clearText());

		CardInfoScreen.typeTextEmailEditText("deepanshumadan@expedia.");
		CardInfoScreen.expirationDateButton().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.emailEditText().check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
		CardInfoScreen.clickOnDoneButton();
		// Successfully asserted that an email address with no TLD is found invalid
		CardInfoScreen.emailEditText().perform(clearText());

		CardInfoScreen.typeTextEmailEditText(user.email);
		CardInfoScreen.expirationDateButton().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.creditCardNumberEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.nameOnCardEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		CardInfoScreen.emailEditText().check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
		// After entering email address, that edit text no longer has error icon
		CardInfoScreen.clickOnDoneButton();

		logInButton().perform(scrollTo());
		logInButton().check(matches(isDisplayed()));
		// After all card info was added, the test was able to return to the checkout screen
	}

	public void testVerifyLoginButtonNotAppearing() throws Exception {
		goToCheckoutTwoAdults();

		Common.pressBack();
		clickCheckoutButton();
		logInButton().perform(scrollTo(), click());

		LogInScreen.emailAddressEditText().perform(waitFor(isDisplayed(), 2, TimeUnit.SECONDS));

		LogInScreen.typeTextEmailEditText(user.email);
		LogInScreen.typeTextPasswordEditText(user.password);
		LogInScreen.clickOnLoginButton();
		Espresso.pressBack();
		clickCheckoutButton();

		logOutButton().perform(waitFor(isDisplayed(), 2, TimeUnit.SECONDS));
		logOutButton().perform(scrollTo());

		EspressoUtils.assertViewWithTextIsDisplayed(user.email);
		clickLogOutButton();
		onView(withText(mRes.getString(R.string.sign_out))).perform(click());
		logInButton().check(matches(isDisplayed()));
	}
}
