package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withContentDescription;
import static com.expedia.bookings.test.espresso.CustomMatchers.withNavigationContentDescription;

public class PackageSearchPresenterTest extends PackageTestCase {

	public void testAccessibility() throws Throwable {
		Common.delay(1);
		checkToolbarNavContentDescription(true);
		PackageScreen.toolbarNavigationUp(R.id.search_toolbar).perform(click());
		checkToolbarNavContentDescription(false);
		SearchScreen.origin().check(matches(withContentDescription("Click here to select where you want to fly from")));
		SearchScreen.origin().perform(click());
		checkToolbarNavContentDescription(true);
		SearchScreen.searchEditText().perform(typeText("SFO"));
		SearchScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		Common.delay(1);
		checkToolbarNavContentDescription(true);
		PackageScreen.toolbarNavigationUp(R.id.search_toolbar).perform(click());
		checkToolbarNavContentDescription(false);
		SearchScreen.destination().check(matches(withContentDescription("Click here to select where you want to fly to")));
		SearchScreen.destination().perform(click());
		checkToolbarNavContentDescription(true);
		SearchScreen.searchEditText().perform(typeText("DTW"));
		SearchScreen.selectLocation("Detroit, MI (DTW-Detroit Metropolitan Wayne County)");
		Common.delay(1);

		Common.pressBack();
		checkToolbarNavContentDescription(false);
		SearchScreen.calendarCard().check(matches(withContentDescription("Click here to select your travel dates")));
		SearchScreen.calendarCard().perform(click());
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);

