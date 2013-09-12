package com.expedia.bookings.test.tests.hotels.ui.regression;

import java.util.Calendar;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;

public class HotelSearchActionBarTests extends ActivityInstrumentationTestCase2<SearchActivity> {

	private static final String TAG = "Hotel Search Regression";

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsTestDriver mDriver;
	private HotelsUserData mUser;
	private TestPreferences mPreferences;

	public HotelSearchActionBarTests() {
		super(SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mRes = getActivity().getBaseContext().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mDriver = new HotelsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser = new HotelsUserData();
		mUser.setHotelCityToRandomUSCity();
		try {
			mDriver.sweepstakesScreen().clickNoThanksButton();
		}
		catch (Throwable e) {
			mDriver.enterLog(TAG, "No sweepstakes activity to interact with!");
		}
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

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
