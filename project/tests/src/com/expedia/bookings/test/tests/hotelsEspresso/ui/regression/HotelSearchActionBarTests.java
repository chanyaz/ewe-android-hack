package com.expedia.bookings.test.tests.hotelsEspresso.ui.regression;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.expedia.bookings.utils.JodaUtils;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.mobiata.android.util.SettingUtils;

import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 5/21/14.
 */
public class HotelSearchActionBarTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	public HotelSearchActionBarTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = HotelSearchActionBarTests.class.getSimpleName();

	Context mContext;
	Resources mRes;
	Calendar mCal = Calendar.getInstance();
	int mYear = mCal.get(mCal.YEAR);
	int mMonth = mCal.get(mCal.MONTH) + 1;
	int mDayOfMonth = mCal.get(Calendar.DATE);

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mRes = mContext.getResources();
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		getActivity();
	}


	// verify that the number shown in the text view
	// inside of guest button shows the proper number
	public void testGuestNumberTextView() {
		final int guestMax = 6;
		final int childMax = 4;
		LaunchScreen.launchHotels();
		String initialCountString = EspressoUtils.getText(R.id.guests_text_view);
		final int initialCount = Integer.parseInt(initialCountString);
		int currentCount = initialCount;
		assertEquals(initialCount, currentCount);
		ScreenActions.enterLog(TAG, "count:" + initialCountString + "," + initialCount);
		HotelsSearchScreen.clickOnGuestsButton();

		int diff = 0;
		for (int i = initialCount; i < guestMax; i++) {
			diff++;
			HotelsGuestPicker.incrementAdultsButton();
			String currentCountString = EspressoUtils.getText(R.id.guests_text_view);
			currentCount = Integer.parseInt(currentCountString);
			assertEquals(diff, currentCount - initialCount);
		}

		for (int i = currentCount; i > 1; i--) {
			diff--;
			HotelsGuestPicker.decrementAdultsButton();
			String currentCountString = EspressoUtils.getText(R.id.guests_text_view);
			currentCount = Integer.parseInt(currentCountString);
			assertEquals(diff, currentCount - initialCount);
		}

		diff = 0;
		for (int i = initialCount; i <= childMax; i++) {
			diff++;
			HotelsGuestPicker.incrementChildrenButton();
			String currentCountString = EspressoUtils.getText(R.id.guests_text_view);
			currentCount = Integer.parseInt(currentCountString);
			assertEquals(diff, currentCount - initialCount);
		}

		for (int i = currentCount; i > 1; i--) {
			diff--;
			HotelsGuestPicker.decrementChildrenButton();
			String currentCountString = EspressoUtils.getText(R.id.guests_text_view);
			currentCount = Integer.parseInt(currentCountString);
			assertEquals(diff, currentCount - initialCount);
		}

		String currentCountString = EspressoUtils.getText(R.id.guests_text_view);
		currentCount = Integer.parseInt(currentCountString);

		diff = 0;
		while (currentCount < 4) {
			diff += 2;
			HotelsGuestPicker.incrementAdultsButton();
			HotelsGuestPicker.incrementChildrenButton();
			currentCountString = EspressoUtils.getText(R.id.guests_text_view);
			currentCount = Integer.parseInt(currentCountString);
			assertEquals(diff, currentCount - initialCount);
		}

		while (currentCount > 1) {
			diff -= 2;
			HotelsGuestPicker.decrementAdultsButton();
			HotelsGuestPicker.decrementChildrenButton();
			currentCountString = EspressoUtils.getText(R.id.guests_text_view);
			currentCount = Integer.parseInt(currentCountString);
			assertEquals(diff, currentCount - initialCount);
		}
		Espresso.pressBack();
		Espresso.pressBack();
	}

	// verify that the text in the calendar button
	// shows the right number when it is changed
	public void testCalendarDateTextView() {
		final int dateOffset = 5;
		LaunchScreen.launchHotels();

		LocalDate mStartDate = new LocalDate(mYear, mMonth, 5);
		LocalDate mEndDate = new LocalDate(mYear, mMonth, 1);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.calendarNumberTextView().check(matches(withText(Integer.toString(mDayOfMonth))));
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);
		HotelsSearchScreen.calendarNumberTextView().check(matches(withText(Integer.toString(dateOffset))));
		Espresso.pressBack();
		Espresso.pressBack();

	}

	public void testHeaderDateText() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		String tonight = mRes.getString(R.string.Tonight);
		HotelsSearchScreen.dateRangeTextView().check(matches(withText(tonight)));
		int daysOffset = 1;

		LocalDate mStartDate = new LocalDate(mYear, mMonth, 5);
		LocalDate mEndDate = new LocalDate(mYear, mMonth, 5 + daysOffset);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		String firstDay = JodaUtils.formatLocalDate(mContext, new LocalDate(mYear, mMonth + 1, 5), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		String secondDay = JodaUtils.formatLocalDate(mContext, new LocalDate(mYear, mMonth + 1, 5 + daysOffset), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		String range = this.mRes.getString(R.string.date_range_TEMPLATE, firstDay, secondDay);
		HotelsSearchScreen.dateRangeTextView().check(matches(withText(range)));

		Espresso.pressBack();
	}

	public void testHeaderPriceInfoText() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		String expectedText = mRes.getString(R.string.prices_avg_per_night);
		HotelsSearchScreen.pricingDescriptionTextView().check(matches(withText(expectedText)));
	}
}
