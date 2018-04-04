package com.expedia.bookings.test.happy;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;
import com.expedia.bookings.test.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.pagemodels.trips.TripDetailsScreen;
import com.expedia.bookings.test.stepdefs.phone.HomeScreenSteps;
import com.expedia.bookings.test.pagemodels.trips.TripsScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.data.abacus.AbacusUtils.EBAndroidAppLXNavigateToSRP;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithContentDescription;
import static com.expedia.bookings.test.espresso.EspressoUtils.getListItemValues;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AllOf.allOf;

public class ItinPhoneHappyPathTest extends PhoneTestCase {

	@Test
	public void testViewItineraries() throws Throwable {
		AbacusTestUtils.bucketTests(EBAndroidAppLXNavigateToSRP);

		HomeScreenSteps.switchToTab("Trips");

		TripsScreen.clickOnLogInButton();

		LogInScreen.typeTextEmailEditText("qa-ehcc@mobiata.com");
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();

		// Hotel assertions
		DataInteraction hotelRow = TripsScreen.tripsListItem().atPosition(0);
		String hotelTitle = getListItemValues(hotelRow, R.id.header_text_view);
		final String expectedHotelTitle = "Orchard Hotel";
		assertEquals(expectedHotelTitle, hotelTitle);
		ViewInteraction chevronButton = onView(allOf(withId(R.id.chevron_image_view), hasSibling(hasDescendant(withText(containsString("Check in"))))));
		assertViewWithContentDescription(chevronButton, "Button to expand trip");

		onView(allOf(withId(R.id.summary_layout), hasDescendant(withText(containsString("Check in"))))).perform(click());
		Common.pressBack();


		//TODO - For now, just replicating the "Inject flight DateTimes" from ExpediaDispatcher::dispatchTrip
		//so any change there will need to be reflected here as well. Ideally we should have a common place where this setup is done
		//so the setup values can be read by the tests when required. That would avoid the duplication which is being done below.
		boolean isOutboundFlightDepartureAtStandardOffset = checkOutboundFlightDepartureAtStandardOffset();
		boolean isOutboundFlightArrivalAtStandardOffset = checkOutboundFlightArrivalAtStandardOffset();

		DataInteraction lxAttachRow = TripsScreen.tripsListItem().atPosition(1);
		lxAttachRow.perform(click());
		LXScreen.didOpenResults();
		Common.pressBack();

		// Outbound flight assertions
		DataInteraction outboundFlightRow = TripsScreen.tripsListItem().atPosition(2);
		outboundFlightRow.onChildView(withId(R.id.flight_status_bottom_line)).check(matches(withText("From SFO at 11:32 AM")));
		outboundFlightRow.onChildView(withId(R.id.header_text_date_view)).perform(click());

		TripDetailsScreen.Toolbar.verifyShareButtonDescription("Press to view itinerary sharing options");

		TripDetailsScreen.FlightConfirmation.verifyConfirmationCode("GWF4NY");
		TripDetailsScreen.FlightConfirmation.assertConfirmationCodeIsClickable();

		TripDetailsScreen.FlightSummary.verifyAirlineName("Delta Air Lines 745");
		TripDetailsScreen.FlightSummary.verifyDepartureTime("11:32 am");
		TripDetailsScreen.FlightSummary.verifyDepartureAirport("San Francisco (SFO)");
		TripDetailsScreen.FlightSummary.verifyArrivalTime("6:04 pm");
		TripDetailsScreen.FlightSummary.verifyArrivalAirport("Detroit (DTW)");
		TripDetailsScreen.FlightSummary.verifySeating("No seats selected");
		TripDetailsScreen.FlightSummary.verifyCabinClass(" • Economy / Coach");

		TripDetailsScreen.FlightDuration.verifyFlightDuration("Total duration: 4h 32m");

		//This Scrolls all the way to the bottom
		TripDetailsScreen.FlightBookingInformation.scrollToAdditionalInformation();

		TripDetailsScreen.FlightMap.assertMapIsDisplayed();
		TripDetailsScreen.FlightMap.verifyTerminalMapsButtonLabel("Terminal maps");
		TripDetailsScreen.FlightMap.verifyDirectionsButtonLabel("Directions");

		TripDetailsScreen.FlightBookingInformation.verifyTravelerInformationHeading("Traveler information");
		TripDetailsScreen.FlightBookingInformation.verifyTravelerInformationSubheading("Philip J. Fry, Turanga Leela");
		TripDetailsScreen.FlightBookingInformation.verifyAdditionalInformationIsDisplayed();

		TripDetailsScreen.Toolbar.clickBackButton();

		// Car assertions
		DataInteraction carRow = TripsScreen.tripsListItem().atPosition(4);
		String carTitle = getListItemValues(carRow, R.id.header_text_view);
		carRow.onChildView(withId(R.id.header_text_date_view)).perform(click());
		onView(allOf(
				withText(containsString("Pick up")),
				isDescendantOfA(withId(R.id.summary_section_layout))
		)).perform(click());

		assertEquals("Budget", carTitle);
		// Return flight assertions
		DataInteraction returnFlightRow = TripsScreen.tripsListItem().atPosition(5);
		String returnFlightAirportTimeStr = getListItemValues(returnFlightRow, R.id.flight_status_bottom_line);
		assertEquals("From DTW at 6:59 PM", returnFlightAirportTimeStr);
		returnFlightRow.onChildView(withId(R.id.header_text_date_view)).perform(click());

		TripDetailsScreen.Toolbar.clickBackButton();

		// Lx assertions
		DataInteraction lxRow = TripsScreen.tripsListItem().atPosition(6);
		String lxTitle = getListItemValues(lxRow, R.id.header_text_view);

		final String expectedLxTitle = "Explorer Pass: Choose 4 Museums, Attractions, & Tours: Explorer Pass - Chose 4 Attractions & Tours";
		assertEquals(expectedLxTitle, lxTitle);

		//TODO - For now, just replicating the "Inject package DateTimes" from ExpediaDispatcher::dispatchTrip
		//so any change there will need to be reflected here as well. Ideally we should have a common place where this setup is done
		//so the setup values can be read by the tests when required. That would avoid the duplication which is being done below.
		boolean isPackageOutboundFlightDepartureAtStandardOffset = checkPackageOutboundFlightDepartureAtStandardOffset();
		boolean isPackageOutboundFlightArrivalAtStandardOffset = checkPackageOutboundFlightArrivalAtStandardOffset();

		// Pacakage outbound flight assertions
		DataInteraction pckgOutboundFlightRow = TripsScreen.tripsListItem().atPosition(7);
		String pckgOutboundFlightAirportTimeStr = getListItemValues(pckgOutboundFlightRow,
			R.id.flight_status_bottom_line);
		assertEquals("From SFO at 4:00 AM", pckgOutboundFlightAirportTimeStr);
		pckgOutboundFlightRow.onChildView(withId(R.id.header_text_date_view)).perform(click());

		TripDetailsScreen.Toolbar.verifyShareButtonDescription("Press to view itinerary sharing options");

		TripDetailsScreen.FlightConfirmation.verifyConfirmationCode("PRWVRU");
		TripDetailsScreen.FlightConfirmation.assertConfirmationCodeIsClickable();

		TripDetailsScreen.FlightSummary.verifyAirlineName("JetBlue Airways 2288");
		TripDetailsScreen.FlightSummary.verifyDepartureTime("4:00 am");
		TripDetailsScreen.FlightSummary.verifyDepartureAirport("San Francisco (SFO)");
		TripDetailsScreen.FlightSummary.verifyDepartureTerminalGate("Terminal I");
		TripDetailsScreen.FlightSummary.verifyArrivalTime("6:04 am");
		TripDetailsScreen.FlightSummary.verifyArrivalAirport("Las Vegas (LAS)");
		TripDetailsScreen.FlightSummary.verifyArrivalTerminalGate("Terminal 3");
		TripDetailsScreen.FlightSummary.verifySeating("No seats selected");
		TripDetailsScreen.FlightSummary.verifyCabinClass(" • Economy / Coach");

		TripDetailsScreen.FlightDuration.verifyFlightDuration("Total duration: 1h 36m");

		//This Scrolls all the way to the bottom
		TripDetailsScreen.FlightBookingInformation.scrollToAdditionalInformation();

		TripDetailsScreen.FlightMap.assertMapIsDisplayed();
		TripDetailsScreen.FlightMap.verifyTerminalMapsButtonLabel("Terminal maps");
		TripDetailsScreen.FlightMap.verifyDirectionsButtonLabel("Directions");

		TripDetailsScreen.FlightBookingInformation.verifyTravelerInformationHeading("Traveler information");
		TripDetailsScreen.FlightBookingInformation.verifyTravelerInformationSubheading("android qa");
		TripDetailsScreen.FlightBookingInformation.verifyAdditionalInformationIsDisplayed();

		TripDetailsScreen.Toolbar.clickBackButton();

		// Package hotel assertions
		DataInteraction pckgHotelRow = TripsScreen.tripsListItem().atPosition(8);
		String pckgHotelTitle = getListItemValues(pckgHotelRow, R.id.header_text_view);
		final String expectedPckgHotelTitle = "Caesars Palace";
		assertEquals(expectedPckgHotelTitle, pckgHotelTitle);
		pckgHotelRow.onChildView(withId(R.id.header_text_date_view)).perform(scrollTo(), click());
		Common.pressBack();

		// Package return flight assertions
		DataInteraction pckgReturnFlightRow = TripsScreen.tripsListItem().atPosition(10);
		String pckgReturnFlightAirportTimeStr = getListItemValues(pckgReturnFlightRow, R.id.flight_status_bottom_line);
		assertEquals("From LAS at 10:00 AM", pckgReturnFlightAirportTimeStr);
		pckgReturnFlightRow.onChildView(withId(R.id.header_text_date_view)).perform(click());

		TripDetailsScreen.Toolbar.clickBackButton();

		// Rails
		DataInteraction railsRow = TripsScreen.tripsListItem().atPosition(11);
		String railsTitle = getListItemValues(railsRow, R.id.header_text_view);

		final String expectedRailsTitle = "Reading-Manchester Piccadilly";
		assertEquals(expectedRailsTitle, railsTitle);

		// Cruise
		DataInteraction cruiseRow = TripsScreen.tripsListItem().atPosition(14);
		String cruiseTitle = getListItemValues(cruiseRow, R.id.header_text_view);

		final String expectedCruiseTitle = "External Cruise, Booked";
		assertEquals(expectedCruiseTitle, cruiseTitle);

		assertViewWithContentDescription(TripsScreen.addGuestItinButton(), "Manually add guest booked trip button");
		TripsScreen.addGuestItinButton().perform(click());
		TripsScreen.enterItinToolbarText().perform(waitForViewToDisplay());
	}

