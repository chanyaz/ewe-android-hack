package com.expedia.bookings.test.ui.happy;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.ui.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.TripsScreen;
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

import static android.support.test.espresso.action.ViewActions.click;

public class PhoneHappyPath extends PhoneTestCase {

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
		FlightsCheckoutScreen.clickCheckoutButton();
		screenshot("Checkout_Details");

		FlightsCheckoutScreen.clickTravelerDetails();
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
		FlightsTravelerInfoScreen.clickDoneButton();
		Common.pressBack();
		FlightsCheckoutScreen.clickCheckoutButton();
		FlightsCheckoutScreen.clickSelectPaymentButton();
		screenshot("Checkout_Payment_Address");
		BillingAddressScreen.typeTextAddressLineOne("123 California Street");
		BillingAddressScreen.typeTextCity("San Francisco");
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
		FlightsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		screenshot("CVV");
		CVVEntryScreen.clickBookButton();
		screenshot("Confirmation");
		FlightsConfirmationScreen.clickDoneButton();
	}

	public void testBookHotel() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		screenshot("Search_City_Entered");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		screenshot("Search");
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		screenshot("Search_Results");
		HotelsSearchScreen.clickHotelWithName("happypath");
		screenshot("Details");
		HotelsDetailsScreen.clickSelectButton();
		screenshot("RoomsAndRates");
		HotelsRoomsRatesScreen.selectRoomItem(0);
		HotelsCheckoutScreen.clickCheckoutButton();

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
		CardInfoScreen.clickOnDoneButton();

		screenshot("Slide_To_Purchase");
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		screenshot("CVV");
		CVVEntryScreen.clickBookButton();
		screenshot("Confirmation");
		HotelsConfirmationScreen.clickDoneButton();
	}

	// FIXME: Disabling for now. Hangs after logging in
	public void ignoreViewItineraries() throws Throwable {
		screenshot("Launch");
		LaunchScreen.tripsButton().perform(click());
		screenshot("Itins");
		TripsScreen.clickOnLogInButton();
		screenshot("Login");
		LogInScreen.typeTextEmailEditText("user");
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();
		screenshot("Trips");
		for (int i = 0; i < 3; i++) {
			TripsScreen.clickListItem(i);
			ScreenActions.delay(2);
			screenshot("Trips_item_" + i + "_press");
			Common.pressBack();
			screenshot("Trips");
		}
	}

}
