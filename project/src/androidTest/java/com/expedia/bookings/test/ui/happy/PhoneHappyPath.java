package com.expedia.bookings.test.ui.happy;

import android.support.test.espresso.DataInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
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

import org.joda.time.LocalDate;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.ui.utils.EspressoUtils.assertViewWithTextIsDisplayed;
import static com.expedia.bookings.test.ui.utils.EspressoUtils.getListItemValues;
import static org.hamcrest.Matchers.containsString;

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

	public void testViewItineraries() throws Throwable {
		screenshot("Launch");
		LaunchScreen.tripsButton().perform(click());
		screenshot("Itins");
		TripsScreen.clickOnLogInButton();
		screenshot("Login");
		LogInScreen.typeTextEmailEditText("user");
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();
		screenshot("Trips");

		// Hotel assertions
		DataInteraction hotelRow = TripsScreen.tripsListItem().atPosition(0);
		String hotelTitle = getListItemValues(hotelRow, R.id.header_text_view);
		final String expectedHotelTitle = "Orchard Hotel";
		assertEquals(expectedHotelTitle, hotelTitle);
		hotelRow.onChildView(withText(containsString("Check in"))).atPosition(0).perform(click());
		onView(withId(R.id.bed_type_text_view)).perform(scrollTo());
		assertViewWithTextIsDisplayed(R.id.local_phone_number_header_text_view, "Local Phone");
		assertViewWithTextIsDisplayed(R.id.local_phone_number_text_view, "1-415-362-8878");
		assertViewWithTextIsDisplayed(R.id.room_type_header_text_view, "Room Type");
		assertViewWithTextIsDisplayed(R.id.room_type_text_view, "Deluxe Room, 1 King Bed");
		assertViewWithTextIsDisplayed(R.id.bed_type_header_text_view, "Bed Type");
		assertViewWithTextIsDisplayed(R.id.bed_type_text_view, "1 king bed");
		hotelRow.onChildView(withText(containsString("Check in"))).perform(scrollTo(), click());

		// Flight assertions
		DataInteraction outboundFlightRow = TripsScreen.tripsListItem().atPosition(1);
		String outboundFlightAirportTimeStr = getListItemValues(outboundFlightRow, R.id.flight_status_bottom_line);
		assertEquals("From SFO at 11:32 AM", outboundFlightAirportTimeStr);
		outboundFlightRow.onChildView(withId(R.id.header_text_date_view)).perform(click());
		assertViewWithTextIsDisplayed(R.id.departure_time, "11:32 AM");
		assertViewWithTextIsDisplayed(R.id.departure_time_tz, "Depart (PDT)");
		assertViewWithTextIsDisplayed(R.id.arrival_time, "9:04 PM");
		assertViewWithTextIsDisplayed(R.id.arrival_time_tz, "Arrive (EDT)");
		onView(withText("Booking Info")).perform(scrollTo());
		assertViewWithTextIsDisplayed("San Francisco Int'l Airport");
		assertViewWithTextIsDisplayed(R.id.airline_text_view, "Delta Air Lines 745");
		assertViewWithTextIsDisplayed(R.id.departure_time_text_view, "11:32 AM");
		assertViewWithTextIsDisplayed(R.id.arrival_time_text_view, "9:04 PM");
		assertViewWithTextIsDisplayed("Detroit Metropolitan Wayne County Airport");
		assertViewWithTextIsDisplayed(R.id.passengers_label, "Passengers");
		assertViewWithTextIsDisplayed(R.id.passenger_name_list, "Philip J. Fry, Turanga Leela");
		assertViewWithTextIsDisplayed("Airline Confirmation");
		assertViewWithTextIsDisplayed("1102138068718");
		assertViewWithTextIsDisplayed("Directions");
		outboundFlightRow.onChildView(withId(R.id.header_text_date_view)).perform(scrollTo(), click());

		// Air attach assertions
		DataInteraction airAttachRow = TripsScreen.tripsListItem().atPosition(2);
		String airAttachMessage = getListItemValues(airAttachRow, R.id.itin_air_attach_text_view);
		assertEquals("Because you booked a flight", airAttachMessage);
		assertViewWithTextIsDisplayed(R.id.itin_air_attach_expiration_date_text_view, "1 day");

		// Car assertions
		DataInteraction carRow = TripsScreen.tripsListItem().atPosition(3);
		String carTitle = getListItemValues(carRow, R.id.header_text_view);
		assertEquals("Budget", carTitle);

		// Lx assertions
		DataInteraction lxRow = TripsScreen.tripsListItem().atPosition(5);
		String lxTitle = getListItemValues(lxRow, R.id.header_text_view);
		final String expectedLxTitle = "Explorer Pass: Choose 4 Museums, Attractions, & Tours: Explorer Pass - Chose 4 Attractions & Tours";
		assertEquals(expectedLxTitle, lxTitle);

		// TODO more assertions for flight, air attach car (e.g. details?)
		// TODO more LOB
	}

}
