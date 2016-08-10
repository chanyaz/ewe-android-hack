package com.expedia.bookings.test.phone.rail;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.RailTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class RailSearchPresenterTest extends RailTestCase {

	public void testOneWaySearch() {
		RailScreen.selectOneWay();
		// Search button clicked without any input params
		SearchScreen.searchButton().perform(click());
		SearchScreen.searchButton().check(matches(isDisplayed()));

		RailScreen.calendarButton().perform(click());
		LocalDate startDate = LocalDate.now().plusDays(3);
		RailScreen.selectDates(startDate, null);

		EspressoUtils.assertViewIsDisplayed(R.id.depart_slider_container);
		EspressoUtils.assertViewIsNotDisplayed(R.id.return_slider_container);
		RailScreen.dialogDoneButton().perform(click());
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedStartDate + " (One Way)");
		SearchScreen.searchButton().perform(click());
		Common.delay(1);
		onView(withText("3:55 PM – 7:22 PM")).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));
	}

	public void testRoundTripSearch() {
		RailScreen.selectRoundTrip();
		// Search button clicked without any input params
		SearchScreen.searchButton().perform(click());
		SearchScreen.searchButton().check(matches(isDisplayed()));

		RailScreen.calendarButton().perform(click());
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = startDate.plusDays(1);
		RailScreen.selectDates(startDate, endDate);

		EspressoUtils.assertViewIsDisplayed(R.id.depart_slider_container);
		EspressoUtils.assertViewIsDisplayed(R.id.return_slider_container);
		RailScreen.dialogDoneButton().perform(click());
		String expectedStartDate = DateUtils.localDateToMMMd(startDate);
		String expectedEndDate = DateUtils.localDateToMMMd(endDate);
		EspressoUtils.assertViewWithTextIsDisplayed(expectedStartDate + " - " + expectedEndDate);
		SearchScreen.searchButton().perform(click());
		onView(withText("3:55 PM – 7:22 PM")).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));
	}
}
