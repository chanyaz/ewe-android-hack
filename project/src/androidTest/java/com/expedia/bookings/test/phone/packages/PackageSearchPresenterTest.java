package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.TestValues;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class PackageSearchPresenterTest extends PackageTestCase {

	@Test
	public void testOriginSameAsDestination() throws Throwable {
		SearchScreen.origin().perform(click());
		Common.delay(1);
		PackageScreen.toolbarNavigationUp(R.id.search_toolbar).perform(click());
		// search button disabled upon entry. Enter location.
		SearchScreen.searchButton().check(matches(isDisplayed()));
		SearchScreen.origin().perform(click());
		SearchScreen.waitForSearchEditText().perform(typeText(TestValues.TYPE_TEXT_SFO));
		SearchScreenActions.selectLocation(TestValues.PACKAGE_ORIGIN_LOCATION_SFO);
		Common.delay(1);
		SearchScreen.waitForSearchEditText().perform(typeText(TestValues.TYPE_TEXT_SFO));
		SearchScreenActions.selectLocation(TestValues.DESTINATION_LOCATION_SFO);
		LocalDate startDate = LocalDate.now().plusDays(35);
		SearchScreenActions.chooseDatesWithDialog(startDate, null);
		//Search button will be enabled
		SearchScreen.searchButton().perform(click());
		PackageScreen.errorDialog(
			"Your departure and arrival locations are the same or too close to each other.\nPlease adjust your search and try again.")
			.check(matches(isDisplayed()));
	}

	@Test
	public void testNoSearchUntilDateAndLocationSelected() throws Throwable {
		SearchScreen.origin().perform(click());
		Common.delay(1);
		PackageScreen.toolbarNavigationUp(R.id.search_toolbar).perform(click());
		// search button disabled upon entry. Enter location.
		SearchScreen.searchButton().check(matches(isDisplayed()));
		SearchScreen.origin().perform(click());
		SearchScreen.waitForSearchEditText().check(matches(withHint("Flying from")));
		SearchScreen.waitForSearchEditText().perform(typeText(TestValues.TYPE_TEXT_SFO));
		SearchScreenActions.selectLocation(TestValues.PACKAGE_ORIGIN_LOCATION_SFO);
		Common.delay(1);
		PackageScreen.toolbarNavigationUp(R.id.search_toolbar).perform(click());
		//Search button will still be disabled
		SearchScreen.searchButton().perform(click());
		SearchScreen.searchButton().check(matches(isDisplayed()));
		SearchScreen.destination().perform(click());
		SearchScreen.waitForSearchEditText().check(matches(withHint("Flying to")));
		SearchScreen.waitForSearchEditText().perform(typeText(TestValues.TYPE_TEXT_DTW));
		SearchScreenActions.selectLocation(TestValues.DESTINATION_LOCATION_DTW);
		Common.pressBack();
		//Search button will still be disabled
		SearchScreen.searchButton().perform(click());
		SearchScreen.searchButton().check(matches(isDisplayed()));
		// Open calendar and select dates
		SearchScreen.selectDateButton().check(matches(withText(R.string.select_dates)));
		LocalDate startDate = LocalDate.now().plusDays(35);
		SearchScreen.selectDateButton().perform(click());
		SearchScreenActions.chooseDatesWithDialog(startDate, null);
		//Search button will be enabled
		SearchScreen.searchButton().perform(click());
	}

	@Test
	public void testDateButtonTextPopulation() throws Throwable {
		// Select location
		SearchScreenActions.selectPackageOriginAndDestination();
		Common.pressBack();
		// Open calendar
		SearchScreen.selectDateButton().check(matches(withText(R.string.select_dates)));

		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate autoDate = startDate.plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(40);

		// Select start date
		SearchScreen.selectDateButton().perform(click());
		SearchScreenActions.chooseDatesWithDialog(startDate, null);
		String expectedStartDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(startDate);
		String autoEndDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(autoDate);
		SearchScreen.selectDateButton().check(matches(withText(expectedStartDate + "  -  " + autoEndDate + " (1 night)")));

		// Select end date
		SearchScreen.selectDateButton().perform(click());
		SearchScreenActions.chooseDatesWithDialog(startDate, endDate);
		String expectedEndDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(endDate);
		String expected = expectedStartDate + "  -  " + expectedEndDate + " (5 nights)" ;
		SearchScreen.selectDateButton().check(matches(withText(expected)));
	}

	@Test
	public void testMaxPackageDuration() throws Throwable {
		// Select location
		SearchScreenActions.selectPackageOriginAndDestination();

		LocalDate startDate = LocalDate.now();
		LocalDate validEndDate = LocalDate.now().plusDays(26);
		LocalDate invalidEndDate = LocalDate.now().plusDays(27);

		//max duration of travel is 26 nights
		SearchScreenActions.chooseDatesWithDialog(startDate, null);
		SearchScreen.selectDateButton().perform(click());
		SearchScreenActions.chooseDatesWithDialog(startDate, invalidEndDate);
		String expectedStartDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(startDate);
		String expectedEndDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(validEndDate);

		String expected = expectedStartDate + "  -  " + expectedEndDate + " (26 nights)";
		SearchScreen.selectDateButton().check(matches(withText(expected)));
		SearchScreen.searchButton().perform(click());

		//Dialog no longer pops up because this is enforced within the calendar
	}

	@Test
	public void testPackageSearchWindow() throws Throwable {
		// Select location
		SearchScreenActions.selectPackageOriginAndDestination();

		LocalDate startDate = LocalDate.now().plusDays(300);
		LocalDate validEndDate = LocalDate.now().plusDays(326);
		LocalDate invalidEndDate = LocalDate.now().plusDays(360);

		//search upto 11 months in advance
		SearchScreenActions.chooseDatesWithDialog(startDate, null);
		SearchScreen.selectDateButton().perform(click());
		SearchScreenActions.chooseDatesWithDialog(startDate, invalidEndDate);
		String expectedStartDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(startDate);
		String expectedEndDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(validEndDate);

		//We tried to click 360 days ahead but that's beyond the max, so defaults to 26
		String expected = expectedStartDate + "  -  " + expectedEndDate + " (26 nights)";
		SearchScreen.selectDateButton().check(matches(withText(expected)));
		SearchScreen.searchButton().perform(click());
	}

	@Test
	public void testSameDay() throws Throwable {
		// Select location
		SearchScreenActions.selectPackageOriginAndDestination();
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now();

		//select same day
		SearchScreenActions.chooseDatesWithDialog(startDate, null);
		SearchScreen.selectDateButton().perform(click());
		SearchScreenActions.chooseDatesWithDialog(startDate, endDate);
		String expectedStartDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(startDate);
		String expectedEndDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(endDate.plusDays(1));
		String expected = expectedStartDate + "  -  " + expectedEndDate + " (1 night)";
		SearchScreen.selectDateButton().check(matches(withText(expected)));
		SearchScreen.searchButton().perform(click());
	}
}
