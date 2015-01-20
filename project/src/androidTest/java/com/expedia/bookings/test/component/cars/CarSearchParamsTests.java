package com.expedia.bookings.test.component.cars;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.matcher.ViewMatchers.Visibility;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.utils.JodaUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public final class CarSearchParamsTests {
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.widget_car_search_params);

	@Test
	public void testSelectingPickupTime() {
		CarSearchParamsModel.calendar().check(matches(withEffectiveVisibility(Visibility.INVISIBLE)));
		CarSearchParamsModel.selectDate().perform(click());
		CarSearchParamsModel.calendar().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

		CarSearchParamsModel.changeTime().check(matches(withEffectiveVisibility(Visibility.INVISIBLE)));
		CarSearchParamsModel.selectDates(LocalDate.now(), null);
		CarSearchParamsModel.changeTime().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
	}

	@Test
	public void testTimePicker() {
		CarSearchParamsModel.selectDate().perform(click());
		CarSearchParamsModel.changeTime().check(matches(withEffectiveVisibility(Visibility.INVISIBLE)));
		CarSearchParamsModel.selectDates(LocalDate.now(), null);
		CarSearchParamsModel.changeTime().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
		CarSearchParamsModel.timeContainer().check(matches(withEffectiveVisibility(Visibility.INVISIBLE)));
		CarSearchParamsModel.changeTime().perform(click());
		CarSearchParamsModel.timeContainer().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
		CarSearchParamsModel.timeConfirm().perform(click());
		CarSearchParamsModel.timeContainer().check(matches(withEffectiveVisibility(Visibility.INVISIBLE)));
		CarSearchParamsModel.calendarContainer().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
	}

	@Test
	public void testViewPopulatesDb() {
		final DateTime expectedStartDate = DateTime.now().withTimeAtStartOfDay();
		final DateTime expectedEndDate = expectedStartDate.plusDays(3);
		final String expectedPickupLocation = "SFO";

		CarSearchParamsModel.selectDate().perform(click());
		CarSearchParamsModel.selectDates(expectedStartDate.toLocalDate(), expectedEndDate.toLocalDate());
		CarSearchParamsModel.pickupLocation().perform(typeText(expectedPickupLocation));

		assertNull(CarDb.searchParams.origin);
		assertNull(CarDb.searchParams.startTime);
		assertNull(CarDb.searchParams.endTime);

		CarSearchParamsModel.searchButton().perform(click());

		assertEquals(expectedStartDate, CarDb.searchParams.startTime);
		assertEquals(expectedEndDate, CarDb.searchParams.endTime);
		assertEquals(expectedPickupLocation, CarDb.searchParams.origin);
	}

	@Test
	public void testDateButtonTextPopulation() {
		CarSearchParamsModel.selectDate().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));
		CarSearchParamsModel.selectDate().perform(click());
		// Select first date
		CarSearchParamsModel.selectDates(LocalDate.now(), null);
		String today = JodaUtils.format(DateTime.now().withHourOfDay(0).withMinuteOfHour(0), "MMM dd, hh:mm a");
		CarSearchParamsModel.selectDate().check(matches(withText(today + " – Select return date")));
		// Select round-trip, overnight
		CarSearchParamsModel.selectDates(LocalDate.now(), LocalDate.now().plusDays(1));
		String expected = JodaUtils.format(DateTime.now().withHourOfDay(0).withMinuteOfHour(0), "MMM dd, hh:mm a")
			+ " – " + JodaUtils.format(DateTime.now().plusDays(1).withHourOfDay(0).withMinuteOfHour(0), "MMM dd, hh:mm a");
		CarSearchParamsModel.selectDate().check(matches(withText(expected)));
	}
}
