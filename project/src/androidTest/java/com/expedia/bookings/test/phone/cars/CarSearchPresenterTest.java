package com.expedia.bookings.test.phone.cars;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.presenter.car.CarSearchPresenter;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.vm.cars.CarSearchViewModel;

@RunWith(AndroidJUnit4.class)
public final class CarSearchPresenterTest {
	private static final String DATE_TIME_PATTERN = "MMM d, h:mm a";

	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.test_car_search_presenter, R.style.V2_Theme_Cars);

	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	CarSearchPresenter carSearchPresenter;

	@Before
	public void before() {
		carSearchPresenter = (CarSearchPresenter) playground.getRoot();
		carSearchPresenter.setSearchViewModel(new CarSearchViewModel(carSearchPresenter.getContext()));
	}

	// 29-Aug-2017 : Disabling car UI tests since car is now a webview
//	@Test
//	public void testViewPopulatesSearchParams() throws Throwable {
//		CarSearchParam actual;
//
//
//		DateTime today = DateTime.now();
//		boolean isEleventhHour = today.getHourOfDay() == 23;
//		DateTime expectedStartDate = isEleventhHour ? today.plusDays(1) : today;
//
//
//		CarScreen.locationCardView().perform(click());
//		CarScreen.pickupLocation().perform(ViewActions.waitForViewToDisplay(), typeText("SFO"));
//		CarScreen.selectPickupLocation("San Francisco, CA");
//		CarScreen.selectDates(expectedStartDate.toLocalDate(), expectedStartDate.plusDays(3).toLocalDate());
//		CarScreen.searchButton().perform(click());
//		actual = carSearchPresenter.getSearchViewModel().getCarParamsBuilder().build();
//
//
//		CarSearchParam expected = (CarSearchParam) new CarSearchParam.Builder()
//			.origin(CarDataUtils.getSuggestionFromLocation("SFO", null, "San Francisco, CA"))
//			.startDate(expectedStartDate.toLocalDate()).endDate(expectedStartDate.plusDays(3).toLocalDate()).build();
//
//		assertEquals(expected.getOriginLocation(), actual.getOriginLocation());
//		assertEquals(expected.getStartDateTime(), actual.getStartDateTime().withTimeAtStartOfDay());
//		assertEquals(expected.getEndDateTime(), actual.getEndDateTime().withTimeAtStartOfDay());
//	}


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