		SearchScreen.origin().check(matches(withContentDescription("Flying from San Francisco, CA (SFO-San Francisco Intl.)")));
		SearchScreen.destination().check(matches(withContentDescription("Flying to Detroit, MI (DTW-Detroit Metropolitan Wayne County)")));
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		String expectedEndDate = DateUtils.localDateToMMMd(endDate);
		SearchScreen.calendarCard().check(matches(withContentDescription("Your trip is from " + expectedStartDate + " to " + expectedEndDate + " for (5 nights)")));
		checkToolbarNavContentDescription(false);
	}

	private void checkToolbarNavContentDescription(boolean isBackButton) {
		if (isBackButton) {
			PackageScreen.searchToolbar().check(matches(withNavigationContentDescription("Back to search screen")));
		}
		else {
			PackageScreen.searchToolbar().check(matches(withNavigationContentDescription("Close search screen")));
		}
	}

	public void testOriginSameAsDestination() throws Throwable {
		Common.delay(1);
		PackageScreen.toolbarNavigationUp(R.id.search_toolbar).perform(click());
		// search button disabled upon entry. Enter location.
		SearchScreen.searchButton().check(matches(isDisplayed()));
		SearchScreen.origin().perform(click());
		SearchScreen.searchEditText().perform(typeText("SFO"));
		SearchScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		Common.delay(1);
		SearchScreen.searchEditText().perform(ViewActions.waitForViewToDisplay());
		SearchScreen.searchEditText().perform(typeText("SFO"));
		SearchScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		LocalDate startDate = LocalDate.now().plusDays(35);
		SearchScreen.selectDates(startDate, null);
		//Search button will be enabled
		SearchScreen.searchButton().perform(click());
		PackageScreen.errorDialog(
			"Departure and arrival airports must be different.").check(matches(isDisplayed()));
	}

	public void testNoSearchUntilDateAndLocationSelected() throws Throwable {
		Common.delay(1);
		PackageScreen.toolbarNavigationUp(R.id.search_toolbar).perform(click());
		// search button disabled upon entry. Enter location.
		SearchScreen.searchButton().check(matches(isDisplayed()));
		SearchScreen.origin().perform(click());
		SearchScreen.searchEditText().check(matches(withHint("Flying from")));
		SearchScreen.searchEditText().perform(typeText("SFO"));
		SearchScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		Common.delay(1);
		SearchScreen.searchEditText().perform(ViewActions.waitForViewToDisplay());
		PackageScreen.toolbarNavigationUp(R.id.search_toolbar).perform(click());
		//Search button will still be disabled
		SearchScreen.searchButton().perform(click());
		SearchScreen.searchButton().check(matches(isDisplayed()));
		SearchScreen.destination().perform(click());
		SearchScreen.searchEditText().check(matches(withHint("Flying to")));
		SearchScreen.searchEditText().perform(typeText("DTW"));
		SearchScreen.selectLocation("Detroit, MI (DTW-Detroit Metropolitan Wayne County)");
		Common.pressBack();
		//Search button will still be disabled
		SearchScreen.searchButton().perform(click());
		SearchScreen.searchButton().check(matches(isDisplayed()));
		// Open calendar and select dates
		SearchScreen.selectDateButton().check(matches(withText(R.string.select_dates)));
		LocalDate startDate = LocalDate.now().plusDays(35);
		SearchScreen.calendarCard().perform(click());
		SearchScreen.selectDates(startDate, null);
		//Search button will be enabled
		SearchScreen.searchButton().perform(click());
	}

	public void testDateButtonTextPopulation() throws Throwable {
		// Select location
		SearchScreen.selectOriginAndDestination();
		Common.pressBack();
		// Open calendar
		SearchScreen.selectDateButton().check(matches(withText(R.string.select_dates)));

		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate autoDate = startDate.plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(40);

		// Select start date
		SearchScreen.calendarCard().perform(click());
		SearchScreen.selectDates(startDate, null);
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		String autoEndDate = DateUtils.localDateToMMMd(autoDate);
		SearchScreen.selectDateButton().check(matches(withText(expectedStartDate + " - " + autoEndDate + " (1 night)")));

		// Select end date
		SearchScreen.calendarCard().perform(click());
		SearchScreen.selectDates(startDate, endDate);
		String expectedEndDate = DateUtils.localDateToMMMd(endDate);
		String expected = expectedStartDate + " - " + expectedEndDate + " (5 nights)" ;
		SearchScreen.selectDateButton().check(matches(withText(expected)));
	}

	public void testMaxPackageDuration() throws Throwable {
		// Select location
		SearchScreen.selectOriginAndDestination();

		LocalDate startDate = LocalDate.now();
		LocalDate validEndDate = LocalDate.now().plusDays(26);
		LocalDate invalidEndDate = LocalDate.now().plusDays(27);

		//max duration of travel is 26 nights
		SearchScreen.selectDates(startDate, null);
		SearchScreen.calendarCard().perform(click());
		SearchScreen.selectDates(startDate, invalidEndDate);
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		String expectedEndDate = DateUtils.localDateToMMMd(validEndDate);

		String expected = expectedStartDate + " - " + expectedEndDate + " (26 nights)";
		SearchScreen.selectDateButton().check(matches(withText(expected)));
		SearchScreen.searchButton().perform(click());

		//Dialog no longer pops up because this is enforced within the calendar
	}

	public void testPackageSearchWindow() throws Throwable {
		// Select location
		SearchScreen.selectOriginAndDestination();

		LocalDate startDate = LocalDate.now().plusDays(300);
		LocalDate validEndDate = LocalDate.now().plusDays(326);
		LocalDate invalidEndDate = LocalDate.now().plusDays(360);

		//search upto 11 months in advance
		SearchScreen.selectDates(startDate, null);
		SearchScreen.calendarCard().perform(click());
		SearchScreen.selectDates(startDate, invalidEndDate);
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		String expectedEndDate = DateUtils.localDateToMMMd(validEndDate);

		//We tried to click 360 days ahead but that's beyond the max, so defaults to 26
		String expected = expectedStartDate + " - " + expectedEndDate + " (26 nights)";
		SearchScreen.selectDateButton().check(matches(withText(expected)));
		SearchScreen.searchButton().perform(click());
	}

	public void testSameDay() throws Throwable {
		// Select location
		SearchScreen.selectOriginAndDestination();
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now();

		//select same day
		SearchScreen.selectDates(startDate, null);
		SearchScreen.calendarCard().perform(click());
		SearchScreen.selectDates(startDate, endDate);
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		String expectedEndDate = DateUtils.localDateToMMMd(endDate.plusDays(1));
		String expected = expectedStartDate + " - " + expectedEndDate + " (1 night)";
		SearchScreen.selectDateButton().check(matches(withText(expected)));
		SearchScreen.searchButton().perform(click());
	}
}
