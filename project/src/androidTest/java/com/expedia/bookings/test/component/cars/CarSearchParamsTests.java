package com.expedia.bookings.test.component.cars;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchParamsBuilder;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.test.ui.espresso.ViewActions;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.widget.CarSearchParamsWidget;

import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public final class CarSearchParamsTests {
	private static final String DATE_TIME_PATTERN = "MMM d, h:mm a";
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.widget_car_search_params);

	@Test
	public void testViewPopulatesSearchParams() {
		final DateTime expectedStartDate = DateTime.now().withTimeAtStartOfDay();

		CarSearchParamsWidget widget = (CarSearchParamsWidget) playground.getRoot();
		CarSearchParams actual;
		CarSearchParams expected = new CarSearchParamsBuilder()
			.startDate(expectedStartDate.toLocalDate())
			.endDate(expectedStartDate.plusDays(3).toLocalDate())
			.origin("SFO")
			.build();

		actual = widget.getCurrentParams();
		assertNull(actual.origin);
		assertNull(actual.startDateTime);
		assertNull(actual.endDateTime);

		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(expected.startDateTime.toLocalDate(), expected.endDateTime.toLocalDate());
		CarViewModel.pickupLocation().perform(typeText(expected.origin));

		CarViewModel.searchButton().perform(click());

		actual = widget.getCurrentParams();
		assertEquals(expected.origin, actual.origin);
		assertEquals(expected.startDateTime, actual.startDateTime);
		assertEquals(expected.endDateTime, actual.endDateTime);
	}

	@Test
	public void testDateButtonTextPopulation() {
		CarViewModel.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));
		CarViewModel.selectDateButton().perform(click());
		// Select first date
		CarViewModel.selectDates(LocalDate.now(), null);
		String today = JodaUtils.format(DateTime.now().withHourOfDay(0).withMinuteOfHour(0), DATE_TIME_PATTERN);
		CarViewModel.selectDateButton().check(matches(withText(today + " – Select return date")));
		// Select round-trip, overnight
		CarViewModel.selectDates(LocalDate.now(), LocalDate.now().plusDays(1));
		String expected = JodaUtils.format(DateTime.now().withHourOfDay(0).withMinuteOfHour(0), DATE_TIME_PATTERN)
			+ " – " + JodaUtils
			.format(DateTime.now().plusDays(1).withHourOfDay(0).withMinuteOfHour(0), DATE_TIME_PATTERN);
		CarViewModel.selectDateButton().check(matches(withText(expected)));
	}

	@Test
	public void testSelectTimeBeforeDates() {
		// 24 == 12:00 PM
		int noonProgress = 24;
		// 26 == 01:00 PM
		int onePmProgress = 26;
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.pickUpTimeBar().perform(ViewActions.setSeekbarTo(noonProgress));
		CarViewModel.dropOffTimeBar().perform(ViewActions.setSeekbarTo(onePmProgress));
		CarViewModel.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));

		//Select dates from calendar
		CarViewModel.selectDates(LocalDate.now(), LocalDate.now().plusDays(1));
		int minutesToMillis = 30 * 60 * 1000;
		String expected = JodaUtils
			.format(DateTime.now().withTimeAtStartOfDay().plusMillis(noonProgress * minutesToMillis), DATE_TIME_PATTERN)
			+ " – " + JodaUtils
			.format(DateTime.now().plusDays(1).withTimeAtStartOfDay().plusMillis(onePmProgress * minutesToMillis),
				DATE_TIME_PATTERN);
		CarViewModel.selectDateButton().check(matches(withText(expected)));
	}

	@Test
	public void testSelectingOnlyPickupDateClearsDropoffDate() {
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4));
		String expected = JodaUtils.format(DateTime.now().plusDays(3).withTimeAtStartOfDay(), DATE_TIME_PATTERN)
			+ " – " + JodaUtils.format(DateTime.now().plusDays(4).withTimeAtStartOfDay(), DATE_TIME_PATTERN);
		CarViewModel.selectDateButton().check(matches(withText(expected)));

		CarViewModel.selectDates(LocalDate.now().plusDays(2), null);
		String expected2 = JodaUtils.format(DateTime.now().plusDays(2).withTimeAtStartOfDay(), DATE_TIME_PATTERN)
			+ " – Select return date";
		CarViewModel.selectDateButton().check(matches(withText(expected2)));
	}

	@Test
	public void testSearchButtonErrorMessageForIncompleteParams() throws Throwable {
		// Test with all params missing
		CarViewModel.searchButton().perform(click());
		CarViewModel.alertDialog().check(matches(isDisplayed()));
		CarViewModel.alertDialogMessage().check(matches(withText(R.string.error_missing_origin_param)));
		CarViewModel.alertDialogNeutralButton().check(matches(isDisplayed()));
		CarViewModel.alertDialogNeutralButton().perform(click());

		// Test with only start date selected
		CarViewModel.pickupLocation().perform(clearText());
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(LocalDate.now().plusDays(3), null);
		CarViewModel.searchButton().perform(click());
		CarViewModel.alertDialog().check(matches(isDisplayed()));
		CarViewModel.alertDialogMessage().check(matches(withText(R.string.error_missing_origin_param)));
		CarViewModel.alertDialogNeutralButton().check(matches(isDisplayed()));
		CarViewModel.alertDialogNeutralButton().perform(click());

		//Test with only start and end date selected
		CarViewModel.pickupLocation().perform(clearText());
		CarViewModel.selectDates(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4));
		CarViewModel.searchButton().perform(click());
		CarViewModel.alertDialog().check(matches(isDisplayed()));
		CarViewModel.alertDialogMessage().check(matches(withText(R.string.error_missing_origin_param)));
		CarViewModel.alertDialogNeutralButton().check(matches(isDisplayed()));
		CarViewModel.alertDialogNeutralButton().perform(click());

		// Test with only pickup location
		CarViewModel.selectPickupLocation(playground.instrumentation(), "SFO");
		CarViewModel.selectDates(null, null);
		CarViewModel.searchButton().perform(click());
		CarViewModel.alertDialog().check(matches(isDisplayed()));
		CarViewModel.alertDialogMessage().check(matches(withText(R.string.error_missing_start_date_param)));
		CarViewModel.alertDialogNeutralButton().check(matches(isDisplayed()));
		CarViewModel.alertDialogNeutralButton().perform(click());

		// Test with origin and start date selected
		CarViewModel.selectPickupLocation(playground.instrumentation(), "SFO");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(LocalDate.now().plusDays(3), null);
		CarViewModel.searchButton().perform(click());
		CarViewModel.alertDialog().check(matches(isDisplayed()));
		CarViewModel.alertDialogMessage().check(matches(withText(R.string.error_missing_end_date_param)));
		CarViewModel.alertDialogNeutralButton().check(matches(isDisplayed()));
	}

	@Test
	public void testSearchButtonHasNoErrorMessageForCompleteParams() throws Throwable {
		CarViewModel.selectPickupLocation(playground.instrumentation(), "SFO");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4));
		CarViewModel.searchButton().perform(click());
		CarViewModel.alertDialog().check(doesNotExist());
	}

	@Test
	public void testDialogShownOnDropOffClick() {
		CarViewModel.dropOffLocation().perform(click());
		CarViewModel.alertDialog().check(matches(isDisplayed()));
		CarViewModel.alertDialogMessage().check(matches(withText(R.string.drop_off_same_as_pick_up)));
		CarViewModel.alertDialogNeutralButton().check(matches(isDisplayed()));
	}

}
