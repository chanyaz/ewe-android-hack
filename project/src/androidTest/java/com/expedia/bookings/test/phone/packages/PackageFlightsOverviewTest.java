package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.not;

public class PackageFlightsOverviewTest extends PackageTestCase {

	public void testPackageFlightsOverview() throws Throwable {
		PackageScreen.selectDepartureAndArrival();
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
		PackageScreen.flightsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(isDisplayed(), withText("Select flight to Detroit, MI")))));
		checkToolBarMenuItemsVisibility(true);

		PackageScreen.selectFlight(1);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(isDisplayed(), withText("Flight to Detroit, MI")))));
		checkToolBarMenuItemsVisibility(false);
		assertSegmentData();
		assertBundlePriceInFlight("$3,864");

		onView(allOf(withId(R.id.select_flight_button), withText("Select this Flight"))).check(matches(isDisplayed()));
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);
	}

	public void checkToolBarMenuItemsVisibility(boolean isVisible) {
		if (isVisible) {
			PackageScreen.flightsToolbarSearchMenu().check(matches(isDisplayed()));
			PackageScreen.flightsToolbarFilterMenu().check(matches(isDisplayed()));
		}
		else {
			PackageScreen.flightsToolbarSearchMenu().check(matches(not(isDisplayed())));
			PackageScreen.flightsToolbarFilterMenu().check(matches(not(isDisplayed())));
		}
	}

	public void assertSegmentData() {
		// Segment #1
		onView(allOf(withId(R.id.departure_arrival_time), withText("9:50AM - 2:27PM"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.airline_airplane_type), withText("Hawaiian Airlines 497 - Boeing 737-900"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.departure_arrival_airport), withText("(SFO) San Francisco - (SAN) San Diego"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_duration), withText("4h 37m"))).check(matches(isDisplayed()));

		// Layover
		onView(allOf(withId(R.id.flight_segment_layover_in), withText("Layover in (SAN) San Diego"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_segment_layover_duration), withText("48m"))).check(matches(isDisplayed()));

		// Segment #2
		onView(allOf(withId(R.id.departure_arrival_time), withText("3:15PM - 4:09PM"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.airline_airplane_type), withText("Hawaiian Airlines 5496 - Boeing 717"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.departure_arrival_airport), withText("(SAN) San Diego - (LAX) Los Angeles"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_duration), withText("54m"))).check(matches(isDisplayed()));

		// Layover
		onView(allOf(withId(R.id.flight_segment_layover_in), withText("Layover in (LAX) Los Angeles"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_segment_layover_duration), withText("1h 1m"))).check(matches(isDisplayed()));

		// Segment #3
		onView(allOf(withId(R.id.flight_duration), withText("40m"))).perform(scrollTo());
		onView(allOf(withId(R.id.departure_arrival_time), withText("5:10PM - 9:50PM"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.airline_airplane_type), withText("Hawaiian Airlines 1182 - Boeing 757"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.departure_arrival_airport), withText("(LAX) Los Angeles - (OGG) Kahului"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_duration), withText("5h 40m"))).check(matches(isDisplayed()));

		// Layover
		onView(allOf(withId(R.id.flight_segment_layover_in), withText("Layover in (OGG) Kahului"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_segment_layover_duration), withText("1h 40m"))).check(matches(isDisplayed()));

		// Segment #4
		onView(allOf(withId(R.id.departure_arrival_time), withText("11:00PM - 11:40PM"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.airline_airplane_type), withText("Hawaiian Airlines 293 - Boeing 717"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.departure_arrival_airport), withText("(OGG) Kahului - (HNL) Honolulu"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_duration), withText("40m"))).check(matches(isDisplayed()));

		onView(withId(R.id.flight_total_duration)).perform(scrollTo());
		onView(allOf(withId(R.id.flight_total_duration), withText("Total Duration: 16h 50m"))).check(matches(isDisplayed()));
	}

	private void assertBundlePriceInFlight(String price) {
		onView(allOf(withId(R.id.bundle_price_label), withText("Bundle Total"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_price), withText(price + "/person"))).check(matches(isDisplayed()));
	}

}
