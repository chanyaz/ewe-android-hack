package com.expedia.bookings.test.happy;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.RailTestCase;
import com.expedia.bookings.test.phone.packages.RailScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class RailPhoneHappyPathTest extends RailTestCase {

	public void testRailPhoneHappyPath() throws Throwable {

		RailScreen.calendarButton().perform(click());
		LocalDate firstStartDate = LocalDate.now().plusDays(10);
		RailScreen.selectDates(firstStartDate, firstStartDate.plusDays(2));
		RailScreen.dialogDoneButton().perform(click());

		RailScreen.searchButton().perform(click());
		Common.delay(1);

		onView(withText("9:45 AM – 12:59 PM")).check(matches(isDisplayed())).perform(click());
		onView(withText("Change at Birmingham New Street")).check(matches(isDisplayed()));

		RailScreen.scrollToFareOptions();
		onView(withText("Any off-peak train")).check(matches(isDisplayed()));
		Common.delay(1);
		RailScreen.clickSelectFareOption();

		onView(withText("Outbound - Thu Jun 09")).check(matches(isDisplayed()));
		assertLegInfo();
		assertDetailsCollapsed();
		assertDetailsExpanded();

		//assert info container
		RailScreen.fareDesciptionInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Travel anytime of day")))));

		RailScreen.checkout().perform(click());
	}

	private void assertLegInfo() {
		RailScreen.legInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("9:45 AM – 12:59 PM")))));
		RailScreen.legInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("3h 22m, 1 Change")))));
		RailScreen.legInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("CrossCountry, CrossCountry")))));

	}

	private void assertDetailsExpanded() {
		RailScreen.detailsIcon().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.rail_leg_details);
		RailScreen.detailsIcon().perform(click());
	}

	private void assertDetailsCollapsed() {
		EspressoUtils.assertViewIsNotDisplayed(R.id.rail_leg_details);
	}
}
