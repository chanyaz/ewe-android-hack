package com.expedia.bookings.test.tests.hotelsEspresso.ui.regression;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
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

/**
 * Created by dmadan on 5/21/14.
 */
public class HotelSearchActionBarTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	public HotelSearchActionBarTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = HotelSearchActionBarTests.class.getSimpleName();

	Context mContext;
	SharedPreferences mPrefs;
	Resources mRes;
	Calendar mCal = Calendar.getInstance();
	int mYear = mCal.get(mCal.YEAR);
	int mMonth = mCal.get(mCal.MONTH) + 1;
	int mDayOfMonth = mCal.get(Calendar.DATE);

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
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
		EspressoUtils.getValues("initialCount", R.id.guests_text_view);
		String initialCountString = mPrefs.getString("initialCount", "");
		final int initialCount = Integer.parseInt(initialCountString);
		int currentCount = initialCount;
		assertEquals(initialCount, currentCount);
		ScreenActions.enterLog(TAG, "count:" + initialCountString + "," + initialCount);
		HotelsSearchScreen.clickOnGuestsButton();

		int diff = 0;
		for (int i = initialCount; i < guestMax; i++) {
			diff++;
			HotelsGuestPicker.incrementAdultsButton();
			EspressoUtils.getValues("currentCount", R.id.guests_text_view);
			String currentCountString = mPrefs.getString("currentCount", "");
			currentCount = Integer.parseInt(currentCountString);
			assertEquals(diff, currentCount - initialCount);
		}

		for (int i = currentCount; i > 1; i--) {
			diff--;
			HotelsGuestPicker.decrementAdultsButton();
			EspressoUtils.getValues("currentCount", R.id.guests_text_view);
			String currentCountString = mPrefs.getString("currentCount", "");
			currentCount = Integer.parseInt(currentCountString);
			assertEquals(diff, currentCount - initialCount);
		}

		diff = 0;
		for (int i = initialCount; i <= childMax; i++) {
			diff++;
			HotelsGuestPicker.incrementChildrenButton();
			EspressoUtils.getValues("currentCount", R.id.guests_text_view);
			String currentCountString = mPrefs.getString("currentCount", "");
			currentCount = Integer.parseInt(currentCountString);
			assertEquals(diff, currentCount - initialCount);
		}

		for (int i = currentCount; i > 1; i--) {
			diff--;
			HotelsGuestPicker.decrementChildrenButton();
			EspressoUtils.getValues("currentCount", R.id.guests_text_view);
			String currentCountString = mPrefs.getString("currentCount", "");
			currentCount = Integer.parseInt(currentCountString);
			assertEquals(diff, currentCount - initialCount);
		}

		EspressoUtils.getValues("currentCount", R.id.guests_text_view);
		String currentCountString = mPrefs.getString("currentCount", "");
		currentCount = Integer.parseInt(currentCountString);

		diff = 0;
		while (currentCount < 4) {
			diff += 2;
			HotelsGuestPicker.incrementAdultsButton();
			HotelsGuestPicker.incrementChildrenButton();
			EspressoUtils.getValues("currentCount", R.id.guests_text_view);
			currentCountString = mPrefs.getString("currentCount", "");
			currentCount = Integer.parseInt(currentCountString);
			assertEquals(diff, currentCount - initialCount);
		}

		while (currentCount > 1) {
			diff -= 2;
			HotelsGuestPicker.decrementAdultsButton();
			HotelsGuestPicker.decrementChildrenButton();
			EspressoUtils.getValues("currentCount", R.id.guests_text_view);
			currentCountString = mPrefs.getString("currentCount", "");
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
		EspressoUtils.getValues("initialCalendarTextViewNumber", R.id.dates_text_view);
		String initialCalendarTextViewString = mPrefs.getString("initialCalendarTextViewNumber", "");
		int initialCalendarTextViewNumber = Integer.parseInt(initialCalendarTextViewString);
		assertEquals(initialCalendarTextViewNumber, mDayOfMonth);
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);

		EspressoUtils.getValues("postChangeCalendarTextViewNumber", R.id.dates_text_view);
		String postChangeCalendarTextViewString = mPrefs.getString("postChangeCalendarTextViewNumber", "");
		int postChangeCalendarTextViewNumber = Integer.parseInt(postChangeCalendarTextViewString);
		assertEquals(dateOffset, postChangeCalendarTextViewNumber);
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

		EspressoUtils.getValues("dateRangeText", R.id.search_date_range_text);
		String dateRangeText = mPrefs.getString("dateRangeText", "");
		String tonight = mRes.getString(R.string.Tonight);
		assertEquals(dateRangeText, tonight);
		int daysOffset = 1;

		LocalDate mStartDate = new LocalDate(mYear, mMonth, 5);
		LocalDate mEndDate = new LocalDate(mYear, mMonth, 5 + daysOffset);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		EspressoUtils.getValues("dateRangeText", R.id.search_date_range_text);
		dateRangeText = mPrefs.getString("dateRangeText", "");
		String firstDay = JodaUtils.formatLocalDate(mContext, new LocalDate(mYear, mMonth + 1, 5), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		String secondDay = JodaUtils.formatLocalDate(mContext, new LocalDate(mYear, mMonth + 1, 5 + daysOffset), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		String range = this.mRes.getString(R.string.date_range_TEMPLATE, firstDay, secondDay);
		assertEquals(range, dateRangeText);
		Espresso.pressBack();
	}

	public void testHeaderPriceInfoText() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		EspressoUtils.getValues("priceInfoText", R.id.lawyer_label_text_view);
		String priceInfoText = mPrefs.getString("priceInfoText", "");
		String expectedText = mRes.getString(R.string.prices_avg_per_night);
		assertEquals(expectedText, priceInfoText);
	}
}
