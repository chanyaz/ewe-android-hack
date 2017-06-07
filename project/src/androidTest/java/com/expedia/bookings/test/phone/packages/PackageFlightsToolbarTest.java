package com.expedia.bookings.test.phone.packages;

import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

public class PackageFlightsToolbarTest extends PackageTestCase {

	@Test
	public void testPackageFlightsToolbar() throws Throwable {
		SearchScreen.selectPackageOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
		SearchScreen.searchButton().perform(click());
		HotelScreen.selectHotel("Package Happy Path");
		PackageScreen.selectRoom();

		PackageScreen.flightsToolbar().perform(waitFor(isDisplayed(), 5, TimeUnit.SECONDS));
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Select flight to Detroit, MI")))));
		PackageScreen.checkFlightToolBarMenuItemsVisibility(true);

		PackageScreen.selectFlight(0);
		PackageScreen.flightsToolbar().perform(waitFor(isDisplayed(), 5, TimeUnit.SECONDS));
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Flight to Detroit, MI")))));
		PackageScreen.checkFlightToolBarMenuItemsVisibility(false);
		checkBaggageFeeToolBarText("Flight to Detroit, MI");
		PackageScreen.selectThisFlight().perform(click());

		PackageScreen.flightsToolbar().perform(waitFor(isDisplayed(), 5, TimeUnit.SECONDS));
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Select return flight")))));
		PackageScreen.checkFlightToolBarMenuItemsVisibility(true);


		PackageScreen.selectFlight(0);
		PackageScreen.flightsToolbar().perform(waitFor(isDisplayed(), 5, TimeUnit.SECONDS));
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Flight to San Francisco, CA")))));
		PackageScreen.checkFlightToolBarMenuItemsVisibility(false);
		checkBaggageFeeToolBarText("Flight to San Francisco, CA");
	}


	private void checkBaggageFeeToolBarText(String previousToolBarText) {
		PackageScreen.baggageFeeInfo().check(matches(isDisplayed()));
		PackageScreen.baggageFeeInfo().perform(click());
		PackageScreen.flightsToolbar().check(matches(not(isDisplayed())));
		onView(allOf(withId(R.id.toolbar),hasDescendant(withText("Baggage fee info")))).check(matches(isDisplayed()));
		Common.pressBack();
		PackageScreen.flightsToolbar().perform(waitFor(isDisplayed(), 5, TimeUnit.SECONDS));
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText(previousToolBarText)))));
	}
}
