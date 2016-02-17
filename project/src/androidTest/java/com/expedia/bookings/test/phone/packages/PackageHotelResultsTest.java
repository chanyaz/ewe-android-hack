package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;

import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.utils.DateUtils;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class PackageHotelResultsTest extends PackageTestCase {

	public void testResultsHeader() throws Throwable {
		PackageScreen.searchPackage();
		PackageScreen.seeHotelResults();
		PackageScreen.hotelResultsHeader().check(matches(withText("50 Results")));
	}

	public void testToolbarText() throws Throwable {
		PackageScreen.searchPackageFor(2, 1);
		PackageScreen.seeHotelResults();
		PackageScreen.hotelResultsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(
			isDisplayed(), withText("Hotels in Detroit, MI")))));

		String startDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(3));
		String endDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(8));
		PackageScreen.hotelResultsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(
			isDisplayed(), withText(startDate + " - " + endDate + ", 3 Guests")))));
	}
}
