package com.expedia.bookings.test.phone.flights;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.FlightTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.phone.pagemodels.common.ConfirmationScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsTravelerPicker;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.contrib.PickerActions.setDate;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;

public class FlightTravelerErrorHandlingTest extends FlightTestCase {

	/*
 	* #512 eb_tp test plan
 	*/

	public void testAdultTravelerAge() throws Throwable {
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
		closeDialog();
		BillingAddressScreen.clickNextButton();

		screenshot("Adult_Traveler_Age_Error");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_birthdate_message));
		Common.clickOkString();

		//enter the correct date of birth and complete the checkout
		FlightsTravelerInfoScreen.clickBirthDateButton();
		onView(withParent(withId(android.R.id.custom))).perform(setDate(1990, 10, 11));
		closeDialog();

		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.selectGender("Male");
		FlightsTravelerInfoScreen.clickDoneButton();

		/*
		 Test case 2: Enter incorrect DOB for child traveler
		 and verify the error dialog
		 */

		CommonCheckoutScreen.clickTravelerDetails();
		FlightsTravelerInfoScreen.enterFirstName("Child");
		FlightsTravelerInfoScreen.enterLastName("Auto");
		Common.closeSoftKeyboard(CommonTravelerInformationScreen.lastNameEditText());
		FlightsTravelerInfoScreen.clickBirthDateButton();

		//incorrect dob
		onView(withParent(withId(android.R.id.custom))).perform(setDate(1990, 10, 11));
		closeDialog();

		BillingAddressScreen.clickNextButton();

		screenshot("Child_Traveler_Age_Error");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_birthdate_message));
		Common.clickOkString();

		//enter the correct date of birth and complete the checkout
		FlightsTravelerInfoScreen.clickBirthDateButton();
		onView(withParent(withId(android.R.id.custom))).perform(setDate(2010, 10, 11));
		closeDialog();

		completeCheckout();
	}

	public void testInfantTravelerAge() throws Throwable {
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
		closeDialog();
		BillingAddressScreen.clickNextButton();

		screenshot("Adult_Traveler_Age_Error");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_birthdate_message));
		Common.clickOkString();

		//enter the correct date of birth and complete the checkout
		FlightsTravelerInfoScreen.clickBirthDateButton();
		onView(withParent(withId(android.R.id.custom))).perform(setDate(1990, 10, 11));
		closeDialog();

		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.selectGender("Male");
		FlightsTravelerInfoScreen.clickDoneButton();

		//Common.pressBack();
		//CommonCheckoutScreen.clickCheckoutButton();

		/*
		 Test case 2: Enter incorrect DOB for infant traveler
		 and verify the error dialog
		 */

		CommonCheckoutScreen.clickTravelerDetails();
		FlightsTravelerInfoScreen.enterFirstName("Infant");
		FlightsTravelerInfoScreen.enterLastName("Auto");
		Common.closeSoftKeyboard(CommonTravelerInformationScreen.lastNameEditText());
		FlightsTravelerInfoScreen.clickBirthDateButton();

		//incorrect dob - enter adult age for an infant
		onView(withParent(withId(android.R.id.custom))).perform(setDate(1990, 10, 11));
		closeDialog();

		BillingAddressScreen.clickNextButton();

		screenshot("Infant_Traveler_Age_Error");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_birthdate_message));
		Common.clickOkString();

		//incorrect dob - enter child age for an infant
		FlightsTravelerInfoScreen.clickBirthDateButton();
		onView(withParent(withId(android.R.id.custom))).perform(setDate(2010, 10, 11));
		closeDialog();
		BillingAddressScreen.clickNextButton();

		screenshot("Infant1_Traveler_Age_Error");
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_birthdate_message));
		Common.clickOkString();

		//enter the correct date of birth and complete the checkout
		FlightsTravelerInfoScreen.clickBirthDateButton();
		onView(withParent(withId(android.R.id.custom))).perform(setDate(2015, 06, 11));
		closeDialog();

		completeCheckout();
	}

	private void completeCheckout() {
		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.selectGender("Male");
		FlightsTravelerInfoScreen.clickDoneButton();

		CommonCheckoutScreen.clickSelectPaymentButton();
		BillingAddressScreen.typeTextAddressLineOne("123 California Street");
		BillingAddressScreen.typeTextCity("San Francisco");
		BillingAddressScreen.typeTextState("CA");
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
		CVVEntryScreen.enterCVV("111");
		CVVEntryScreen.clickBookButton();
		ConfirmationScreen.clickDoneButton();
	}

	private void closeDialog() {
		Common.delay(4);
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			try {
				CommonTravelerInformationScreen.clickDoneString();
			}
			catch (Exception ex) {
				Common.pressBack();
			}
		}
	}

}
