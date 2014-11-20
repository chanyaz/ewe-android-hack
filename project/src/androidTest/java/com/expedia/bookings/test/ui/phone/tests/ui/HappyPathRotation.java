package com.expedia.bookings.test.ui.phone.tests.ui;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.ui.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsConfirmationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

/**
 * Created by dmadan on 9/22/14.
 */
public class HappyPathRotation extends PhoneTestCase {

	public void testBookFlight() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		rotateScreenTwice();
		screenshot("Airport_Entered");
		FlightsSearchScreen.clickSelectDepartureButton();
		rotateScreenTwice();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		screenshot("Search");
		rotateScreenTwice();
		FlightsSearchScreen.clickSearchButton();
		screenshot("Search_Results");
		rotateScreenTwice();
		FlightsSearchResultsScreen.clickListItem(1);
		screenshot("Details1");
		rotateScreenTwice();
		FlightLegScreen.clickSelectFlightButton();
		screenshot("Details2");
		rotateScreenTwice();
		FlightsSearchResultsScreen.clickListItem(1);
		rotateScreenTwice();
		FlightLegScreen.clickSelectFlightButton();
		screenshot("Checkout_Overview");
		rotateScreenTwice();
		FlightsCheckoutScreen.clickCheckoutButton();
		screenshot("Checkout_Details");
		rotateScreenTwice();

		FlightsCheckoutScreen.clickTravelerDetails();
		screenshot("Checkout_Traveler");
		FlightsTravelerInfoScreen.enterFirstName("Mobiata");
		FlightsTravelerInfoScreen.enterLastName("Auto");
		FlightsTravelerInfoScreen.enterPhoneNumber("1112223333");
		FlightsTravelerInfoScreen.clickBirthDateButton();
		rotateScreenTwice();
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}
		screenshot("Checkout_Traveler_Entered");
		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.clickDoneButton();
		rotateScreenTwice();
		Common.pressBack();
		FlightsCheckoutScreen.clickCheckoutButton();
		FlightsCheckoutScreen.clickSelectPaymentButton();
		screenshot("Checkout_Payment_Address");
		BillingAddressScreen.typeTextAddressLineOne("123 California Street");
		BillingAddressScreen.typeTextCity("San Francisco");
		BillingAddressScreen.typeTextPostalCode("94105");
		screenshot("Checkout_Address_Entered");
		BillingAddressScreen.clickNextButton();
		rotateScreenTwice();

		screenshot("Checkout_Payment_Card");
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		rotateScreenTwice();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.typeTextEmailEditText("mobiataauto@gmail.com");
		screenshot("Checkout_Payment_Card_Entered");
		rotateScreenTwice();
		CardInfoScreen.clickOnDoneButton();

		screenshot("Slide_To_Purchase");
		rotateScreenTwice();
		FlightsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		screenshot("CVV");
		rotateScreenTwice();
		CVVEntryScreen.clickBookButton();
		screenshot("Confirmation");
		rotateScreenTwice();
		FlightsConfirmationScreen.clickDoneButton();
	}

	public void testBookHotel() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		screenshot("Search_City_Entered");
		rotateScreenTwice();
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		rotateScreenTwice();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(70);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		rotateScreenTwice();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		//rotate screen on Search error:hotel stay longer than 28 days
		rotateScreenTwice();
		SettingsScreen.clickOKString();
		endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		screenshot("Search");
		HotelsSearchScreen.clickOnGuestsButton();
		rotateScreenTwice();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		screenshot("Search_Results");
		HotelsSearchScreen.clickHotelWithName("happy_path");
		screenshot("Details");
		rotateScreenTwice();
		HotelsDetailsScreen.clickSelectButton();
		screenshot("RoomsAndRates");
		rotateScreenTwice();
		HotelsRoomsRatesScreen.selectRoomItem(0);
		rotateScreenTwice();
		HotelsCheckoutScreen.clickCheckoutButton();
		rotateScreenTwice();

		HotelsCheckoutScreen.clickOnLegalInfoButton();
		rotateScreenTwice();
		Common.pressBack();
		HotelsCheckoutScreen.clickGuestDetails();
		screenshot("Checkout_Traveler");
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");
		screenshot("Checkout_Traveler_Entered");
		CommonTravelerInformationScreen.clickDoneButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		screenshot("Checkout_Payment");
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextPostalCode("94015");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		screenshot("Checkout_Payment_Entered");
		rotateScreenTwice();
		CardInfoScreen.clickOnDoneButton();

		screenshot("Slide_To_Purchase");
		rotateScreenTwice();
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		screenshot("CVV");
		rotateScreenTwice();
		CVVEntryScreen.clickBookButton();
		screenshot("Confirmation");
		rotateScreenTwice();
		HotelsConfirmationScreen.clickDoneButton();
	}
}
