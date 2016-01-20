package com.expedia.bookings.test.phone.packages;

import com.expedia.bookings.test.espresso.PackageTestCase;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class PackageHotelResultsTest extends PackageTestCase {

	public void testResultsHeader() throws Throwable {
		PackageScreen.searchPackage();
		PackageScreen.seeHotelResults();
		PackageScreen.hotelResultsHeader().check(matches(withText("50 Results")));
	}
}
