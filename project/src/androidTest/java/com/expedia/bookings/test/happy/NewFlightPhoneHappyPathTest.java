package com.expedia.bookings.test.happy;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.NewFlightTestCase;
import com.expedia.bookings.test.phone.newflights.FlightsScreen;
import com.expedia.bookings.test.phone.newflights.FlightTestHelpers;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class NewFlightPhoneHappyPathTest extends NewFlightTestCase {

	public void testNewFlightHappyPath() throws Throwable {
		SearchScreen.destination().perform(click());
		SearchScreen.selectOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);

		SearchScreen.searchButton().perform(click());

		FlightTestHelpers.assertFlightOutbound();
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0);
		FlightsScreen.selectOutboundFlight().perform(click());

		FlightTestHelpers.assertFlightInbound();
		FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0);
		FlightsScreen.selectInboundFlight().perform(click());

		assertCheckoutOverview();
	}

	private void assertCheckoutOverview() {
		onView(allOf(withId(R.id.destination), withParent(withId(R.id.checkout_overview_floating_toolbar)),
			withText("San Francisco, CA (SFO-San Francisco Intl.)"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.travelers), withParent(withId(R.id.checkout_overview_floating_toolbar)),
			withText("1 Traveler"))).check(matches(isDisplayed()));

		onView(allOf(withId(R.id.flight_card_view_text),
			isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)))).check(
			matches(withText("Flight to (SFO) San Francisco")));

		onView(allOf(withId(R.id.flight_card_view_text),
			isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)))).check(
			matches(withText("Flight to (DTW) Detroit")));
	}
}
