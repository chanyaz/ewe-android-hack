package com.expedia.bookings.test.happy;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.NewFlightTestCase;
import com.expedia.bookings.test.phone.flights.FlightsScreen;
import com.expedia.bookings.test.phone.packages.PackageScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;

public class NewFlightPhoneHappyPathTest extends NewFlightTestCase {

	public void testNewFlightHappyPath() throws Throwable {
		PackageScreen.selectDepartureAndArrival();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		PackageScreen.selectDates(startDate, endDate);

		PackageScreen.searchButton().perform(click());

		assertFlightOutbound();
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0);
		FlightsScreen.selectOutboundFlight().perform(click());

		assertFlightInbound();
		FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0);
		FlightsScreen.selectInboundFlight().perform(click());

		assertCheckoutOverview();
	}

	private void assertFlightOutbound() {
		FlightsScreen.outboundFlightList().perform(waitForViewToDisplay());
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), 2, R.id.flight_time_detail_text_view,
			"9:00 pm - 8:15 pm");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), 2, R.id.flight_duration_text_view, "2h 0m (Nonstop)");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), 2, R.id.price_text_view, "$696.00");
		EspressoUtils.assertViewWithIdIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), 2, R.id.custom_flight_layover_widget);
	}

	private void assertFlightInbound() {
		FlightsScreen.inboundFlightList().perform(waitForViewToDisplay());
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.inboundFlightList(), 2, R.id.flight_time_detail_text_view, "5:40 pm - 8:15 pm");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.inboundFlightList(), 2, R.id.flight_duration_text_view, "2h 0m (Nonstop)");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.inboundFlightList(), 2, R.id.price_text_view, "$696.00");
		EspressoUtils.assertViewWithIdIsDisplayedAtPosition(FlightsScreen.inboundFlightList(), 2, R.id.custom_flight_layover_widget);
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
