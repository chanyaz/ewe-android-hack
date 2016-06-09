package com.expedia.bookings.test.phone.accessibility.packages;

import org.joda.time.LocalDate;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUser;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static com.expedia.bookings.test.espresso.CustomMatchers.atFlightListPosition;
import static com.expedia.bookings.test.espresso.CustomMatchers.withContentDescription;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class PackagesFlightsTest extends PackageTestCase {

	public void testPackageFlightsOverview() throws Throwable {
		SearchScreen.selectOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
		SearchScreen.searchButton().perform(click());
		Common.delay(1);
		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(1);
		PackageScreen.selectRoom();
		Common.delay(1);
		PackageScreen.flightList().check(matches(atFlightListPosition(1, withContentDescription(
			"Flight time is 9:00 am - 11:12 am with price difference of +$0. Flying with United. The flight duration is 5 hours 12 minutes with 0 stops.SFO to HNL. 5 hours 12 minutes.Button"))));
		PackageScreen.flightList().check(matches(atFlightListPosition(4, withContentDescription(
			"Flight time is 9:50 am - 11:40 pm with price difference of +$0. Flying with Hawaiian Airlines. The flight duration is 16 hours 50 minutes with 3 stops.SFO to SAN. 4 hours 37 minutes.Layover. 0 hours 48 minutes.SAN to LAX. 0 hours 54 minutes.Layover. 1 hour 1 minute.LAX to OGG. 5 hours 40 minutes.Layover. 1 hour 40 minutes.OGG to HNL. 0 hours 40 minutes.Button"))));
	}

	public void testPackageFlightsFilters() throws Throwable {
		SearchScreen.selectOriginAndDestination();
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

		EspressoUser.scrollToView(R.id.collapsed_container);
		PackageScreen.showMoreButton().check(matches(isDisplayed()));
		PackageScreen.showMoreButton().check(
			matches(ViewMatchers.withContentDescription(R.string.packages_flight_search_filter_show_more_cont_desc)));
		PackageScreen.showMoreButton().perform(click());
		EspressoUser.scrollToView(R.id.collapsed_container);
		PackageScreen.showMoreButton().check(
			matches(ViewMatchers.withContentDescription(R.string.packages_flight_search_filter_show_less_cont_desc)));
		PackageScreen.showMoreButton().perform(click());
	}
}
