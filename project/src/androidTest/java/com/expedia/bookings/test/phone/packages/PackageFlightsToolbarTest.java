package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.not;

public class PackageFlightsToolbarTest extends PackageTestCase {

	public void testPackageFlightsToolbar() throws Throwable {
		SearchScreen.selectOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
		SearchScreen.searchButton().perform(click());
		Common.delay(1);

		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(1);

		PackageScreen.selectRoom();
		Common.delay(1);

		PackageScreen.outboundFlight().perform(click());
		Common.delay(1);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Select flight to Detroit, MI")))));
		checkToolBarMenuItemsVisibility(true);

		PackageScreen.selectFlight(0);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Flight to Detroit, MI")))));
		checkToolBarMenuItemsVisibility(false);
		checkBaggageFeeToolBarText("Flight to Detroit, MI");
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		PackageScreen.inboundFLight().perform(click());
		Common.delay(1);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Select return flight")))));
		checkToolBarMenuItemsVisibility(true);


		PackageScreen.selectFlight(0);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Flight to San Francisco, CA")))));
		checkToolBarMenuItemsVisibility(false);
		checkBaggageFeeToolBarText("Flight to San Francisco, CA");
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

	public void checkBaggageFeeToolBarText(String previousToolBarText) {
		PackageScreen.baggageFeeInfo().check(matches(isDisplayed()));
		PackageScreen.baggageFeeInfo().perform(click());
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Baggage Fee Info")))));
		Common.pressBack();
		Common.delay(1);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText(previousToolBarText)))));
	}
}
