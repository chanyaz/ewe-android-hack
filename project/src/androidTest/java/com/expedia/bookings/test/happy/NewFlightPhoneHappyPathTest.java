package com.expedia.bookings.test.happy;

import com.expedia.bookings.test.espresso.NewFlightTestCase;

public class NewFlightPhoneHappyPathTest extends NewFlightTestCase {

//	public void testNewFlightHappyPath() throws Throwable {
//		SearchScreen.origin().perform(click());
//		SearchScreen.selectOriginAndDestination();
//		LocalDate startDate = LocalDate.now().plusDays(3);
//		LocalDate endDate = LocalDate.now().plusDays(8);
//		SearchScreen.selectDates(startDate, endDate);
//
//		SearchScreen.searchButton().perform(click());
//
//		FlightTestHelpers.assertFlightOutbound();
//		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0);
//		FlightsScreen.selectOutboundFlight().perform(click());
//
//		FlightTestHelpers.assertFlightInbound();
//		FlightTestHelpers.assertDockedOutboundFlightSelectionWidget();
//		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("Outbound");
//		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("Delta");
//		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("9:00 pm - 8:15 pm (2h 0m)");
//		FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0);
//		FlightsScreen.selectInboundFlight().perform(click());
//
//		Common.pressBack();
//		assertCheckoutOverview();
//	}
//
//	private void assertCheckoutOverview() {
//		onView(allOf(withId(R.id.destination), withParent(withId(R.id.checkout_overview_floating_toolbar)),
//			withText("San Francisco, CA (SFO-San Francisco Intl.)"))).check(matches(isDisplayed()));
//		onView(allOf(withId(R.id.travelers), withParent(withId(R.id.checkout_overview_floating_toolbar)),
//			withText("1 Traveler"))).check(matches(isDisplayed()));
//
//		onView(allOf(withId(R.id.flight_card_view_text),
//			isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)))).check(
//			matches(withText("Flight to (SFO) San Francisco")));
//
//		onView(allOf(withId(R.id.flight_card_view_text),
//			isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)))).check(
//			matches(withText("Flight to (DTW) Detroit")));
//	}
}
