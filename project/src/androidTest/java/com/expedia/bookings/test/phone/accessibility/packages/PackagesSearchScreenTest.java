package com.expedia.bookings.test.phone.accessibility.packages;

import org.joda.time.LocalDate;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static com.expedia.bookings.test.espresso.CustomMatchers.withContentDescription;
import static com.expedia.bookings.test.espresso.CustomMatchers.withNavigationContentDescription;

public class PackagesSearchScreenTest extends PackageTestCase {

	public void testPackageSearchScreen() throws Throwable {
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

		Common.delay(1);
		SearchScreen.selectGuestsButton().perform(click());
		SearchScreen.addAdultsButton().check(matches(ViewMatchers.withContentDescription("Add one adult traveler")));
		SearchScreen.removeAdultsButton().check(matches(ViewMatchers.withContentDescription("Remove one adult traveler")));
		SearchScreen.addChildButton().check(matches(ViewMatchers.withContentDescription("Add one child traveler")));
		SearchScreen.removeChildButton().check(matches(ViewMatchers.withContentDescription("Remove one child traveler")));
		Common.delay(1);
	}

	private void checkToolbarNavContentDescription(boolean isBackButton) {
		if (isBackButton) {
			PackageScreen.searchToolbar().check(matches(withNavigationContentDescription("Back to search screen")));
		}
		else {
			PackageScreen.searchToolbar().check(matches(withNavigationContentDescription("Close search screen")));
		}
	}
}