//	@Test
//	public void testSelectTimeBeforeDates() throws Throwable {
//		// 24 == 12:00 PM
//		int noonProgress = 24;
//		// 26 == 01:00 PM
//		int onePmProgress = 26;
//		CarScreen.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));
//
//		CarScreen.locationCardView().perform(click());
//		CarScreen.selectAirport("SFO", "San Francisco, CA");
//		CarScreen.pickUpTimeBar(playground.getActivity()).perform(ViewActions.setSeekBarTo(noonProgress));
//		CarScreen.dropOffTimeBar(playground.getActivity()).perform(ViewActions.setSeekBarTo(onePmProgress));
//
//		//Select dates from calendar
//		final DateTime tomorrow = DateTime.now().plusDays(1);
//		final DateTime tomorrowsTomorrow = tomorrow.plusDays(1);
//		CarScreen.selectDates(tomorrow.toLocalDate(), tomorrowsTomorrow.toLocalDate());
//		int minutesToMillis = 30 * 60 * 1000;
//		String expected = DateFormatUtils.formatStartEndDateTimeRange(playground.getActivity(),
//			tomorrow.withTimeAtStartOfDay().plusMillis(noonProgress * minutesToMillis),
//			tomorrowsTomorrow.withTimeAtStartOfDay().plusMillis(onePmProgress * minutesToMillis), false);
//		CarScreen.selectDateButton().check(matches(withText(expected)));
//	}
//
//	@Test
//	public void testSearchButtonErrorMessageForIncompleteParams() throws Throwable {
//		// Test with all params missing
//		CarScreen.searchButton().perform(click());
//		CarScreen.didNotGoToResults();
//
//		// Test with only start date selected
//		CarScreen.calendarCard().perform(click());
//		CarScreen.selectDates(LocalDate.now().plusDays(3), null);
//		CarScreen.searchButton().perform(click());
//		CarScreen.didNotGoToResults();
//
//		// Test with origin and start date selected
//		CarScreen.locationCardView().perform(click());
//		CarScreen.selectAirport("SFO", "San Francisco, CA");
//		CarScreen.searchButton().perform(click());
//		CarScreen.didNotGoToResults();
//	}
//
//	@Test
//	public void testSearchOnlyStartEndDateSelected() throws Throwable {
//		//Test with only start and end date selected
//		CarScreen.dropOffLocation().perform(click());
//		CarScreen.alertDialogPositiveButton().perform(click());
//		CarScreen.calendarCard().perform(click());
//		CarScreen.selectDates(LocalDate.now().plusDays(3), null);
//		CarScreen.didNotGoToResults();
//		EspressoUtils.assertViewIsDisplayed(R.id.search_container);
//	}
//
//	@Test
//	public void testSearchButtonHasNoErrorMessageForCompleteParams() throws Throwable {
//		CarScreen.locationCardView().perform(click());
//		SearchScreen.doGenericCarSearch();
//		CarScreen.alertDialog().check(doesNotExist());
//	}
//
//	@Test
//	public void testDialogShownOnDropOffClick() {
//		CarScreen.dropOffLocation().perform(click());
//		CarScreen.alertDialog().check(matches(isDisplayed()));
//		CarScreen.alertDialogMessage().check(matches(withText(R.string.drop_off_same_as_pick_up)));
//		CarScreen.alertDialogPositiveButton().check(matches(isDisplayed()));
//	}

	/*@Test
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
		CarScreen.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));
		CarScreen.locationCardView().perform(click());
		CarScreen.selectAirport("SFO", "San Francisco, CA");
		CarScreen.pickUpTimeBar(playground.getActivity()).perform(ViewActions.setSeekBarTo(startTimeSteps));
		CarScreen.dropOffTimeBar(playground.getActivity()).perform(ViewActions.setSeekBarTo(ninePMSteps));

		//Select dates from calendar
		final DateTime tomorrow = today.plusDays(1);
		CarScreen.selectDates(today.toLocalDate(), tomorrow.toLocalDate());
		int millisInOneStep = 30 * 60 * 1000;
		String expected = DateFormatUtils.formatStartEndDateTimeRange(playground.getActivity(),
			today.withTimeAtStartOfDay().plusMillis(currentTimeSteps * millisInOneStep),
			tomorrow.withTimeAtStartOfDay().plusMillis(ninePMSteps * millisInOneStep), false);
		CarScreen.selectDateButton().check(matches(withText(expected)));
	}*/

//	@Test
//	public void testEndTimeBeforeStartTimeSameDay() throws Throwable {
//		// 28 == 02:00 PM
//		int twoPmProgress = 28;
//		// 42 == 09:00 PM
//		int ninePmProgress = 42;
//		CarScreen.selectDateButton().check(matches(withText(R.string.select_pickup_and_dropoff_dates)));
//
//		CarScreen.locationCardView().perform(click());
//		CarScreen.selectAirport("SFO", "San Francisco, CA");
//		CarScreen.pickUpTimeBar(playground.getActivity()).perform(ViewActions.setSeekBarTo(ninePmProgress));
//		CarScreen.dropOffTimeBar(playground.getActivity()).perform(ViewActions.setSeekBarTo(twoPmProgress));
//
//		//Select dates from calendar
//		final DateTime date = DateTime.now().plusDays(1);
//		CarScreen.selectDates(date.toLocalDate(), date.toLocalDate());
//		int minutesToMillis = 30 * 60 * 1000;
//		String expected = DateFormatUtils.formatStartEndDateTimeRange(playground.getActivity(),
//			date.withTimeAtStartOfDay().plusMillis(ninePmProgress * minutesToMillis),
//			date.withTimeAtStartOfDay().plusMillis((ninePmProgress + 4) * minutesToMillis), false);
//		CarScreen.selectDateButton().check(matches(withText(expected)));
//	}
}
