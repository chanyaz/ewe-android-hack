package com.expedia.bookings.test.phone.accessibility.packages;

import org.joda.time.LocalDate;
import org.junit.Test;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.CustomMatchers;
import com.expedia.bookings.test.espresso.EspressoUser;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.atFlightListPosition;
import static com.expedia.bookings.test.espresso.CustomMatchers.withContentDescription;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.core.AllOf.allOf;

public class PackagesFlightsTest extends PackageTestCase {

	@Test
	public void testPackageFlightsOverview() throws Throwable {
		SearchScreen.selectPackageOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
		SearchScreen.searchButton().perform(click());
		Common.delay(1);
		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(1);
		PackageScreen.selectFirstRoom();
		Common.delay(1);
		PackageScreen.flightList().check(matches(atFlightListPosition(1, withContentDescription(
			"Best Flight. cheap • short • popular departure.  Flight time is 9:00 am to 11:12 am with price difference of +$0. Flying with United. The flight duration is 5 hours 12 minutes with 0 stops SFO to HNL. 5 hours 12 minutes.  Button"))));
		PackageScreen.flightList().check(matches(atFlightListPosition(4, withContentDescription(
			"Flight time is 9:50 am to 11:40 pm with price difference of +$0. Flying with Hawaiian Airlines. The flight duration is 16 hours 50 minutes with 3 stops SFO to SAN. 4 hours 37 minutes.  Layover 0 hours 48 minutes.  SAN to LAX. 0 hours 54 minutes.  Layover 1 hour 1 minute.  LAX to OGG. 5 hours 40 minutes.  Layover 1 hour 40 minutes.  OGG to HNL. 0 hours 40 minutes.  Button"))));
		PackageScreen.selectFlight(1);
		onView(allOf(withId(R.id.flight_segment_layover_duration), hasSibling(withText("Layover in (SAN) San Diego")))).check(matches(withContentDescription("48 minutes")));
		onView(allOf(withId(R.id.flight_duration), hasSibling(withText("Hawaiian Airlines 497 • Boeing 737-900")))).check(matches(withContentDescription("4 hour 37 minutes")));

	}

	@Test
	public void testPackageFlightsFilters() throws Throwable {
		SearchScreen.selectPackageOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
		SearchScreen.searchButton().perform(click());

		HotelScreen.selectHotel("Package Happy Path");
		HotelScreen.selectRoomButton().perform(click());
		HotelScreen.clickRoom("Packages Flights Show More Airlines");
		PackageScreen.selectRoom();

		PackageScreen.flightList().perform(waitForViewToDisplay());
		// Open flights filter
		PackageScreen.flightsToolbarFilterMenu().perform(click());
		PackageScreen.flightFilterView().perform(waitForViewToDisplay());

		//Filter by stops content description test
		PackageScreen.checkBoxContainerWithTitle("Nonstop").check(matches(CustomMatchers.withContentDescription("3 available Nonstop flights. Checkbox. Unchecked.")));
		PackageScreen.tickCheckboxWithText("Nonstop");
		PackageScreen.checkBoxContainerWithTitle("Nonstop").check(matches(CustomMatchers.withContentDescription("3 available Nonstop flights. Checkbox. Checked.")));
		PackageScreen.checkBoxContainerWithTitle("2+ Stops").check(matches(CustomMatchers.withContentDescription("1 available 2+ Stops flights. Checkbox. Unchecked.")));
		PackageScreen.tickCheckboxWithText("2+ Stops");
		PackageScreen.checkBoxContainerWithTitle("2+ Stops").check(matches(CustomMatchers.withContentDescription("1 available 2+ Stops flights. Checkbox. Checked.")));
		PackageScreen.resetFlightsFliter();

		EspressoUser.scrollToView(R.id.collapsed_container);
		PackageScreen.showMoreButton().check(matches(isDisplayed()));
		PackageScreen.showMoreButton().check(
			matches(ViewMatchers.withContentDescription(R.string.packages_flight_search_filter_show_more_cont_desc)));
		PackageScreen.showMoreButton().perform(click());
		EspressoUser.scrollToView(R.id.collapsed_container);
		PackageScreen.showMoreButton().check(
			matches(ViewMatchers.withContentDescription(R.string.packages_flight_search_filter_show_less_cont_desc)));
		PackageScreen.showMoreButton().perform(click());

		//Filter by airlines content description test
		PackageScreen.checkBoxContainerWithTitle("Hawaiian Airlines").check(matches(CustomMatchers.withContentDescription("1 available Hawaiian Airlines flights. Checkbox. Unchecked.")));
		PackageScreen.tickCheckboxWithText("Hawaiian Airlines");
		PackageScreen.checkBoxContainerWithTitle("Hawaiian Airlines").check(matches(CustomMatchers.withContentDescription("1 available Hawaiian Airlines flights. Checkbox. Checked.")));
		PackageScreen.checkBoxContainerWithTitle("Delta").check(matches(CustomMatchers.withContentDescription("1 available Delta flights. Checkbox. Unchecked.")));
		PackageScreen.tickCheckboxWithText("Delta");
		PackageScreen.checkBoxContainerWithTitle("Delta").check(matches(CustomMatchers.withContentDescription("1 available Delta flights. Checkbox. Checked.")));
	}
}
