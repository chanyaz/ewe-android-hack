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
import com.expedia.bookings.presenter.CarSearchPresenter;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.test.ui.espresso.ViewActions;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.JodaUtils;

import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public final class CarSearchPresenterTests {
	private static final String DATE_TIME_PATTERN = "MMM d, h:mm a";

	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.widget_car_search_params);
	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	@Test
	public void testViewPopulatesSearchParams() throws Throwable {
		DateTime expectedStartDate = DateTime.now().withTimeAtStartOfDay();

		CarSearchPresenter widget = (CarSearchPresenter) playground.getRoot();
		CarSearchParams actual;
		CarSearchParamsBuilder.DateTimeBuilder dateTimeBuilder =
			new CarSearchParamsBuilder.DateTimeBuilder()
				.startDate(expectedStartDate.toLocalDate())
				.endDate(expectedStartDate.plusDays(3).toLocalDate());

		CarSearchParams expected = new CarSearchParamsBuilder()
			.dateTimeBuilder(dateTimeBuilder)
			.origin("SFO")
			.build();

		actual = widget.getCurrentParams();
		assertNull(actual.origin);
		assertNull(actual.startDateTime);
		assertNull(actual.endDateTime);

		CarViewModel.selectAirport(playground.instrumentation(), "SFO", "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(expected.startDateTime.toLocalDate(), expected.endDateTime.toLocalDate());
		CarViewModel.searchButton().perform(click());

		actual = widget.getCurrentParams();
		assertEquals(expected.origin, actual.origin);
		assertEquals(expected.startDateTime, actual.startDateTime.withTimeAtStartOfDay());
		assertEquals(expected.endDateTime, actual.endDateTime.withTimeAtStartOfDay());
	}

	@Test
	public void testDateButtonTextPopulation() throws Throwable {
		// Open calendar
		CarViewModel.selectAirport(playground.instrumentation(), "SFO", "San Francisco, CA");
		CarViewModel.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));
		CarViewModel.selectDateButton().perform(click());

		final DateTime tomorrow = DateTime.now().plusDays(1);
		final DateTime tomorrowsTomorrow = tomorrow.plusDays(1);
		String tomorrowStr = generateDefaultStartDateTimeStrForDateTimeButton(tomorrow);
		String tomorrowsTomorrowStr = generateDefaultEndDateTimeStrForDateTimeButton(tomorrowsTomorrow);

		// Select start date
		String expectedText = tomorrowStr + " – Select return date";
		CarViewModel.selectDates(tomorrow.toLocalDate(), null);
		CarViewModel.selectDateButton().check(matches(withText(expectedText)));

		// Select end date
		CarViewModel.selectDates(tomorrow.toLocalDate(), tomorrowsTomorrow.toLocalDate());
		String expected = tomorrowStr + " – " + tomorrowsTomorrowStr;
		CarViewModel.selectDateButton().check(matches(withText(expected)));
	}

	private static String generateDefaultStartDateTimeStrForDateTimeButton(DateTime dateTime) {
		return JodaUtils.format(dateTime
				.withTimeAtStartOfDay()
				.plusHours(9)
				.withMinuteOfHour(0),
			DATE_TIME_PATTERN);
	}

	private static String generateDefaultEndDateTimeStrForDateTimeButton(DateTime dateTime) {
		return JodaUtils.format(dateTime
				.withTimeAtStartOfDay()
				.plusHours(18)
				.withMinuteOfHour(0),
			DATE_TIME_PATTERN);
	}

	@Test
	public void testSelectTimeBeforeDates() throws Throwable {
		// 24 == 12:00 PM
		int noonProgress = 24;
		// 26 == 01:00 PM
		int onePmProgress = 26;
		CarViewModel.selectAirport(playground.instrumentation(), "SFO", "San Francisco, CA");
		CarViewModel.pickupLocation().perform(clearText());
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.pickUpTimeBar().perform(ViewActions.setSeekbarTo(noonProgress));
		CarViewModel.dropOffTimeBar().perform(ViewActions.setSeekbarTo(onePmProgress));
		CarViewModel.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));

		//Select dates from calendar
		final DateTime tomorrow = DateTime.now().plusDays(1);
		final DateTime tomorrowsTomorrow = tomorrow.plusDays(1);
		CarViewModel.selectDates(tomorrow.toLocalDate(), tomorrowsTomorrow.toLocalDate());
		int minutesToMillis = 30 * 60 * 1000;
		String expected = DateFormatUtils.formatDateTimeRange(playground.get(),
		tomorrow.withTimeAtStartOfDay().plusMillis(noonProgress * minutesToMillis),
		tomorrowsTomorrow.withTimeAtStartOfDay().plusMillis(onePmProgress * minutesToMillis),
		DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT);
		CarViewModel.selectDateButton().check(matches(withText(expected)));
	}

	// FIXME
	@Test
	public void testSelectingOnlyPickupDateClearsDropoffDate() throws Throwable {
		CarViewModel.selectAirport(playground.instrumentation(), "SFO", "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		final DateTime twoDaysOut = DateTime.now().plusDays(3);
		final DateTime threeDaysOut = DateTime.now().plusDays(3);
		final DateTime fourDaysOut = threeDaysOut.plusDays(1);
		CarViewModel.selectDates(threeDaysOut.toLocalDate(), fourDaysOut.toLocalDate());
		String expected = generateDefaultStartDateTimeStrForDateTimeButton(threeDaysOut) +
			" – " + generateDefaultEndDateTimeStrForDateTimeButton(fourDaysOut);
		CarViewModel.selectDateButton().check(matches(withText(expected)));

		CarViewModel.selectDates(twoDaysOut.toLocalDate(), null);
		String expected2 = generateDefaultStartDateTimeStrForDateTimeButton(twoDaysOut) + " – Select return date";
		CarViewModel.selectDateButton().check(matches(withText(expected2)));
	}

	@Test
	public void testSearchButtonErrorMessageForIncompleteParams() throws Throwable {
		// Test with all params missing
		CarViewModel.searchButton().perform(click());
		CarViewModel.didNotGoToResults();

		// Test with only pickup location
		CarViewModel.selectAirport(playground.instrumentation(), "SFO", "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.searchButton().perform(click());
		CarViewModel.didNotGoToResults();

		// Test with only start date selected
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(LocalDate.now().plusDays(3), null);
		CarViewModel.searchButton().perform(click());
		CarViewModel.didNotGoToResults();

		// Test with origin and start date selected
		CarViewModel.selectAirport(playground.instrumentation(), "SFO", "San Francisco, CA");
		CarViewModel.searchButton().perform(click());
		CarViewModel.didNotGoToResults();

		//Test with invalid airport code we dont show calendar
		CarViewModel.pickupLocation().perform(typeText("AAAA"));
		CarViewModel.searchButton().perform(click());
		CarViewModel.didNotshowCalendar();
	}

	@Test
	public void testSearchOnlyStartEndDateSelected() throws Throwable {
		//Test with only start and end date selected
		CarViewModel.dropOffLocation().perform(click());
		CarViewModel.alertDialogPositiveButton().perform(click());
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.didNotGoToResults();
		EspressoUtils.assertViewIsDisplayed(R.id.search_container);
	}

	@Test
	public void testSearchButtonHasNoErrorMessageForCompleteParams() throws Throwable {
		CarViewModel.selectAirport(playground.instrumentation(), "SFO", "San Francisco, CA");
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
		CarViewModel.alertDialogPositiveButton().check(matches(isDisplayed()));
	}

	@Test
	public void testStartTimeBeforeCurrentTime() throws Throwable {
		final DateTime today = DateTime.now();
		int currentTime = ((today.getHourOfDay() + 1) * 2) + (today.getMinuteOfHour() > 30 ? 1 : 0);
		int startTime = today.minusHours(2).getHourOfDay() * 2;

		int ninePmProgress = 42;
		CarViewModel.selectAirport(playground.instrumentation(), "SFO", "San Francisco, CA");
		CarViewModel.pickupLocation().perform(clearText());
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.pickUpTimeBar().perform(ViewActions.setSeekbarTo(startTime));
		CarViewModel.dropOffTimeBar().perform(ViewActions.setSeekbarTo(ninePmProgress));
		CarViewModel.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));

		//Select dates from calendar
		final DateTime tomorrow = today.plusDays(1);
		CarViewModel.selectDates(today.toLocalDate(), tomorrow.toLocalDate());
		int minutesToMillis = 30 * 60 * 1000;
		String expected =  DateFormatUtils.formatDateTimeRange(playground.get(),
			today.withTimeAtStartOfDay().plusMillis(currentTime * minutesToMillis),
			tomorrow.withTimeAtStartOfDay().plusMillis(ninePmProgress * minutesToMillis),
			DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT);
		CarViewModel.selectDateButton().check(matches(withText(expected)));
	}

	@Test
	public void testEndTimeBeforeStartTimeSameDay() throws Throwable {
		// 28 == 02:00 PM
		int twoPmProgress = 28;
		// 42 == 09:00 PM
		int ninePmProgress = 42;
		CarViewModel.selectAirport(playground.instrumentation(), "SFO", "San Francisco, CA");
		CarViewModel.pickupLocation().perform(clearText());
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.pickUpTimeBar().perform(ViewActions.setSeekbarTo(ninePmProgress));
		CarViewModel.dropOffTimeBar().perform(ViewActions.setSeekbarTo(twoPmProgress));
		CarViewModel.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));

		//Select dates from calendar
		final DateTime date = DateTime.now().plusDays(1);
		CarViewModel.selectDates(date.toLocalDate(), date.toLocalDate());
		int minutesToMillis = 30 * 60 * 1000;
		String expected = DateFormatUtils.formatDateTimeRange(playground.get(),
			date.withTimeAtStartOfDay().plusMillis(ninePmProgress * minutesToMillis),
			date.withTimeAtStartOfDay().plusMillis((ninePmProgress + 4) * minutesToMillis),
			DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT);
		CarViewModel.selectDateButton().check(matches(withText(expected)));

	}

}
