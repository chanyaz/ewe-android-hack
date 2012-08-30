package com.expedia.bookings.test.tests.ui;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import com.expedia.bookings.test.activity.CalendarDatePickerTestActivity;
import com.expedia.bookings.utils.CalendarUtils;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.util.Ui;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.testutils.CalendarTouchUtils;

import java.util.List;

public class CalendarDatePickerTest extends ActivityInstrumentationTestCase2<CalendarDatePickerTestActivity> {

	private CalendarDatePicker mCal = null;

	private Solo mSolo;

	private Activity mActivity;

	public CalendarDatePickerTest() {
		super(CalendarDatePickerTestActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		mActivity = getActivity();
		mSolo = new Solo(getInstrumentation(), mActivity);
	}

	@Override
	protected void tearDown() throws Exception {
		// reset the calendar so that the previous days do not appear on subsequent test runs
		resetCalendar();

		// must run this command otherwise subsequent tests will hang
		mSolo.finishOpenedActivities();

		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FLIGHTS (HYBRID)

	public void testNoDatesOnCalendarLoad() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);
		assertNull(mCal.getStartTime());
		assertNull(mCal.getEndTime());
	}

	public void testStartDate() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);

		Time expectedTime = CalendarTouchUtils.getDay(1);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, expectedTime);

		assertEquals(expectedTime, mCal.getStartTime());
	}

	public void testTapStartDateAndEndDate() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);

		Time start = CalendarTouchUtils.getDay(2);
		Time end = CalendarTouchUtils.getDay(5);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, start);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, end);

		assertEquals(start, mCal.getStartTime());
		assertEquals(end, mCal.getEndTime());
	}

	public void testTapStartDateBeforeSelectedStartDateResetsStartDate() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);

		Time firstTime = CalendarTouchUtils.getDay(5);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, firstTime);

		Time expectedTime = CalendarTouchUtils.getDay(2);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, expectedTime);

		assertEquals(expectedTime, mCal.getStartTime());
		assertNull(mCal.getEndTime());
	}

	public void testCanSelectOneDayInPast() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);

		Time past = CalendarTouchUtils.getDay(-1);

		if (!CalendarTouchUtils.clickOnDay(mSolo, mCal, past)) {
			CalendarTouchUtils.clickPrevMonth(mSolo, mCal);
			CalendarTouchUtils.clickOnDay(mSolo, mCal, past);
		}

		assertEquals(past, mCal.getStartTime());
	}

	public void testCannotSelectFifteenDaysInPast() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);

		Time past = CalendarTouchUtils.getDay(-15);

		if (!CalendarTouchUtils.clickOnDay(mSolo, mCal, past)) {
			CalendarTouchUtils.clickPrevMonth(mSolo, mCal);
			CalendarTouchUtils.clickOnDay(mSolo, mCal, past);
		}

		assertNull(mCal.getStartTime());
		assertNull(mCal.getEndTime());
	}

	public void testCanAdvanceMonthAndSelectDate() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);

		Time day = CalendarTouchUtils.getDay(35);

		CalendarTouchUtils.clickNextMonth(mSolo, mCal);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, day);

		assertEquals(day, mCal.getStartTime());
	}

	public void testSelectStartThenTapInPastMaintainsStart() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);

		Time one = CalendarTouchUtils.getDay(1);
		Time past = CalendarTouchUtils.getDay(-2);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, one);

		if (!CalendarTouchUtils.clickOnDay(mSolo, mCal, past)) {
			CalendarTouchUtils.clickPrevMonth(mSolo, mCal);
			CalendarTouchUtils.clickOnDay(mSolo, mCal, past);
		}

		assertEquals(one, mCal.getStartTime());
	}

	public void testRange() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);

		Time one = CalendarTouchUtils.getDay(1);
		Time four = CalendarTouchUtils.getDay(4);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, one);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, four);

		List<Time> range = mCal.getDayRange();

		assertTrue(range.contains(CalendarTouchUtils.getDay(2)));
		assertTrue(range.contains(CalendarTouchUtils.getDay(3)));
	}

	public void testRangeClears() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);

		Time one = CalendarTouchUtils.getDay(1);
		Time two = CalendarTouchUtils.getDay(2);
		Time four = CalendarTouchUtils.getDay(4);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, two);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, four);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, one);

		assertFalse(mCal.getDayRange().contains(two));
	}

	public void testSameDayRoundtrip() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);

		Time two = CalendarTouchUtils.getDay(2);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, two);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, two);

		assertEquals(two, mCal.getStartTime());
		assertEquals(two, mCal.getEndTime());
	}

	public void testHybridDragModeAllowsDrag() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);

		Time one = CalendarTouchUtils.getDay(1);
		Time three = CalendarTouchUtils.getDay(3);
		Time four = CalendarTouchUtils.getDay(4);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, one);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, three);
		CalendarTouchUtils.dragOnCalendar(mSolo, mCal, three, four);

		assertEquals(one, mCal.getStartTime());
		assertEquals(four, mCal.getEndTime());
	}

	public void testHybridDragModeAllowsDragToSameDayRoundtrip() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, true);

		Time one = CalendarTouchUtils.getDay(1);
		Time three = CalendarTouchUtils.getDay(3);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, one);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, three);

		assertEquals(one, mCal.getStartTime());
		assertEquals(three, mCal.getEndTime());

		Time two = CalendarTouchUtils.getDay(2);

		CalendarTouchUtils.dragOnCalendar(mSolo, mCal, three, two);
		CalendarTouchUtils.dragOnCalendar(mSolo, mCal, one, two);

		assertEquals(two, mCal.getStartTime());
		assertEquals(two, mCal.getEndTime());
	}

	public void testSelectStartAndEndThenTapStartAgainNullsEndDateNoDragMode() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, false);

		Time start = CalendarTouchUtils.getDay(1);
		Time end = CalendarTouchUtils.getDay(3);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, start);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, end);

		assertEquals(end, mCal.getEndTime());

		CalendarTouchUtils.clickOnDay(mSolo, mCal, start);

		assertEquals(start, mCal.getStartTime());
		assertNull(mCal.getEndTime());
	}

	public void testSelectedStartAndEndThenTapEndYieldsExpectedBehaviorNoDragMode() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, false);

		Time one = CalendarTouchUtils.getDay(1);
		Time three = CalendarTouchUtils.getDay(3);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, one);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, three);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, three);

		assertEquals(three, mCal.getStartTime());
		assertNull(mCal.getEndTime());
	}

	public void testNotHybridDragModeDoesNotAllowDrag() {
		setCalendarMode(CalendarDatePicker.SelectionMode.HYBRID, false);
		mCal.setAllowHybridDragging(false);

		Time two = CalendarTouchUtils.getDay(2);
		Time four = CalendarTouchUtils.getDay(4);
		Time five = CalendarTouchUtils.getDay(5);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, two);
		CalendarTouchUtils.clickOnDay(mSolo, mCal, four);
		CalendarTouchUtils.dragOnCalendar(mSolo, mCal, four, five);

		assertEquals(five, mCal.getStartTime());
		assertNull(mCal.getEndTime());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// HOTELS (RANGE)

	public void testDefault() {
		setCalendarMode(CalendarDatePicker.SelectionMode.RANGE);

		Time expectedStartDay = CalendarTouchUtils.getDay(0);
		Time expectedEndDay = CalendarTouchUtils.getDay(1);

		assertEquals(expectedStartDay, mCal.getStartTime());
		assertEquals(expectedEndDay, mCal.getEndTime());
	}

	public void testSelectNewDays() {
		setCalendarMode(CalendarDatePicker.SelectionMode.RANGE);

		Time expectedStartDay = CalendarTouchUtils.getDay(3);
		Time expectedEndDay = CalendarTouchUtils.getDay(4);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, expectedStartDay);

		assertEquals(expectedStartDay, mCal.getStartTime());
		assertEquals(expectedEndDay, mCal.getEndTime());
	}

	public void testDragStartDate() {
		setCalendarMode(CalendarDatePicker.SelectionMode.RANGE);

		Time four = CalendarTouchUtils.getDay(4);
		Time two = CalendarTouchUtils.getDay(2);

		CalendarTouchUtils.clickOnDay(mSolo, mCal, four);

		CalendarTouchUtils.dragOnCalendar(mSolo, mCal, four, two);

		assertEquals(two, mCal.getStartTime());

		Time five = CalendarTouchUtils.getDay(5);
		assertEquals(five, mCal.getEndTime());
	}

	public void testDragStartDatePastEndDate() {
		setCalendarMode(CalendarDatePicker.SelectionMode.RANGE);

		Time zero = CalendarTouchUtils.getDay(0);
		Time one = CalendarTouchUtils.getDay(1);
		Time two = CalendarTouchUtils.getDay(2);

		CalendarTouchUtils.dragOnCalendar(mSolo, mCal, zero, two);

		assertEquals(one, mCal.getStartTime());
		assertEquals(two, mCal.getEndTime());
	}

	public void testDragEndDate() {
		setCalendarMode(CalendarDatePicker.SelectionMode.RANGE);

		Time originalEndDay = CalendarTouchUtils.getDay(1);
		Time expectedEndDay = CalendarTouchUtils.getDay(2);

		CalendarTouchUtils.dragOnCalendar(mSolo, mCal, originalEndDay, expectedEndDay);

		assertEquals(expectedEndDay, mCal.getEndTime());
	}

	public void testDragEndDateBeforeStartDate() {
		setCalendarMode(CalendarDatePicker.SelectionMode.RANGE);

		Time negOne = CalendarTouchUtils.getDay(-1);
		Time zero = CalendarTouchUtils.getDay(0);
		Time one = CalendarTouchUtils.getDay(1);

		CalendarTouchUtils.dragOnCalendar(mSolo, mCal, one, negOne);

		assertEquals(negOne, mCal.getStartTime());
		assertEquals(zero, mCal.getEndTime());
	}

	public void testDragStartAndEndDates() {
		setCalendarMode(CalendarDatePicker.SelectionMode.RANGE);

		Time negOne = CalendarTouchUtils.getDay(-1);
		Time zero = CalendarTouchUtils.getDay(0);
		Time one = CalendarTouchUtils.getDay(1);
		Time two = CalendarTouchUtils.getDay(2);

		CalendarTouchUtils.dragOnCalendar(mSolo, mCal, zero, negOne);
		CalendarTouchUtils.dragOnCalendar(mSolo, mCal, one, two);

		assertEquals(negOne, mCal.getStartTime());
		assertEquals(two, mCal.getEndTime());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FLIGHTTRACK (SINGLE)

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// HELPER METHODS

	private void setCalendarMode(final CalendarDatePicker.SelectionMode mode) {
		setCalendarMode(mode, false);
	}

	private void setCalendarMode(final CalendarDatePicker.SelectionMode mode, final boolean allowHybridDragging) {
		mCal = Ui.findView(mActivity, com.expedia.bookings.R.id.calendar_date_picker);
		try {
			runTestOnUiThread(new Runnable() {
				@Override
				public void run() {
					mCal.setSelectionMode(mode);
					mCal.setAllowHybridDragging(allowHybridDragging);
					CalendarUtils.configureCalendarDatePicker(mCal, mode);
				}
			});
		}
		catch (Throwable throwable) {
			throwable.printStackTrace();
		}

	}

	private void resetCalendar() {
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
	}
}
