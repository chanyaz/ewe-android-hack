package com.expedia.bookings.test.ui.phone.tests.flights;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ConfirmationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsTravelerPicker;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.contrib.PickerActions.setDate;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;

public class FlightTravelerErrorHandlingTest extends PhoneTestCase {

	/*
 	* #512 eb_tp test plan
 	*/

	public void testAdultTravelerAge() throws Throwable {
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate, null);
		FlightsSearchScreen.clickPassengerSelectionButton();
		FlightsSearchScreen.incrementChildrenButton();
		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickListItem(3);
		FlightLegScreen.clickSelectFlightButton();
		CommonCheckoutScreen.clickCheckoutButton();

		/*
		 Test case 1: Enter incorrect DOB for adult traveler
		 and verify the error dialog
		 */

		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(0);
		FlightsTravelerInfoScreen.enterFirstName("Mobiata");
		FlightsTravelerInfoScreen.enterLastName("Auto");
		FlightsTravelerInfoScreen.enterPhoneNumber("1112223333");
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.phoneNumberEditText());
		FlightsTravelerInfoScreen.clickBirthDateButton();

		//incorrect dob
		onView(withParent(withId(android.R.id.custom))).perform(setDate(2010, 10, 11));
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}
		BillingAddressScreen.clickNextButton();

		screenshot("Adult_Traveler_Age_Error");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_birthdate_message));
		SettingsScreen.clickOkString();

		//enter the correct date of birth and complete the checkout
		FlightsTravelerInfoScreen.clickBirthDateButton();
		onView(withParent(withId(android.R.id.custom))).perform(setDate(1990, 10, 11));
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}

		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.clickDoneButton();
		Common.pressBack();
		CommonCheckoutScreen.clickCheckoutButton();

		/*
		 Test case 2: Enter incorrect DOB for child traveler
		 and verify the error dialog
		 */

		CommonCheckoutScreen.clickTravelerDetails();
		FlightsTravelerInfoScreen.enterFirstName("Child");
		FlightsTravelerInfoScreen.enterLastName("Auto");
		FlightsTravelerInfoScreen.clickBirthDateButton();

		//incorrect dob
		onView(withParent(withId(android.R.id.custom))).perform(setDate(1990, 10, 11));
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}
		BillingAddressScreen.clickNextButton();

		screenshot("Child_Traveler_Age_Error");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_birthdate_message));
		SettingsScreen.clickOkString();

		//enter the correct date of birth and complete the checkout
		FlightsTravelerInfoScreen.clickBirthDateButton();
		onView(withParent(withId(android.R.id.custom))).perform(setDate(2010, 10, 11));
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}

		completeCheckout();
	}

	public void testInfantTravelerAge() throws Throwable {
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate, null);
		FlightsSearchScreen.clickPassengerSelectionButton();
		FlightsSearchScreen.incrementChildrenButton();
		FlightsTravelerPicker.selectChildAge(getActivity(), 1, 0);

		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickListItem(4);
		FlightLegScreen.clickSelectFlightButton();
		CommonCheckoutScreen.clickCheckoutButton();

		/*
		 Test case 1: Enter incorrect DOB for adult traveler
		 and verify the error dialog
		 */

		FlightsTravelerInfoScreen.clickEmptyTravelerDetails(0);
		FlightsTravelerInfoScreen.enterFirstName("Mobiata");
		FlightsTravelerInfoScreen.enterLastName("Auto");
		FlightsTravelerInfoScreen.enterPhoneNumber("1112223333");
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.phoneNumberEditText());
		FlightsTravelerInfoScreen.clickBirthDateButton();

		//incorrect dob
		onView(withParent(withId(android.R.id.custom))).perform(setDate(2014, 10, 11));
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}
		BillingAddressScreen.clickNextButton();

		screenshot("Adult_Traveler_Age_Error");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_birthdate_message));
		SettingsScreen.clickOkString();

		//enter the correct date of birth and complete the checkout
		FlightsTravelerInfoScreen.clickBirthDateButton();
		onView(withParent(withId(android.R.id.custom))).perform(setDate(1990, 10, 11));
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}

		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.clickDoneButton();
		Common.pressBack();
		CommonCheckoutScreen.clickCheckoutButton();

		/*
		 Test case 2: Enter incorrect DOB for infant traveler
		 and verify the error dialog
		 */

		CommonCheckoutScreen.clickTravelerDetails();
		FlightsTravelerInfoScreen.enterFirstName("Infant");
		FlightsTravelerInfoScreen.enterLastName("Auto");
		FlightsTravelerInfoScreen.clickBirthDateButton();

		//incorrect dob - enter adult age for an infant
		onView(withParent(withId(android.R.id.custom))).perform(setDate(1990, 10, 11));
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}

		BillingAddressScreen.clickNextButton();

		screenshot("Infant_Traveler_Age_Error");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_birthdate_message));
		SettingsScreen.clickOkString();

		//incorrect dob - enter child age for an infant
		FlightsTravelerInfoScreen.clickBirthDateButton();
		onView(withParent(withId(android.R.id.custom))).perform(setDate(2010, 10, 11));
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}
		BillingAddressScreen.clickNextButton();

		screenshot("Infant1_Traveler_Age_Error");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_birthdate_message));
		SettingsScreen.clickOkString();

		//enter the correct date of birth and complete the checkout
		FlightsTravelerInfoScreen.clickBirthDateButton();
		onView(withParent(withId(android.R.id.custom))).perform(setDate(2015, 06, 11));
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}

		completeCheckout();
	}

	private void completeCheckout() {
		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.clickDoneButton();

		Common.pressBack();
		CommonCheckoutScreen.clickCheckoutButton();
		CommonCheckoutScreen.clickSelectPaymentButton();
		BillingAddressScreen.typeTextAddressLineOne("123 California Street");
		BillingAddressScreen.typeTextCity("San Francisco");
		BillingAddressScreen.typeTextPostalCode("94105");
		BillingAddressScreen.clickNextButton();

		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.typeTextEmailEditText("mobiataauto@gmail.com");
		CardInfoScreen.clickOnDoneButton();

		CommonCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();
		ConfirmationScreen.clickDoneButton();
	}

}
