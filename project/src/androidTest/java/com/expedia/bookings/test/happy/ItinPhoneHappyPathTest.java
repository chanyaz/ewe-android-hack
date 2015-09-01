package com.expedia.bookings.test.happy;

import android.support.test.espresso.DataInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.TripsScreen;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.getListItemValues;
import static org.hamcrest.Matchers.containsString;

public class ItinPhoneHappyPathTest extends PhoneTestCase {

	public void testViewItineraries() throws Throwable {
		screenshot("Launch");
		LaunchScreen.tripsButton().perform(click());
		screenshot("Itins");
		TripsScreen.clickOnLogInButton();
		screenshot("Login");
		LogInScreen.typeTextEmailEditText("user@gmail.com");
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();
		screenshot("Trips");

		// Hotel assertions
		DataInteraction hotelRow = TripsScreen.tripsListItem().atPosition(0);
		String hotelTitle = getListItemValues(hotelRow, R.id.header_text_view);
		final String expectedHotelTitle = "Orchard Hotel";
		assertEquals(expectedHotelTitle, hotelTitle);
		hotelRow.onChildView(withText(containsString("Check in"))).perform(click());
		screenshot("Hotel Itin");
		onView(withId(R.id.bed_type_text_view)).perform(scrollTo());
		assertViewWithTextIsDisplayed(R.id.local_phone_number_header_text_view, "Local Phone");
		assertViewWithTextIsDisplayed(R.id.local_phone_number_text_view, "1-415-362-8878");
		assertViewWithTextIsDisplayed(R.id.room_type_header_text_view, "Room Type");
		assertViewWithTextIsDisplayed(R.id.room_type_text_view, "Deluxe Room, 1 King Bed");
		assertViewWithTextIsDisplayed(R.id.bed_type_header_text_view, "Bed Type");
		assertViewWithTextIsDisplayed(R.id.bed_type_text_view, "1 king bed");
		hotelRow.onChildView(withText(containsString("Check in"))).perform(scrollTo(), click());

		// Outbound flight assertions
		DataInteraction outboundFlightRow = TripsScreen.tripsListItem().atPosition(1);
		outboundFlightRow.onChildView(withId(R.id.flight_status_bottom_line)).check(matches(withText("From SFO at 11:32 AM")));
		outboundFlightRow.onChildView(withId(R.id.header_text_date_view)).perform(click());
		screenshot("Outbound Flight Itin");
		assertViewWithTextIsDisplayed(R.id.departure_time, "11:32 AM");
		assertViewWithTextIsDisplayed(R.id.departure_time_tz, "Depart (PDT)");
		assertViewWithTextIsDisplayed(R.id.arrival_time, "9:04 PM");
		assertViewWithTextIsDisplayed(R.id.arrival_time_tz, "Arrive (EDT)");
		onView(withText("1102138068718")).perform(scrollTo());
		assertViewWithTextIsDisplayed("San Francisco Int'l Airport");
		// TODO - investigate why flight name differs locally to buildbot #4657
		//assertViewWithTextIsDisplayed(R.id.airline_text_view, "Delta Air Lines 745");
		assertViewWithTextIsDisplayed(R.id.departure_time_text_view, "11:32 AM");
		assertViewWithTextIsDisplayed(R.id.arrival_time_text_view, "9:04 PM");
		assertViewWithTextIsDisplayed("Detroit Metropolitan Wayne County Airport");
		assertViewWithTextIsDisplayed(R.id.passengers_label, "Passengers");
		assertViewWithTextIsDisplayed(R.id.passenger_name_list, "Philip J. Fry, Turanga Leela");
		assertViewWithTextIsDisplayed("Airline Confirmation");
		assertViewWithTextIsDisplayed("1102138068718");
		assertViewWithTextIsDisplayed("Directions");
		outboundFlightRow.onChildView(withId(R.id.flight_status_bottom_line)).perform(scrollTo(), click());

		// Air attach assertions
		DataInteraction airAttachRow = TripsScreen.tripsListItem().atPosition(2);
		String airAttachMessage = getListItemValues(airAttachRow, R.id.itin_air_attach_text_view);
		screenshot("Air Attach");
		assertEquals("Because you booked a flight", airAttachMessage);
		assertViewWithTextIsDisplayed(R.id.itin_air_attach_expiration_date_text_view, "1 day");

		// Car assertions
		DataInteraction carRow = TripsScreen.tripsListItem().atPosition(3);
		String carTitle = getListItemValues(carRow, R.id.header_text_view);
		carRow.onChildView(withId(R.id.header_text_date_view)).perform(click());
		screenshot("Car Itin");
		assertEquals("Budget", carTitle);
		carRow.onChildView(withText(containsString("Pick up"))).perform(scrollTo(), click());

		// Return flight assertions
		DataInteraction returnFlightRow = TripsScreen.tripsListItem().atPosition(4);
		String returnFlightAirportTimeStr = getListItemValues(returnFlightRow, R.id.flight_status_bottom_line);
		assertEquals("From DTW at 9:59 PM", returnFlightAirportTimeStr);
		returnFlightRow.onChildView(withId(R.id.header_text_date_view)).perform(click());
		screenshot("Return Flight Itin");
		returnFlightRow.onChildView(withText(returnFlightAirportTimeStr)).perform(scrollTo(), click());


		// Lx assertions
		DataInteraction lxRow = TripsScreen.tripsListItem().atPosition(5);
		String lxTitle = getListItemValues(lxRow, R.id.header_text_view);
		screenshot("LX Itin");
		final String expectedLxTitle = "Explorer Pass: Choose 4 Museums, Attractions, & Tours: Explorer Pass - Chose 4 Attractions & Tours";
		assertEquals(expectedLxTitle, lxTitle);

		// Pacakage outbound flight assertions
		DataInteraction pckgOutboundFlightRow = TripsScreen.tripsListItem().atPosition(6);
		String pckgOutboundFlightAirportTimeStr = getListItemValues(pckgOutboundFlightRow,
			R.id.flight_status_bottom_line);
		assertEquals("From SFO at 4:00 AM", pckgOutboundFlightAirportTimeStr);
		pckgOutboundFlightRow.onChildView(withId(R.id.header_text_date_view)).perform(click());
		screenshot("Package Outbound Flight Itin");
		assertViewWithTextIsDisplayed(R.id.departure_time, "4:00 AM");
		assertViewWithTextIsDisplayed(R.id.departure_time_tz, "Depart (PDT)");
		assertViewWithTextIsDisplayed(R.id.arrival_time, "6:04 AM");
		assertViewWithTextIsDisplayed(R.id.arrival_time_tz, "Arrive (PDT)");
		onView(withText("11590764196")).perform(scrollTo());
		assertViewWithTextIsDisplayed("San Francisco Int'l Airport");
		assertViewWithTextIsDisplayed(R.id.departure_time_text_view, "4:00 AM");
		assertViewWithTextIsDisplayed("McCarran Int'l Airport");
		assertViewWithTextIsDisplayed(R.id.passengers_label, "Passengers");
		assertViewWithTextIsDisplayed(R.id.passenger_name_list, "android qa");
		assertViewWithTextIsDisplayed("Airline Confirmation");
		assertViewWithTextIsDisplayed("11590764196");
		assertViewWithTextIsDisplayed("Directions");
		pckgOutboundFlightRow.onChildView(withText(pckgOutboundFlightAirportTimeStr)).perform(scrollTo(), click());

		// Package hotel assertions
		DataInteraction pckgHotelRow = TripsScreen.tripsListItem().atPosition(7);
		String pckgHotelTitle = getListItemValues(pckgHotelRow, R.id.header_text_view);
		final String expectedPckgHotelTitle = "Caesars Palace";
		assertEquals(expectedPckgHotelTitle, pckgHotelTitle);
		pckgHotelRow.onChildView(withId(R.id.header_text_date_view)).perform(scrollTo(), click());
		screenshot("Package Hotel Itin");
		pckgHotelRow.onChildView(withId(R.id.bed_type_text_view)).perform(scrollTo());
		assertViewWithTextIsDisplayed(R.id.local_phone_number_header_text_view, "Local Phone");
		assertViewWithTextIsDisplayed(R.id.local_phone_number_text_view, "1-702-731-7110");
		assertViewWithTextIsDisplayed(R.id.room_type_header_text_view, "Room Type");
		assertViewWithTextIsDisplayed(R.id.room_type_text_view, "Roman Tower");
		assertViewWithTextIsDisplayed(R.id.bed_type_header_text_view, "Bed Type");
		assertViewWithTextIsDisplayed(R.id.bed_type_text_view, "1 king bed");
		pckgHotelRow.onChildView(withText(containsString("Check in"))).perform(scrollTo(), click());

		// Package return flight assertions
		DataInteraction pckgReturnFlightRow = TripsScreen.tripsListItem().atPosition(8);
		String pckgReturnFlightAirportTimeStr = getListItemValues(pckgReturnFlightRow, R.id.flight_status_bottom_line);
		assertEquals("From LAS at 10:00 AM", pckgReturnFlightAirportTimeStr);
		pckgReturnFlightRow.onChildView(withId(R.id.header_text_date_view)).perform(click());
		screenshot("Package Return Flight Itin");
		pckgReturnFlightRow.onChildView(withText(pckgReturnFlightAirportTimeStr)).perform(scrollTo(), click());

		// Cruise
		DataInteraction cruiseRow = TripsScreen.tripsListItem().atPosition(9);
		String cruiseTitle = getListItemValues(cruiseRow, R.id.header_text_view);
		screenshot("Cruise");
		final String expectedCruiseTitle = "Cruise Card";
		assertEquals(expectedCruiseTitle, cruiseTitle);
	}

}
