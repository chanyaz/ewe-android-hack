package com.expedia.bookings.test.tests.hotels.ui.regression;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.CalendarTouchUtils;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.utils.JodaUtils;

public class HotelSearchActionBarTests extends CustomActivityInstrumentationTestCase<SearchActivity> {

	private static final String TAG = HotelSearchActionBarTests.class.getSimpleName();

	public HotelSearchActionBarTests() {
		super(SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();

		mUser.setHotelCityToRandomUSCity();
		mDriver.launchScreen().openMenuDropDown();
		mDriver.launchScreen().pressSettings();
		mDriver.settingsScreen().clickToClearPrivateData();
		mDriver.settingsScreen().clickOKString();
		mDriver.settingsScreen().clickOKString();
		mDriver.goBack();
	}

	// verify that the number shown in the text view
	// inside of guest button shows the proper number
	public void testGuestNumberTextView() {
		final int guestMax = 6;
		final int childMax = 4;
		mDriver.launchScreen().launchHotels();
		mDriver.delay(1);
		final int initialCount = Integer.parseInt(mDriver.hotelsSearchScreen().guestNumberTextView().getText()
				.toString());
		int currentCount = initialCount;
		assertEquals(initialCount, currentCount);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();

		int diff = 0;
		for (int i = initialCount; i < guestMax; i++) {
			diff++;
			mDriver.hotelsSearchScreen().guestPicker().clickIncrementAdultsButton();
			currentCount = Integer.parseInt(mDriver.hotelsSearchScreen().guestNumberTextView().getText()
					.toString());
			assertEquals(diff, currentCount - initialCount);
		}

		for (int i = currentCount; i > 1; i--) {
			diff--;
			mDriver.hotelsSearchScreen().guestPicker().clickDecrementAdultsButton();
			currentCount = Integer.parseInt(mDriver.hotelsSearchScreen().guestNumberTextView().getText()
					.toString());
			assertEquals(diff, currentCount - initialCount);
		}

		diff = 0;
		for (int i = initialCount; i <= childMax; i++) {
			diff++;
			mDriver.hotelsSearchScreen().guestPicker().clickIncrementChildrenButton();
			currentCount = Integer.parseInt(mDriver.hotelsSearchScreen().guestNumberTextView().getText()
					.toString());
			assertEquals(diff, currentCount - initialCount);
		}

		for (int i = currentCount; i > 1; i--) {
			diff--;
			mDriver.hotelsSearchScreen().guestPicker().clickDecrementChildrenButton();
			currentCount = Integer.parseInt(mDriver.hotelsSearchScreen().guestNumberTextView().getText()
					.toString());
			assertEquals(diff, currentCount - initialCount);
		}

		currentCount = Integer.parseInt(mDriver.hotelsSearchScreen().guestNumberTextView().getText()
				.toString());

		diff = 0;
		while (currentCount < 4) {
			diff += 2;
			mDriver.hotelsSearchScreen().guestPicker().clickIncrementAdultsButton();
			mDriver.hotelsSearchScreen().guestPicker().clickIncrementChildrenButton();
			currentCount = Integer.parseInt(mDriver.hotelsSearchScreen().guestNumberTextView().getText()
					.toString());
			assertEquals(diff, currentCount - initialCount);
		}

		while (currentCount > 1) {
			diff -= 2;
			mDriver.hotelsSearchScreen().guestPicker().clickDecrementAdultsButton();
			mDriver.hotelsSearchScreen().guestPicker().clickDecrementChildrenButton();
			currentCount = Integer.parseInt(mDriver.hotelsSearchScreen().guestNumberTextView().getText()
					.toString());
			assertEquals(diff, currentCount - initialCount);
		}
	}

	// verify that the text in the calendar button
	// shows the right number when it is changed
	public void testCalendarDateTextView() {
		final int dateOffset = 3;
		int modifiableOffset = 3;

		Calendar c = Calendar.getInstance();
		int dayOfMonth = c.get(Calendar.DATE);
		int daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);

		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickOnCalendarButton();

		// If a 3-day offset brings the selected date into the next month
		// click on the first day of the next month so that it is the reference
		// point
		if (dayOfMonth + modifiableOffset > daysInMonth) {
			mDriver.hotelsSearchScreen().clickDate(daysInMonth - dayOfMonth + 1);
			modifiableOffset += daysInMonth - dayOfMonth + 1;
		}
		mDriver.delay(1);
		int initialCalendarTextViewNumber = Integer.parseInt((String) mDriver.hotelsSearchScreen()
				.calendarNumberTextView()
				.getText());
		assertEquals(initialCalendarTextViewNumber, dayOfMonth);

		mDriver.hotelsSearchScreen().clickDate(modifiableOffset);
		mDriver.hotelsSearchScreen().clickDate(modifiableOffset + 1);
		mDriver.hotelsSearchScreen().clickDate(modifiableOffset);
		mDriver.delay(1);

		int postChangeCalendarTextViewNumber = Integer.parseInt((String) mDriver.hotelsSearchScreen()
				.calendarNumberTextView()
				.getText());

		assertEquals(dateOffset, postChangeCalendarTextViewNumber - initialCalendarTextViewNumber);
	}

	public void testHeaderDateText() throws Exception {
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText("New York, NY");
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());

		String dateRangeText = mDriver.hotelsSearchScreen().dateRangeTextView().getText().toString();
		String tonight = getString(R.string.Tonight);
		assertEquals(dateRangeText, tonight);

		int daysOffset = 1;
		mDriver.hotelsSearchScreen().clickOnCalendarButton();
		mDriver.hotelsSearchScreen().clickDate(daysOffset);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		dateRangeText = mDriver.hotelsSearchScreen().dateRangeTextView().getText().toString();
		long first = CalendarTouchUtils.getDay(daysOffset).toMillis(false);
		String firstDay = JodaUtils.formatLocalDate(getActivity().getApplicationContext(), new LocalDate(first),
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		long second = CalendarTouchUtils.getDay(daysOffset + 1).toMillis(false);
		String secondDay = JodaUtils.formatLocalDate(getActivity().getApplicationContext(), new LocalDate(second),
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		String range = this.getString(R.string.date_range_TEMPLATE, firstDay, secondDay);
		assertEquals(range, dateRangeText);
	}

	public void testHeaderPriceInfoText() throws Exception {
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText("New York, NY");
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		String priceInfoText = mDriver.hotelsSearchScreen().pricingDescriptionTextView().getText().toString();
		String expectedText = getString(R.string.prices_avg_per_night);
		assertEquals(expectedText, priceInfoText);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
