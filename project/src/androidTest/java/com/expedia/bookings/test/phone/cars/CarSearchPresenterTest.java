package com.expedia.bookings.test.phone.cars;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchParamsBuilder;
import com.expedia.bookings.presenter.car.CarSearchPresenter;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.espresso.EspressoUtils;
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

@RunWith(AndroidJUnit4.class)
public final class CarSearchPresenterTest {
	private static final String DATE_TIME_PATTERN = "MMM d, h:mm a";

	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.widget_car_search_params);
	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	@Test
	public void testViewPopulatesSearchParams() throws Throwable {
		DateTime today = DateTime.now();
		boolean isEleventhHour = today.getHourOfDay() == 23;
		DateTime expectedStartDate = isEleventhHour ? today.plusDays(1) : today;

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

		CarScreen.selectAirport("SFO", "San Francisco, CA");
		CarScreen.selectDateButton().perform(click());
		CarScreen.selectDates(expected.startDateTime.toLocalDate(), expected.endDateTime.toLocalDate());
		CarScreen.searchButton().perform(click());

		actual = widget.getCurrentParams();
		assertEquals(expected.origin, actual.origin);
		assertEquals(expected.startDateTime, actual.startDateTime.withTimeAtStartOfDay());
		assertEquals(expected.endDateTime, actual.endDateTime.withTimeAtStartOfDay());
	}

	@Test
	public void testDateButtonTextPopulation() throws Throwable {
		// Open calendar
		CarScreen.selectAirport("SFO", "San Francisco, CA");
		CarScreen.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));
		CarScreen.selectDateButton().perform(click());

		final DateTime tomorrow = DateTime.now().plusDays(1);
		final DateTime tomorrowsTomorrow = tomorrow.plusDays(1);
		String tomorrowStr = generateDefaultStartDateTimeStrForDateTimeButton(tomorrow);
		String tomorrowsTomorrowStr = generateDefaultEndDateTimeStrForDateTimeButton(tomorrowsTomorrow);

		// Select start date
		String expectedText = tomorrowStr + " – Select return date";
		CarScreen.selectDates(tomorrow.toLocalDate(), null);
		CarScreen.selectDateButton().check(matches(withText(expectedText)));

		// Select end date
		CarScreen.selectDates(tomorrow.toLocalDate(), tomorrowsTomorrow.toLocalDate());
		String expected = tomorrowStr + " – " + tomorrowsTomorrowStr;
		CarScreen.selectDateButton().check(matches(withText(expected)));
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
		CarScreen.selectAirport("SFO", "San Francisco, CA");
		CarScreen.pickupLocation().perform(clearText());
		CarScreen.selectDateButton().perform(click());
		CarScreen.pickUpTimeBar().perform(ViewActions.setSeekbarTo(noonProgress));
		CarScreen.dropOffTimeBar().perform(ViewActions.setSeekbarTo(onePmProgress));
		CarScreen.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));

		//Select dates from calendar
		final DateTime tomorrow = DateTime.now().plusDays(1);
		final DateTime tomorrowsTomorrow = tomorrow.plusDays(1);
		CarScreen.selectDates(tomorrow.toLocalDate(), tomorrowsTomorrow.toLocalDate());
		int minutesToMillis = 30 * 60 * 1000;
		String expected = DateFormatUtils.formatCarDateTimeRange(playground.getActivity(),
			tomorrow.withTimeAtStartOfDay().plusMillis(noonProgress * minutesToMillis),
			tomorrowsTomorrow.withTimeAtStartOfDay().plusMillis(onePmProgress * minutesToMillis));
		CarScreen.selectDateButton().check(matches(withText(expected)));
	}

	// FIXME
	@Test
	public void testSelectingOnlyPickupDateClearsDropoffDate() throws Throwable {
		CarScreen.selectAirport("SFO", "San Francisco, CA");
		CarScreen.selectDateButton().perform(click());
		final DateTime twoDaysOut = DateTime.now().plusDays(3);
		final DateTime threeDaysOut = DateTime.now().plusDays(3);
		final DateTime fourDaysOut = threeDaysOut.plusDays(1);
		CarScreen.selectDates(threeDaysOut.toLocalDate(), fourDaysOut.toLocalDate());
		String expected = generateDefaultStartDateTimeStrForDateTimeButton(threeDaysOut) +
			" – " + generateDefaultEndDateTimeStrForDateTimeButton(fourDaysOut);
		CarScreen.selectDateButton().check(matches(withText(expected)));

		CarScreen.selectDates(twoDaysOut.toLocalDate(), null);
		String expected2 = generateDefaultStartDateTimeStrForDateTimeButton(twoDaysOut) + " – Select return date";
		CarScreen.selectDateButton().check(matches(withText(expected2)));
	}

	@Test
	public void testSearchButtonErrorMessageForIncompleteParams() throws Throwable {
		// Test with all params missing
		CarScreen.searchButton().perform(click());
		CarScreen.didNotGoToResults();

		// Test with only pickup location
		CarScreen.selectAirport("SFO", "San Francisco, CA");
		CarScreen.selectDateButton().perform(click());
		CarScreen.searchButton().perform(click());
		CarScreen.didNotGoToResults();

		// Test with only start date selected
		CarScreen.selectDateButton().perform(click());
		CarScreen.selectDates(LocalDate.now().plusDays(3), null);
		CarScreen.searchButton().perform(click());
		CarScreen.didNotGoToResults();

		// Test with origin and start date selected
		CarScreen.selectAirport("SFO", "San Francisco, CA");
		CarScreen.searchButton().perform(click());
		CarScreen.didNotGoToResults();

		//Test with invalid airport code we dont show calendar
		CarScreen.pickupLocation().perform(typeText("AAAA"));
		CarScreen.searchButton().perform(click());
		CarScreen.didNotshowCalendar();
	}

	@Test
	public void testSearchOnlyStartEndDateSelected() throws Throwable {
		//Test with only start and end date selected
		CarScreen.dropOffLocation().perform(click());
		CarScreen.alertDialogPositiveButton().perform(click());
		CarScreen.selectDateButton().perform(click());
		CarScreen.didNotGoToResults();
		EspressoUtils.assertViewIsDisplayed(R.id.search_container);
	}

	@Test
	public void testSearchButtonHasNoErrorMessageForCompleteParams() throws Throwable {
		CarScreen.selectAirport("SFO", "San Francisco, CA");
		CarScreen.selectDateButton().perform(click());
		CarScreen.selectDates(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4));
		CarScreen.searchButton().perform(click());
		CarScreen.alertDialog().check(doesNotExist());
	}

	@Test
	public void testDialogShownOnDropOffClick() {
		CarScreen.dropOffLocation().perform(click());
		CarScreen.alertDialog().check(matches(isDisplayed()));
		CarScreen.alertDialogMessage().check(matches(withText(R.string.drop_off_same_as_pick_up)));
		CarScreen.alertDialogPositiveButton().check(matches(isDisplayed()));
	}

	@Test
	public void testStartTimeBeforeCurrentTime() throws Throwable {
		//All `step` vars below are 30-minute-steps as on the Time Bar for Cars Search

		final DateTime today = DateTime.now();
		boolean isEleventhHour = today.getHourOfDay() == 23;
		int currentTimeSteps = (today.getHourOfDay() + 1) * 2 + (today.getMinuteOfHour() > 30 ? 1 : 0);
		int startTimeSteps = today.minusHours(2).getHourOfDay() * 2;
		if (startTimeSteps > currentTimeSteps || isEleventhHour) {
			// 1. `startTimeSteps > currentTimeSteps`
			// Our intention was to bring startTimeSteps below currentTimeSteps, but `.minusHours(2)` took us back 1 day
			// frustrating our intention. Since we need to be on the same day, so stay at the minimum step for that day!
			// 2. `isEleventhHour`
			// At the eleventh hour, we disable selecting the current date altogether, so select the minimum step for next day!
			startTimeSteps = today.getMinuteOfHour() > 30 ? 1 : 0;
		}

		int ninePMSteps = (12 + 9) * 2;
		CarScreen.selectAirport("SFO", "San Francisco, CA");
		CarScreen.pickupLocation().perform(clearText());
		CarScreen.selectDateButton().perform(click());
		CarScreen.pickUpTimeBar().perform(ViewActions.setSeekbarTo(startTimeSteps));
		CarScreen.dropOffTimeBar().perform(ViewActions.setSeekbarTo(ninePMSteps));
		CarScreen.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));

		//Select dates from calendar
		final DateTime tomorrow = today.plusDays(1);
		CarScreen.selectDates(today.toLocalDate(), tomorrow.toLocalDate());
		int millisInOneStep = 30 * 60 * 1000;
		String expected = DateFormatUtils.formatCarDateTimeRange(playground.getActivity(),
			today.withTimeAtStartOfDay().plusMillis(currentTimeSteps * millisInOneStep),
			tomorrow.withTimeAtStartOfDay().plusMillis(ninePMSteps * millisInOneStep));
		CarScreen.selectDateButton().check(matches(withText(expected)));
	}

	@Test
	public void testEndTimeBeforeStartTimeSameDay() throws Throwable {
		// 28 == 02:00 PM
		int twoPmProgress = 28;
		// 42 == 09:00 PM
		int ninePmProgress = 42;
		CarScreen.selectAirport("SFO", "San Francisco, CA");
		CarScreen.pickupLocation().perform(clearText());
		CarScreen.selectDateButton().perform(click());
		CarScreen.pickUpTimeBar().perform(ViewActions.setSeekbarTo(ninePmProgress));
		CarScreen.dropOffTimeBar().perform(ViewActions.setSeekbarTo(twoPmProgress));
		CarScreen.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));

		//Select dates from calendar
		final DateTime date = DateTime.now().plusDays(1);
		CarScreen.selectDates(date.toLocalDate(), date.toLocalDate());
		int minutesToMillis = 30 * 60 * 1000;
		String expected = DateFormatUtils.formatCarDateTimeRange(playground.getActivity(),
			date.withTimeAtStartOfDay().plusMillis(ninePmProgress * minutesToMillis),
			date.withTimeAtStartOfDay().plusMillis((ninePmProgress + 4) * minutesToMillis));
		CarScreen.selectDateButton().check(matches(withText(expected)));
	}
}
