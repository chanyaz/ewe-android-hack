package com.expedia.bookings.test.happy;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.RailTestCase;
import com.expedia.bookings.test.phone.packages.RailScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class RailPhoneHappyPathTest extends RailTestCase {

	public void testRailPhoneHappyPath() throws Throwable {

		RailScreen.calendarButton().perform(click());
		LocalDate firstStartDate = LocalDate.now().plusDays(10);
		RailScreen.selectDates(firstStartDate, firstStartDate.plusDays(2));
		RailScreen.dialogDoneButton().perform(click());

		RailScreen.searchButton().perform(click());
		Common.delay(1);

		onView(withText("12:33 PM – 5:09 PM")).check(matches(isDisplayed())).perform(click());
		onView(withText("Take the Ferry from Portsmouth Harbour to Ryde Pier Head")).check(matches(isDisplayed()));

		Common.delay(1);
		onView(allOf(withId(R.id.details_fare_options))).perform(scrollTo());
		onView(withText("Specified train only")).check(matches(isDisplayed()));
	}
}
