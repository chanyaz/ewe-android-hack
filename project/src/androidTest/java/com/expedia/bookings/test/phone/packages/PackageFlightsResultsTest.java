package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class PackageFlightsResultsTest extends PackageTestCase {

	public void testPackageFlightsResultsTest() throws Throwable {
		PackageScreen.selectOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		PackageScreen.selectDates(startDate, endDate);
		PackageScreen.searchButton().perform(click());
		Common.delay(1);

		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(1);

		HotelScreen.selectRoom();
		Common.delay(1);

		PackageScreen.outboundFlight().perform(click());
		Common.delay(1);

		assertFlightOutbound(3);
		onView(withId(R.id.all_flights_header)).check(matches(isDisplayed()));
		assertBestFlight();
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());

		PackageScreen.inboundFLight().perform(click());

		assertFlightInbound(3);
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
		EspressoUtils.assertViewWithIdIsDisplayedAtPosition(PackageScreen.flightList(), position, R.id.custom_flight_layover_widget);
	}

	private void assertFlightInbound(int position) {
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), position, R.id.flight_time_detail_text_view, "1:45 pm - 10:00 pm");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), position, R.id.flight_duration_text_view, "5h 15m (Nonstop)");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), position, R.id.price_text_view, "+$0");
		EspressoUtils.assertViewWithIdIsDisplayedAtPosition(PackageScreen.flightList(), position, R.id.custom_flight_layover_widget);
	}
}
