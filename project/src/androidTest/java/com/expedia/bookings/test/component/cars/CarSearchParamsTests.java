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
import com.expedia.bookings.utils.JodaUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public final class CarSearchParamsTests {
	private static final String DATE_TIME_PATTERN = "MMM dd, h:mm a";
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.widget_car_search_params);

	@Test
	public void testViewPopulatesDb() {
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
}
