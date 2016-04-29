package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class PackageSearchPresenterTest extends PackageTestCase {

	public void testOriginSameAsDesitination() throws Throwable {
		// search button disabled upon entry. Enter location.
		PackageScreen.searchButton().perform(click());
		PackageScreen.searchButton().check(matches(isDisplayed()));
		PackageScreen.destination().perform(click());
		PackageScreen.searchEditText().perform(typeText("SFO"));
		PackageScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		//Search button will still be disabled
		PackageScreen.searchButton().perform(click());
		PackageScreen.searchButton().check(matches(isDisplayed()));
		PackageScreen.arrival().perform(click());
		PackageScreen.searchEditText().perform(typeText("SFO"));
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
		PackageScreen.errorDialog(
			"Departure and arrival airports must be different.").check(matches(isDisplayed()));
	}

	public void testNoSearchUntilDateAndLocationSelected() throws Throwable {
		// search button disabled upon entry. Enter location.
		PackageScreen.searchButton().perform(click());
		PackageScreen.searchButton().check(matches(isDisplayed()));
		PackageScreen.destination().perform(click());
		PackageScreen.searchEditText().check(matches(withHint("Flying from")));
		PackageScreen.searchEditText().perform(typeText("SFO"));
		PackageScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		//Search button will still be disabled
		PackageScreen.searchButton().perform(click());
		PackageScreen.searchButton().check(matches(isDisplayed()));
		PackageScreen.arrival().perform(click());
		PackageScreen.searchEditText().check(matches(withHint("Flying to")));
		PackageScreen.searchEditText().perform(typeText("DTW"));
		PackageScreen.selectLocation("Detroit, MI (DTW-Detroit Metropolitan Wayne County)");
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
		PackageScreen.selectOriginAndDestination();
		// Open calendar
		PackageScreen.selectDateButton().check(matches(withText(R.string.select_dates)));

		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate autoDate = startDate.plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(40);

		// Select start date
		PackageScreen.selectDates(startDate, null);
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		String autoEndDate = DateUtils.localDateToMMMd(autoDate);
		PackageScreen.selectDateButton().check(matches(withText(expectedStartDate + " - " + autoEndDate + " (1 night)")));

		// Select end date
		PackageScreen.selectDates(startDate, endDate);
		String expectedEndDate = DateUtils.localDateToMMMd(endDate);
		String expected = expectedStartDate + " - " + expectedEndDate + " (5 nights)" ;
		PackageScreen.selectDateButton().check(matches(withText(expected)));
	}

	public void testMaxPackageDuration() throws Throwable {
		// Select location
		PackageScreen.selectOriginAndDestination();

		LocalDate startDate = LocalDate.now();
		LocalDate validEndDate = LocalDate.now().plusDays(26);
		LocalDate invalidEndDate = LocalDate.now().plusDays(27);

		//max duration of travel is 26 nights
		PackageScreen.selectDates(startDate, null);
		PackageScreen.selectDates(startDate, invalidEndDate);
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		String expectedEndDate = DateUtils.localDateToMMMd(validEndDate);

		String expected = expectedStartDate + " - " + expectedEndDate + " (26 nights)";
		PackageScreen.selectDateButton().check(matches(withText(expected)));
		PackageScreen.searchButton().perform(click());

		//Dialog no longer pops up because this is enforced within the calendar
	}

	public void testPackageSearchWindow() throws Throwable {
		// Select location
		PackageScreen.selectOriginAndDestination();

		LocalDate startDate = LocalDate.now().plusDays(300);
		LocalDate validEndDate = LocalDate.now().plusDays(326);
		LocalDate invalidEndDate = LocalDate.now().plusDays(360);

		//search upto 11 months in advance
		PackageScreen.selectDates(startDate, null);
		PackageScreen.selectDates(startDate, invalidEndDate);
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		String expectedEndDate = DateUtils.localDateToMMMd(validEndDate);

		//We tried to click 360 days ahead but that's beyond the max, so defaults to 26
		String expected = expectedStartDate + " - " + expectedEndDate + " (26 nights)";
		PackageScreen.selectDateButton().check(matches(withText(expected)));
		PackageScreen.searchButton().perform(click());
	}

	public void testSameDay() throws Throwable {
		// Select location
		PackageScreen.selectOriginAndDestination();
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now();

		//select same day
		PackageScreen.selectDates(startDate, null);
		PackageScreen.selectDates(startDate, endDate);
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		String expectedEndDate = DateUtils.localDateToMMMd(endDate.plusDays(1));
		String expected = expectedStartDate + " - " + expectedEndDate + " (1 night)";
		PackageScreen.selectDateButton().check(matches(withText(expected)));
		PackageScreen.searchButton().perform(click());
	}
}
