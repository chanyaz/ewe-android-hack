package com.expedia.bookings.test.phone.packages;
import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.newhotels.HotelScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

public class PackageFlightsToolbarTest extends PackageTestCase {

	public void testPackageFlightsToolbar() throws Throwable {
		PackageScreen.destination().perform(typeText("SFO"));
		PackageScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		PackageScreen.arrival().perform(typeText("DTW"));
		PackageScreen.selectLocation("Detroit, MI (DTW-Detroit Metropolitan Wayne County)");
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		PackageScreen.selectDates(startDate, endDate);
		PackageScreen.searchButton().perform(click());
		Common.delay(1);

		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		HotelScreen.selectHotel("packagehappypath");
		Common.delay(1);

		HotelScreen.selectRoom();
		Common.delay(1);

		PackageScreen.outboundFlight().perform(click());
		Common.delay(1);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(isDisplayed(), withText("Select flight to Detroit, MI")))));
		checkToolBarMenuItemsVisibility(true);

		PackageScreen.selectFlight(0);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(isDisplayed(), withText("Flight to Detroit, MI")))));
		checkToolBarMenuItemsVisibility(false);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		PackageScreen.inboundFLight().perform(click());
		Common.delay(1);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(isDisplayed(), withText("Select return flight")))));
		checkToolBarMenuItemsVisibility(true);


		PackageScreen.selectFlight(0);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(isDisplayed(), withText("Flight to San Francisco, CA")))));
		checkToolBarMenuItemsVisibility(false);
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

}
