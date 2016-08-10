package com.expedia.bookings.test.happy;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.phone.pagemodels.common.ConfirmationScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsTravelerInfoScreen;

public class FlightPhoneHappyPathTest extends PhoneTestCase {

	public void testBookFlight() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		screenshot("Airport_Entered");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		screenshot("Search");
		FlightsSearchScreen.clickSearchButton();
		screenshot("Search_Results");
		FlightsSearchResultsScreen.clickListItem(1);
		screenshot("Details1");
		FlightLegScreen.clickSelectFlightButton();
		screenshot("Details2");
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		screenshot("Checkout_Overview");
		CommonCheckoutScreen.clickCheckoutButton();
		screenshot("Checkout_Details");

		CommonCheckoutScreen.clickTravelerDetails();
		screenshot("Checkout_Traveler");
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
		screenshot("Checkout_Traveler_Entered");
		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.selectGender("Male");
		FlightsTravelerInfoScreen.clickDoneButton();
		Common.pressBack();
		CommonCheckoutScreen.clickCheckoutButton();
		CommonCheckoutScreen.clickSelectPaymentButton();
		screenshot("Checkout_Payment_Address");
		BillingAddressScreen.typeTextAddressLineOne("123 California Street");
		BillingAddressScreen.typeTextCity("San Francisco");
		BillingAddressScreen.typeTextState("CA");
		BillingAddressScreen.typeTextPostalCode("94105");
		screenshot("Checkout_Address_Entered");
		BillingAddressScreen.clickNextButton();

		screenshot("Checkout_Payment_Card");
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.typeTextEmailEditText("mobiataauto@gmail.com");
		screenshot("Checkout_Payment_Card_Entered");
		CardInfoScreen.clickOnDoneButton();

		screenshot("Slide_To_Purchase");
		CommonCheckoutScreen.slideToCheckout();
		CVVEntryScreen.enterCVV("111");
		screenshot("CVV");
		CVVEntryScreen.clickBookButton();
		screenshot("Confirmation");
		ConfirmationScreen.clickDoneButton();
	}
}
