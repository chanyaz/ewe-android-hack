package com.expedia.bookings.test.tests;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import com.expedia.bookings.test.activity.CalendarDatePickerTestActivity;
import com.expedia.bookings.utils.CalendarUtils;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.util.Ui;
import com.mobiata.android.widget.CalendarDatePicker;

import java.util.List;

public class CalendarDatePickerFlightsTest extends ActivityInstrumentationTestCase2<CalendarDatePickerTestActivity> {

	private CalendarDatePicker mCal = null;

	private Solo mSolo;

	private Activity mActivity;

	public CalendarDatePickerFlightsTest() {
		super(CalendarDatePickerTestActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		mActivity = getActivity();
		mSolo = new Solo(getInstrumentation(), mActivity);

		mCal = Ui.findView(mActivity, com.expedia.bookings.R.id.dates_date_picker);
		try {
			runTestOnUiThread(new Runnable() {
				@Override
				public void run() {
					mCal.setSelectionMode(CalendarDatePicker.SelectionMode.HYBRID);
					CalendarUtils.configureCalendarDatePicker(mCal, CalendarDatePicker.SelectionMode.HYBRID);
				}
			});
		}
		catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			runTestOnUiThread(new Runnable() {
				@Override
				public void run() {
					mCal.reset();

				}
			});
		}
		catch (Throwable throwable) {
			throwable.printStackTrace();
		}

		// must run this command otherwise subsequent tests will hang
		mSolo.finishOpenedActivities();
	}

	public void testNoDatesOnCalendarLoad() {
		assertNull(mCal.getStartTime());
		assertNull(mCal.getEndTime());
	}

	public void testStartDate() {
		Time expectedTime = getDay(1);

		clickOnDay(expectedTime);

		assertEquals(expectedTime, mCal.getStartTime());
	}

	public void testTapStartDateBeforeSelectedStartDateResetsStartDate() {
		Time firstTime = getDay(5);

		clickOnDay(firstTime);

		Time expectedTime = getDay(2);
		clickOnDay(expectedTime);

		assertEquals(expectedTime, mCal.getStartTime());
		assertNull(mCal.getEndTime());
	}

	public void testTapStartDateAndEndDate() {
		Time start = getDay(2);
		Time end = getDay(5);

		clickOnDay(start);
		clickOnDay(end);

		assertEquals(start, mCal.getStartTime());
		assertEquals(end, mCal.getEndTime());
	}

	public void testSelectStartAndEndThenTapStartAgainNullsEndDate() {
		Time start = getDay(1);
		Time end = getDay(3);

		clickOnDay(start);
		clickOnDay(end);

		assertEquals(end, mCal.getEndTime());

		clickOnDay(start);

		assertEquals(start, mCal.getStartTime());
		assertNull(mCal.getEndTime());
	}

	public void testSelectedStartAndEndThenTapEndYieldsExpectedBehavior() {
		Time one = getDay(1);
		Time three = getDay(3);

		clickOnDay(one);
		clickOnDay(three);
		clickOnDay(three);

		assertEquals(three, mCal.getStartTime());
		assertNull(mCal.getEndTime());
	}

	public void testCanSelectOneDayInPast() {
		Time past = getDay(-1);

		if (!clickOnDay(past)) {
			clickPrevMonth();
			clickOnDay(past);
		}

		assertEquals(past, mCal.getStartTime());
	}

	public void testCannotSelectFifteenDaysInPast() {
		Time past = getDay(-15);

		if (!clickOnDay(past)) {
			clickPrevMonth();
			clickOnDay(past);
		}

		assertNull(mCal.getStartTime());
		assertNull(mCal.getEndTime());
	}

	public void testCanAdvanceMonthAndSelectDate() {
		Time day = getDay(35);

		clickNextMonth();
		clickOnDay(day);

		assertEquals(day, mCal.getStartTime());
	}

	public void testSelectStartThenTapInPastMaintainsStart() {
		Time one = getDay(1);
		Time past = getDay(-2);

		clickOnDay(one);

		if (!clickOnDay(past)) {
			clickPrevMonth();
			clickOnDay(past);
		}

		assertEquals(one, mCal.getStartTime());
	}

	public void testRange() {
		Time one = getDay(1);
		Time four = getDay(4);

		clickOnDay(one);
		clickOnDay(four);

		List<Time> range = mCal.getDayRange();

		assertTrue(range.contains(getDay(2)));
		assertTrue(range.contains(getDay(3)));
	}

	public void testRangeClears() {
		Time one = getDay(1);
		Time four = getDay(4);

		clickOnDay(one);
		clickOnDay(four);
		clickOnDay(four);

		assertFalse(mCal.getDayRange().contains(getDay(2)));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// HELPER METHODS

	/**
	 * Retrieve a time object with a given offset in days
	 */
	private Time getDay(int daysOffset) {
		Time time = new Time(System.currentTimeMillis());
		time.monthDay += daysOffset;
		time.normalize(true);

		return time;
	}

	/**
	 * Returns true if the day to be clicked is present on the current month grid
	 */
	private boolean clickOnDay(Time day) {
		float x = mCal.getX(day);
		float y = mCal.getY(day);

		if (x >= 0.0f && y >= 0.0f) {
			mSolo.clickOnScreen(mCal.getX(day), mCal.getY(day));
			return true;
		}
		return false;
	}

	private void clickNextMonth() {
		mSolo.clickOnScreen(mCal.getNextMonthX(), mCal.getNextMonthY());
	}

	private void clickPrevMonth() {
		mSolo.clickOnScreen(mCal.getPrevMonthX(), mCal.getPrevMonthY());
	}
}