	private boolean checkOutboundFlightArrivalAtStandardOffset() {
		DateTime startOfTodayEastern = DateTime.now().withZone(DateTimeZone.forID("America/New_York")).withTimeAtStartOfDay();
		DateTime outboundFlightArrival = startOfTodayEastern.plusDays(14).withHourOfDay(18).withMinuteOfHour(4);
		return DateTimeZone.forID("America/New_York").isStandardOffset(outboundFlightArrival.getMillis());
	}

	private boolean checkOutboundFlightDepartureAtStandardOffset() {
		DateTime startOfTodayPacific = DateTime.now().withZone(DateTimeZone.forID("America/Los_Angeles")).withTimeAtStartOfDay();
		DateTime outboundFlightDeparture = startOfTodayPacific.plusDays(14).withHourOfDay(11).withMinuteOfHour(32);
		return DateTimeZone.forID("America/Los_Angeles").isStandardOffset(outboundFlightDeparture.getMillis());
	}

	private boolean checkPackageOutboundFlightArrivalAtStandardOffset() {
		DateTime startOfTodayPacific = DateTime.now().withZone(DateTimeZone.forID("America/Los_Angeles")).withTimeAtStartOfDay();
		DateTime outboundFlightArrival = startOfTodayPacific.plusDays(35).withHourOfDay(4);
		return DateTimeZone.forID("America/Los_Angeles").isStandardOffset(outboundFlightArrival.getMillis());
	}

	private boolean checkPackageOutboundFlightDepartureAtStandardOffset() {
		DateTime startOfTodayPacific = DateTime.now().withZone(DateTimeZone.forID("America/Los_Angeles")).withTimeAtStartOfDay();
		DateTime outboundFlightArrival = startOfTodayPacific.plusDays(35).withHourOfDay(6).withMinuteOfHour(4);
		return DateTimeZone.forID("America/Los_Angeles").isStandardOffset(outboundFlightArrival.getMillis());
	}
}
