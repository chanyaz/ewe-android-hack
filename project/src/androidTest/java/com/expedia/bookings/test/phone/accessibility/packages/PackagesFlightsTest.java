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
		PackageScreen.flightList().check(matches(atFlightListPosition(1, withContentDescription("Flight time is 9:00 am - 11:12 am with price difference of +$0. Flying with United and the flight duration is 5 hours 12 minutes. Click to view flight details"))));
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
		PackageScreen.showMoreButton().check(matches(ViewMatchers.withContentDescription(R.string.packages_flight_search_filter_show_more_cont_desc)));
		PackageScreen.showMoreButton().perform(click());
		EspressoUser.scrollToView(R.id.collapsed_container);
		PackageScreen.showMoreButton().check(matches(ViewMatchers.withContentDescription(R.string.packages_flight_search_filter_show_less_cont_desc)));
		PackageScreen.showMoreButton().perform(click());
	}
}
