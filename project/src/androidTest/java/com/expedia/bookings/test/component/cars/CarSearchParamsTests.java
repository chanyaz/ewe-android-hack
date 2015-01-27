package com.expedia.bookings.test.component.cars;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.test.ui.espresso.ViewActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.SpoonScreenshotUtils;
import com.expedia.bookings.utils.JodaUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public final class CarSearchParamsTests {
	private static final String DATE_TIME_PATTERN = "MMM dd, h:mm a";
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.widget_car_search_params);

	@Test
	public void testViewPopulatesDb() {
		CarDb.searchParams.origin = null;
		CarDb.searchParams.startDateTime = null;
		CarDb.searchParams.endDateTime = null;

		final DateTime expectedStartDate = DateTime.now().withTimeAtStartOfDay();
		final DateTime expectedEndDate = expectedStartDate.plusDays(3);
		final String expectedPickupLocation = "SFO";

		CarSearchParamsModel.selectDate().perform(click());
		CarSearchParamsModel.selectDates(expectedStartDate.toLocalDate(), expectedEndDate.toLocalDate());
		CarSearchParamsModel.pickupLocation().perform(typeText(expectedPickupLocation));

		assertNull(CarDb.searchParams.origin);
		assertNull(CarDb.searchParams.startDateTime);
		assertNull(CarDb.searchParams.endDateTime);

		CarSearchParamsModel.searchButton().perform(click());

		assertEquals(expectedStartDate, CarDb.searchParams.startDateTime);
		assertEquals(expectedEndDate, CarDb.searchParams.endDateTime);
		assertEquals(expectedPickupLocation, CarDb.searchParams.origin);
	}

	@Test
	public void testDateButtonTextPopulation() {
		CarSearchParamsModel.selectDate().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));
		CarSearchParamsModel.selectDate().perform(click());
		// Select first date
		CarSearchParamsModel.selectDates(LocalDate.now(), null);
		String today = JodaUtils.format(DateTime.now().withHourOfDay(0).withMinuteOfHour(0), DATE_TIME_PATTERN);
		CarSearchParamsModel.selectDate().check(matches(withText(today + " – Select return date")));
		// Select round-trip, overnight
		CarSearchParamsModel.selectDates(LocalDate.now(), LocalDate.now().plusDays(1));
		String expected = JodaUtils.format(DateTime.now().withHourOfDay(0).withMinuteOfHour(0), DATE_TIME_PATTERN)
			+ " – " + JodaUtils.format(DateTime.now().plusDays(1).withHourOfDay(0).withMinuteOfHour(0), DATE_TIME_PATTERN);
		CarSearchParamsModel.selectDate().check(matches(withText(expected)));
	}

	@Test
	public void testSelectTimeBeforeDates() {
		// 24 == 12:00 PM
		int noonProgress = 24;
		// 26 == 01:00 PM
		int onePmProgress = 26;
		CarSearchParamsModel.selectDate().perform(click());
		CarSearchParamsModel.pickUpTimeBar().perform(ViewActions.setSeekbarTo(noonProgress));
		CarSearchParamsModel.dropOffTimeBar().perform(ViewActions.setSeekbarTo(onePmProgress));
		CarSearchParamsModel.selectDate().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));

		//Select dates from calendar
		CarSearchParamsModel.selectDates(LocalDate.now(), LocalDate.now().plusDays(1));
		int minutesToMillis = 30 * 60 * 1000;
		String expected = JodaUtils.format(DateTime.now().withTimeAtStartOfDay().plusMillis(noonProgress * minutesToMillis), DATE_TIME_PATTERN)
			+ " – " + JodaUtils.format(DateTime.now().plusDays(1).withTimeAtStartOfDay().plusMillis(onePmProgress * minutesToMillis), DATE_TIME_PATTERN);
		CarSearchParamsModel.selectDate().check(matches(withText(expected)));
	}

	@Test
	public void testSelectingOnlyPickupDateClearsDropoffDate() {
		CarSearchParamsModel.selectDate().perform(click());
		CarSearchParamsModel.selectDates(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4));
		String expected = JodaUtils.format(DateTime.now().plusDays(3).withTimeAtStartOfDay(), DATE_TIME_PATTERN)
			+ " – " + JodaUtils.format(DateTime.now().plusDays(4).withTimeAtStartOfDay(), DATE_TIME_PATTERN);
		CarSearchParamsModel.selectDate().check(matches(withText(expected)));

		CarSearchParamsModel.selectDates(LocalDate.now().plusDays(2), null);
		String expected2 = JodaUtils.format(DateTime.now().plusDays(2).withTimeAtStartOfDay(), DATE_TIME_PATTERN)
			+ " – Select return date";
		CarSearchParamsModel.selectDate().check(matches(withText(expected2)));
	}

	@Test
	public void testSearchButtonErrorMessageForIncompleteParams() throws Throwable {
		// Test with all params missing
		CarSearchParamsModel.searchButton().perform(click());
		CarSearchParamsModel.alertDialog().check(matches(isDisplayed()));
		CarSearchParamsModel.alertDialogMessage().check(matches(withText(R.string.error_missing_origin_param)));
		CarSearchParamsModel.alertDialogNeutralButton().check(matches(isDisplayed()));
		CarSearchParamsModel.alertDialogNeutralButton().perform(click());

		// Test with only start date selected
		CarSearchParamsModel.pickupLocation().perform(clearText());
		CarSearchParamsModel.selectDate().perform(click());
		CarSearchParamsModel.selectDates(LocalDate.now().plusDays(3), null);
		CarSearchParamsModel.searchButton().perform(click());
		CarSearchParamsModel.alertDialog().check(matches(isDisplayed()));
		CarSearchParamsModel.alertDialogMessage().check(matches(withText(R.string.error_missing_origin_param)));
		CarSearchParamsModel.alertDialogNeutralButton().check(matches(isDisplayed()));
		CarSearchParamsModel.alertDialogNeutralButton().perform(click());

		//Test with only start and end date selected
		CarSearchParamsModel.pickupLocation().perform(clearText());
		CarSearchParamsModel.selectDates(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4));
		CarSearchParamsModel.searchButton().perform(click());
		CarSearchParamsModel.alertDialog().check(matches(isDisplayed()));
		CarSearchParamsModel.alertDialogMessage().check(matches(withText(R.string.error_missing_origin_param)));
		CarSearchParamsModel.alertDialogNeutralButton().check(matches(isDisplayed()));
		CarSearchParamsModel.alertDialogNeutralButton().perform(click());

		// Test with only pickup location
		CarSearchParamsModel.pickupLocation().perform(typeText("SFO"));
		//TODO don't do this
		ScreenActions.delay(3);
		onView(withText("SFO"))
			.inRoot(withDecorView(not(is(SpoonScreenshotUtils.getCurrentActivity(playground.instrumentation()).getWindow().getDecorView()))))
			.perform(click());
		CarSearchParamsModel.selectDates(null, null);
		CarSearchParamsModel.searchButton().perform(click());
		CarSearchParamsModel.alertDialog().check(matches(isDisplayed()));
		CarSearchParamsModel.alertDialogMessage().check(matches(withText(R.string.error_missing_start_date_param)));
		CarSearchParamsModel.alertDialogNeutralButton().check(matches(isDisplayed()));
		CarSearchParamsModel.alertDialogNeutralButton().perform(click());

		// Test with origin and start date selected
		CarSearchParamsModel.pickupLocation().perform(typeText("SFO"));
		CarSearchParamsModel.selectDate().perform(click());
		CarSearchParamsModel.selectDates(LocalDate.now().plusDays(3), null);
		CarSearchParamsModel.searchButton().perform(click());
		CarSearchParamsModel.alertDialog().check(matches(isDisplayed()));
		CarSearchParamsModel.alertDialogMessage().check(matches(withText(R.string.error_missing_end_date_param)));
		CarSearchParamsModel.alertDialogNeutralButton().check(matches(isDisplayed()));
	}

	@Test
	public void testSearchButtonHasNoErrorMessageForCompleteParams() {
		CarSearchParamsModel.pickupLocation().perform(typeText("SFO"));
		CarSearchParamsModel.selectDate().perform(click());
		CarSearchParamsModel.selectDates(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4));
		CarSearchParamsModel.searchButton().perform(click());
		CarSearchParamsModel.alertDialog().check(doesNotExist());
	}

	@Test
	public void testDialogShownOnDropOffClick() {
		CarSearchParamsModel.dropOffLocation().perform(click());
		CarSearchParamsModel.alertDialog().check(matches(isDisplayed()));
		CarSearchParamsModel.alertDialogMessage().check(matches(withText(R.string.drop_off_same_as_pick_up)));
		CarSearchParamsModel.alertDialogNeutralButton().check(matches(isDisplayed()));
	}
}
