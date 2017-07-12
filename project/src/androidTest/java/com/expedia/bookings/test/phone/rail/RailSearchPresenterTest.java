package com.expedia.bookings.test.phone.rail;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.RailTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.rail.RailScreen;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class RailSearchPresenterTest extends RailTestCase {

	@Test
	public void testOneWaySearch() throws Throwable {
		RailScreen.selectOneWay();
		// Search button clicked without any input params
		SearchScreen.searchButton().perform(click());
		SearchScreen.searchButton().check(matches(isDisplayed()));

		SearchScreen.selectRailOriginAndDestination();
		RailScreen.calendarButton().perform(click());

		DateTime startDateTime = DateTime.now().plusDays(3).withTimeAtStartOfDay();
		LocalDate startDate = startDateTime.toLocalDate();
		RailScreen.selectDates(startDate, null);

		EspressoUtils.assertViewIsDisplayed(R.id.depart_slider_container);
		EspressoUtils.assertViewIsNotDisplayed(R.id.return_slider_container);
		RailScreen.dialogDoneButton().perform(click());
		String expectedStartDateTime = DateUtils.dateTimeToMMMdhmma(startDateTime);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedStartDateTime);
		SearchScreen.searchButton().perform(click());
		Common.delay(1);
		onView(withId(R.id.rail_outbound_list)).perform(scrollToPosition(5));
		onView(withText("12:55 PM – 4:16 PM")).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));
	}

// Disabled on April 28, 2017 for repeated flakiness - ScottW
//	public void testRoundTripSearch() throws Throwable {
//		RailScreen.selectRoundTrip();
//		// Search button clicked without any input params
//		SearchScreen.searchButton().perform(click());
//		SearchScreen.searchButton().check(matches(isDisplayed()));
//
//		SearchScreen.selectRailOriginAndDestination();
//		RailScreen.calendarButton().perform(click());
//
//		DateTime startDateTime = DateTime.now().plusDays(3).withTimeAtStartOfDay();
//		LocalDate startDate = startDateTime.toLocalDate();
//		String expectedStartDateTime = DateUtils.dateTimeToMMMdhmma(startDateTime);
//
//		DateTime endDateTime = startDateTime.plusDays(1).withTimeAtStartOfDay();
//		LocalDate endDate = endDateTime.toLocalDate();
//		String expectedEndDateTime = DateUtils.dateTimeToMMMdhmma(endDateTime);
//		RailScreen.selectDates(startDate, endDate);
//
//		EspressoUtils.assertViewIsDisplayed(R.id.depart_slider_container);
//		EspressoUtils.assertViewIsDisplayed(R.id.return_slider_container);
//		RailScreen.dialogDoneButton().perform(click());
//
//		EspressoUtils.assertViewWithTextIsDisplayed(expectedStartDateTime + " – " + expectedEndDateTime);
//		SearchScreen.searchButton().perform(click());
//
//		onView(withText(R.string.select_outbound)).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));
//	}
}
