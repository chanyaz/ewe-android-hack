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
import com.expedia.bookings.test.stepdefs.phone.HomeScreenSteps;
import com.expedia.bookings.test.pagemodels.trips.TripsScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.data.abacus.AbacusUtils.EBAndroidAppLXNavigateToSRP;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithContentDescription;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;
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
		onView(allOf(withId(R.id.itin_overflow_image_button), isDisplayed()))
			.check(matches(withContentDescription("Press to view itinerary sharing options")));
		assertViewWithTextIsDisplayed(R.id.departure_time, "11:32 AM");
		assertViewWithTextIsDisplayed(R.id.departure_time_tz, isOutboundFlightDepartureAtStandardOffset ? "Depart (PST)" : "Depart (PDT)");
		assertViewWithTextIsDisplayed(R.id.arrival_time, "6:04 PM");
		assertViewWithTextIsDisplayed(R.id.arrival_time_tz, isOutboundFlightArrivalAtStandardOffset ? "Arrive (EST)" : "Arrive (EDT)");
		onView(withText("San Francisco Int'l Airport")).perform(scrollTo());
		assertViewWithTextIsDisplayed("San Francisco Int'l Airport");
		onView(withText("1102138068718")).perform(scrollTo());
		// TODO - investigate why flight name differs locally to buildbot #4657
		//assertViewWithTextIsDisplayed(R.id.airline_text_view, "Delta Air Lines 745");
		assertViewWithTextIsDisplayed(R.id.departure_time_text_view, "11:32 AM");
		assertViewWithTextIsDisplayed(R.id.arrival_time_text_view, "6:04 PM");
		assertViewWithTextIsDisplayed("Detroit Metropolitan Wayne County Airport");
		onView(allOf(withId(R.id.terminal_map_or_directions_btn), hasSibling(withChild(withText("San Francisco Int'l Airport")))))
			.check(matches(withContentDescription("Terminal Maps and Directions")));
		assertViewWithTextIsDisplayed(R.id.passengers_label, "Passengers");
		assertViewWithTextIsDisplayed(R.id.passenger_name_list, "Philip J. Fry, Turanga Leela");
		assertViewWithTextIsDisplayed("Airline Confirmation");
		assertViewWithTextIsDisplayed("1102138068718");
		assertViewWithTextIsDisplayed("Directions");
		onView(withText("Reload Flight Info")).perform(scrollTo());
		onView(withId(R.id.booking_info)).perform(click());
		onView(allOf(withText("Additional Information"),isDescendantOfA(withId(R.id.toolbar)))).check(matches(isDisplayed()));
		Common.pressBack();

		assertViewWithContentDescription(onView(withId(R.id.summary_left_button)), "Directions Button");
		assertViewWithContentDescription(onView(withId(R.id.summary_right_button)), "GWF4NY Button");
		onView(withId(R.id.close_image_button)).perform(click());
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

		onView(withText(returnFlightAirportTimeStr)).perform(click());

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

		assertViewWithTextIsDisplayed(R.id.departure_time, "4:00 AM");
		assertViewWithTextIsDisplayed(R.id.departure_time_tz, isPackageOutboundFlightDepartureAtStandardOffset ? "Depart (PST)" : "Depart (PDT)");
		assertViewWithTextIsDisplayed(R.id.arrival_time, "6:04 AM");
		assertViewWithTextIsDisplayed(R.id.arrival_time_tz, isPackageOutboundFlightArrivalAtStandardOffset ? "Arrive (PST)" : "Arrive (PDT)");
		onView(withText("San Francisco Int'l Airport")).perform(scrollTo());
		assertViewWithTextIsDisplayed("San Francisco Int'l Airport");
		onView(withText("11590764196")).perform(scrollTo());
		assertViewWithTextIsDisplayed(R.id.departure_time_text_view, "4:00 AM");
		assertViewWithTextIsDisplayed("McCarran Int'l Airport");
		assertViewWithTextIsDisplayed(R.id.passengers_label, "Passengers");
		assertViewWithTextIsDisplayed(R.id.passenger_name_list, "android qa");
		assertViewWithTextIsDisplayed("Airline Confirmation");
		assertViewWithTextIsDisplayed("11590764196");
		assertViewWithTextIsDisplayed("Directions");
		onView(withText("Reload Flight Info")).perform(scrollTo());
		onView(withId(R.id.booking_info)).perform(click());
		onView(allOf(withText("Additional Information"),isDescendantOfA(withId(R.id.toolbar)))).check(matches(isDisplayed()));
		Common.pressBack();
		onView(withId(R.id.close_image_button)).perform(click());

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

		onView(withText(pckgReturnFlightAirportTimeStr)).perform(click());

		// Rails
		DataInteraction railsRow = TripsScreen.tripsListItem().atPosition(11);
		String railsTitle = getListItemValues(railsRow, R.id.header_text_view);

		final String expectedRailsTitle = "Reading-Manchester Piccadilly";
		assertEquals(expectedRailsTitle, railsTitle);

		// Cruise
		DataInteraction cruiseRow = TripsScreen.tripsListItem().atPosition(14);
		String cruiseTitle = getListItemValues(cruiseRow, R.id.header_text_view);

		final String expectedCruiseTitle = "Cruise Card";
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
