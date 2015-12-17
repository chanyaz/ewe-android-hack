package com.expedia.bookings.test.ui.phone.tests.hotels;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.utils.JodaUtils;
import android.support.test.espresso.Espresso;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 5/21/14.
 */
public class HotelSearchActionBarTests extends PhoneTestCase {

	private static final String TAG = HotelSearchActionBarTests.class.getSimpleName();

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
		Calendar mCal = Calendar.getInstance();
		int mDayOfMonth = mCal.get(Calendar.DATE);
		LocalDate startDate = LocalDate.now().plusDays(dateOffset);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.calendarNumberTextView().check(matches(withText(Integer.toString(mDayOfMonth))));
		HotelsSearchScreen.clickDate(startDate);
		HotelsSearchScreen.calendarNumberTextView().check(matches(withText(Integer.toString(LocalDate.now().plusDays(dateOffset).getDayOfMonth()))));
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

		String tonight = getActivity().getResources().getString(R.string.Tonight);
		HotelsSearchScreen.dateRangeTextView().check(matches(withText(tonight)));
		int daysOffset = 1;

		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(35 + daysOffset);

		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		String firstDay = JodaUtils.formatLocalDate(getInstrumentation().getTargetContext(), LocalDate.now().plusDays(35), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		String secondDay = JodaUtils.formatLocalDate(getInstrumentation().getTargetContext(), LocalDate.now().plusDays(35 + daysOffset), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		String range = this.getActivity().getResources().getString(R.string.date_range_TEMPLATE, firstDay, secondDay);
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
		String expectedText = getActivity().getResources().getString(R.string.prices_avg_per_night);
		HotelsSearchScreen.pricingDescriptionTextView().check(matches(withText(expectedText)));
	}
}
