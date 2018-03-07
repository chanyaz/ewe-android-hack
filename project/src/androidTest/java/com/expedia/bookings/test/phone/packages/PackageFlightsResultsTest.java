package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class PackageFlightsResultsTest extends PackageTestCase {

	@Test
	public void testPackageFlightsResultsTest() throws Throwable {
		SearchScreenActions.selectPackageOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreenActions.chooseDatesWithDialog(startDate, endDate);
		SearchScreen.searchButton().perform(click());
		Common.delay(1);


		HotelResultsScreen.selectHotel("Package Happy Path");
		Common.delay(1);

		HotelInfoSiteScreen.bookFirstRoom();
		Common.delay(1);

		assertFlightOutbound(3);
		onView(withId(R.id.all_flights_header)).check(matches(isDisplayed()));
		onView(withText("View your bundle")).perform(click());
		onView(withId(R.id.package_bundle_outbound_flight_widget)).perform(click());
		assertBestFlight();

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());

		assertFlightInbound(3);
		onView(withText("View your bundle")).perform(click());
		onView(withId(R.id.package_bundle_outbound_flight_widget)).perform(click());
		assertOutboundCardExpands();
		onView(withId(R.id.package_bundle_outbound_flight_widget)).perform(click());
		onView(withId(R.id.package_bundle_inbound_flight_widget)).perform(click());

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
	}

	private void assertBestFlight() {
		EspressoUtils.assertViewWithIdIsDisplayedAtPosition(PackageScreen.flightList(), 1, R.id.package_best_flight);
		assertFlightOutbound(1);
	}

	private void assertFlightOutbound(int position) {
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), position, R.id.flight_time_detail_text_view,
			"9:00 am - 11:12 am");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), position, R.id.flight_duration_text_view, "5h 12m (Nonstop)");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), position, R.id.price_text_view, "+$0");
	}

	private void assertFlightInbound(int position) {
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), position, R.id.flight_time_detail_text_view, "1:45 pm - 10:00 pm");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), position, R.id.flight_duration_text_view, "5h 15m (Nonstop)");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), position, R.id.price_text_view, "+$0");
	}

	private void assertOutboundCardExpands() {
		EspressoUtils.assertViewIsDisplayed(R.id.departure_arrival_time);
		EspressoUtils.assertViewIsDisplayed(R.id.departure_arrival_airport);
	}
}
