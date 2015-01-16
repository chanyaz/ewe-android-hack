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

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static junit.framework.Assert.assertNull;

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
		CarSearchParamsModel.selectDates(LocalDate.now(), null);
		CarSearchParamsModel.timeContainer().check(matches(withEffectiveVisibility(Visibility.INVISIBLE)));
		CarSearchParamsModel.changeTime().perform(click());
		CarSearchParamsModel.timeContainer().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
		CarSearchParamsModel.timeConfirm().perform(click());
		CarSearchParamsModel.timeContainer().check(matches(withEffectiveVisibility(Visibility.INVISIBLE)));
		CarSearchParamsModel.calendarContainer().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
	}

	@Test
	public void testCalendarViewDataPopulation() {
		final DateTime expectedStartDate = DateTime.now().withTimeAtStartOfDay();
		final DateTime expectedEndDate = expectedStartDate.plusDays(3);

		CarSearchParamsModel.selectDate().perform(click());
		CarSearchParamsModel.selectDates(expectedStartDate.toLocalDate(), null);
		CarSearchParamsModel.selectDates(expectedEndDate.toLocalDate(), null);

		assertNull(CarDb.searchParams.startTime);
		assertNull(CarDb.searchParams.endTime);

		CarSearchParamsModel.searchButton().perform(click());

		// TODO make data entry work again!
//		assertEquals(expectedStartDate, CarDb.searchParams.startTime);
//		assertEquals(expectedEndDate, CarDb.searchParams.endTime);
	}
}
