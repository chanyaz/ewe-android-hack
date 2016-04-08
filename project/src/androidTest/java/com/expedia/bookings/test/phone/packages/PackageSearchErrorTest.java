package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class PackageSearchErrorTest extends PackageTestCase {

	public void testSearchError() throws Throwable {
		PackageScreen.destination().perform(click());
		PackageScreen.searchEditText().perform(typeText("GGW"));
		Common.closeSoftKeyboard(PackageScreen.destination());
		PackageScreen.selectLocation("Glasgow, MT (GGW-Glasgow Intl.)");
		PackageScreen.arrival().perform(click());
		PackageScreen.searchEditText().perform(typeText("DTW"));
		PackageScreen.selectLocation("Detroit, MI (DTW-Detroit Metropolitan Wayne County)");

		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		PackageScreen.selectDates(startDate, endDate);
		PackageScreen.searchButton().perform(click());

		PackageScreen.assertErrorScreen("Edit Search", "We were unable to find any results. Please adjust your search.");
		onView(withId(R.id.error_action_button)).perform(click());
		PackageScreen.searchButton().check(matches(isDisplayed()));
	}

}
