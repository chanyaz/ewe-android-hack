package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withContentDescription;
import static org.hamcrest.CoreMatchers.allOf;

public class PackageFlightsOverviewTest extends PackageTestCase {

	@Test
	public void testPackageFlightsOverview() throws Throwable {
		SearchScreen.selectPackageOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
		SearchScreen.searchButton().perform(click());
		Common.delay(1);

		Common.delay(1);

		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(1);

		PackageScreen.selectFirstRoom();
		Common.delay(1);

		Common.delay(1);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(isDisplayed(), withText("Outbound to Detroit (DTW)")))));
		PackageScreen.checkFlightToolBarMenuItemsVisibility(true);
		PackageScreen.selectFlight(1);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(isDisplayed(), withText("Outbound to Detroit (DTW)")))));
		PackageScreen.checkFlightToolBarMenuItemsVisibility(false);
		assertSegmentData();
		assertBundlePriceInFlight("$1,932");

		onView(allOf(withId(R.id.select_flight_button), withText("Select this Flight"))).check(matches(isDisplayed()));
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);
	}

	private void assertSegmentData() {
		// Segment #1
		onView(allOf(withId(R.id.departure_arrival_time), withText("9:50 am - 2:27 pm"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.airline_airplane_type), withText("Hawaiian Airlines 497 • Boeing 737-900"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.departure_arrival_airport), withText("(SFO) San Francisco - (SAN) San Diego"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.departure_arrival_airport), withContentDescription("(SFO) San Francisco to (SAN) San Diego"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_duration), withText("4h 37m"))).check(matches(isDisplayed()));

		// Layover
		onView(allOf(withId(R.id.flight_segment_layover_in), withText("Layover in (SAN) San Diego"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_segment_layover_duration), withText("48m"))).check(matches(isDisplayed()));

		// Segment #2
		onView(allOf(withId(R.id.departure_arrival_time), withText("3:15 pm - 4:09 pm"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.airline_airplane_type), withText("Hawaiian Airlines 5496 • Boeing 717"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.departure_arrival_airport), withText("(SAN) San Diego - (LAX) Los Angeles"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.departure_arrival_airport), withContentDescription("(SAN) San Diego to (LAX) Los Angeles"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_duration), withText("54m"))).check(matches(isDisplayed()));

		// Layover
		onView(allOf(withId(R.id.flight_segment_layover_in), withText("Layover in (LAX) Los Angeles"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_segment_layover_duration), withText("1h 1m"))).check(matches(isDisplayed()));

		// Segment #3
		onView(allOf(withId(R.id.flight_duration), withText("40m"))).perform(scrollTo());
		onView(allOf(withId(R.id.departure_arrival_time), withText("5:10 pm - 9:50 pm"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.airline_airplane_type), withText("Hawaiian Airlines 1182 • Boeing 757"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.departure_arrival_airport), withText("(LAX) Los Angeles - (OGG) Kahului"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.departure_arrival_airport), withContentDescription("(LAX) Los Angeles to (OGG) Kahului"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_duration), withText("5h 40m"))).check(matches(isDisplayed()));

		// Layover
		onView(allOf(withId(R.id.flight_segment_layover_in), withText("Layover in (OGG) Kahului"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_segment_layover_duration), withText("1h 40m"))).check(matches(isDisplayed()));

		// Segment #4
		onView(allOf(withId(R.id.departure_arrival_time), withText("11:00 pm - 11:40 pm"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.airline_airplane_type), withText("Hawaiian Airlines 293 • Boeing 717"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.departure_arrival_airport), withText("(OGG) Kahului - (HNL) Honolulu"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.departure_arrival_airport), withContentDescription("(OGG) Kahului to (HNL) Honolulu"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.flight_duration), withText("40m"))).check(matches(isDisplayed()));


		onView(Matchers.allOf(isDescendantOfA(withId(R.id.widget_flight_overview)),
			withId(R.id.flight_total_duration))).perform(scrollTo());
		onView(allOf(isDescendantOfA(withId(R.id.widget_flight_overview)),
			withId(R.id.flight_total_duration), withText("Total Duration: 16h 50m"))).check(matches(isDisplayed()));
		onView(allOf(isDescendantOfA(withId(R.id.widget_flight_overview)),
			withId(R.id.flight_total_duration), withContentDescription("Total Duration: 16 hour 50 minutes"))).check(matches(isDisplayed()));
	}

	private void assertBundlePriceInFlight(String price) {
		onView(allOf(withId(R.id.bundle_price_label), withText("Bundle Total"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_price), withText(price + "/person"))).check(matches(isDisplayed()));
	}
}
