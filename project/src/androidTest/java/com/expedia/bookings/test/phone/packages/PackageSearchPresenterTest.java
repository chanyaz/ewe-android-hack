package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class PackageSearchPresenterTest extends PackageTestCase {

	public void testNoSearchUntilDateAndLocationSelected() throws Throwable {
		// search button disabled upon entry. Enter location.
		PackageScreen.searchButton().perform(click());
		PackageScreen.searchButton().check(matches(isDisplayed()));
		PackageScreen.destination().perform(typeText("SFO"));
		PackageScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		//Search button will still be disabled
		PackageScreen.searchButton().perform(click());
		PackageScreen.searchButton().check(matches(isDisplayed()));
		PackageScreen.arrival().perform(typeText("SFO"));
		PackageScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		//Search button will still be disabled
		PackageScreen.searchButton().perform(click());
		PackageScreen.searchButton().check(matches(isDisplayed()));
		// Open calendar and select dates
		PackageScreen.selectDateButton().check(matches(withText(R.string.select_dates)));
		LocalDate startDate = LocalDate.now().plusDays(35);
		PackageScreen.selectDates(startDate, null);
		//Search button will be enabled
		PackageScreen.searchButton().perform(click());
	}

	public void testDateButtonTextPopulation() throws Throwable {
		// Select location
		PackageScreen.destination().perform(typeText("SFO"));
		PackageScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		PackageScreen.arrival().perform(typeText("SFO"));
		PackageScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		// Open calendar
		PackageScreen.selectDateButton().check(matches(withText(R.string.select_dates)));

		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);

		// Select start date
		PackageScreen.selectDates(startDate, null);
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		PackageScreen.selectDateButton().check(matches(withText(expectedStartDate + " - Select check out date")));

		// Select end date
		PackageScreen.selectDates(startDate, endDate);
		String expectedEndDate = DateUtils.localDateToMMMd(endDate);
		String expected = expectedStartDate + " - " + expectedEndDate + " (5 nights)" ;
		PackageScreen.selectDateButton().check(matches(withText(expected)));
	}
}
