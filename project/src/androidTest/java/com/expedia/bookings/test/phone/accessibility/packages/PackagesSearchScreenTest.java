package com.expedia.bookings.test.phone.accessibility.packages;

import org.joda.time.LocalDate;
import org.junit.Test;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.TestValues;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static com.expedia.bookings.test.espresso.CustomMatchers.withContentDescription;
import static com.expedia.bookings.test.espresso.CustomMatchers.withNavigationContentDescription;

public class PackagesSearchScreenTest extends PackageTestCase {

	@Test
	public void testPackageSearchScreen() throws Throwable {
		Common.delay(1);
		checkToolbarNavContentDescription(true);
		PackageScreen.toolbarNavigationUp(R.id.search_toolbar).perform(click());
		checkToolbarNavContentDescription(false);
		SearchScreen.origin().check(matches(withContentDescription("Flying from. Button")));
		SearchScreen.origin().perform(click());
		checkToolbarNavContentDescription(true);
		SearchScreen.searchEditText().perform(typeText(TestValues.TYPE_TEXT_SFO));
		SearchScreen.selectLocation(TestValues.PACKAGE_ORIGIN_LOCATION_SFO);
		Common.delay(1);
		checkToolbarNavContentDescription(true);
		PackageScreen.toolbarNavigationUp(R.id.search_toolbar).perform(click());
		checkToolbarNavContentDescription(false);
		SearchScreen.destination().check(matches(withContentDescription("Flying to. Button")));
		SearchScreen.destination().perform(click());
		checkToolbarNavContentDescription(true);
		SearchScreen.searchEditText().perform(typeText(TestValues.TYPE_TEXT_DTW));
		SearchScreen.selectLocation(TestValues.DESTINATION_LOCATION_DTW);
		Common.delay(1);

		Common.pressBack();
		checkToolbarNavContentDescription(false);
		SearchScreen.calendarCard().check(matches(withContentDescription("Select Dates Button. Opens dialog. ")));
		SearchScreen.calendarCard().perform(click());
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);

		SearchScreen.selectGuestsButton().check(matches(withContentDescription("Number of travelers. Button. Opens dialog. 1 traveler")));
		SearchScreen.selectGuestsButton().perform(click());
		SearchScreen.incrementAdultsButton();
		SearchScreen.searchAlertDialogDone().perform(click());
		SearchScreen.selectGuestsButton().check(matches(withContentDescription("Number of travelers. Button. Opens dialog. 2 travelers")));
		SearchScreen.selectGuestsButton().perform(click());
		SearchScreen.incrementChildrenButton();
		SearchScreen.searchAlertDialogDone().perform(click());
		SearchScreen.selectGuestsButton().check(matches(withContentDescription("Number of travelers. Button. Opens dialog. 3 travelers")));
		SearchScreen.selectGuestsButton().perform(click());
		SearchScreen.removeChildButton().perform(click());
		SearchScreen.searchAlertDialogDone().perform(click());
		SearchScreen.selectGuestsButton().check(matches(withContentDescription("Number of travelers. Button. Opens dialog. 2 travelers")));
		SearchScreen.selectGuestsButton().perform(click());
		SearchScreen.removeAdultsButton().perform(click());
		SearchScreen.searchAlertDialogDone().perform(click());
		SearchScreen.selectGuestsButton().check(matches(withContentDescription("Number of travelers. Button. Opens dialog. 1 traveler")));

		SearchScreen.origin().check(matches(withContentDescription("Flying from. Button. SFO - San Francisco Intl.")));
		SearchScreen.destination().check(matches(withContentDescription("Flying to. Button. Detroit, MI (DTW-Detroit Metropolitan Wayne County)")));
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		String expectedEndDate = DateUtils.localDateToMMMd(endDate);
		SearchScreen.calendarCard().check(matches(withContentDescription("Trip dates Button. Opens dialog. " + expectedStartDate + " to " + expectedEndDate + " (5 nights)")));
		checkToolbarNavContentDescription(false);

		Common.delay(1);
		SearchScreen.selectGuestsButton().perform(click());
		SearchScreen.addAdultsButton().check(matches(ViewMatchers.withContentDescription("Add one adult traveler")));
		SearchScreen.removeAdultsButton().check(matches(ViewMatchers.withContentDescription("Remove one adult traveler")));
		SearchScreen.addChildButton().check(matches(ViewMatchers.withContentDescription("Add one child traveler")));
		SearchScreen.removeChildButton().check(matches(ViewMatchers.withContentDescription("Remove one child traveler")));
		SearchScreen.incrementChildrenButton();
		SearchScreen.childAgeDropDown(1).check(matches(ViewMatchers.withContentDescription("Select age for child 1. 10 years old selected.")));
		SearchScreen.incrementChildrenButton();
		SearchScreen.childAgeDropDown(2).check(matches(ViewMatchers.withContentDescription("Select age for child 2. 10 years old selected.")));
		SearchScreen.incrementChildrenButton();
		SearchScreen.childAgeDropDown(3).check(matches(ViewMatchers.withContentDescription("Select age for child 3. 10 years old selected.")));
		SearchScreen.incrementChildrenButton();
		SearchScreen.childAgeDropDown(4).check(matches(ViewMatchers.withContentDescription("Select age for child 4. 10 years old selected.")));
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
