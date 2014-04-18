package com.expedia.bookings.test.utilsEspresso;

import android.view.View;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import com.google.android.apps.common.testing.ui.espresso.ViewAction;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.mobiata.android.widget.CalendarDatePicker;
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers;

public final class ViewActions {

	private ViewActions() {
	}

	// View Action for Calender Utils

	public static ViewAction clickDates(LocalDate start, LocalDate end) {
		return new CalendarPickerClickDatesAction(start, end);
	}

	public final static class CalendarPickerClickDatesAction implements ViewAction {
		private LocalDate mStartDate;
		private LocalDate mEndDate;

		public CalendarPickerClickDatesAction(LocalDate start, LocalDate end) {
			mStartDate = start;
			mEndDate = end;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Matcher<View> getConstraints() {
			return Matchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.isAssignableFrom(CalendarDatePicker.class));
		}

		@Override
		public void perform(UiController uiController, View view) {
			CalendarDatePicker cp = (CalendarDatePicker) view;
			cp.updateStartDate(mStartDate.getYear(), mStartDate.getMonthOfYear(), mStartDate.getDayOfMonth());
			cp.updateEndDate(mEndDate.getYear(), mEndDate.getMonthOfYear(), mEndDate.getDayOfMonth());
		}

		@Override
		public String getDescription() {
			return "selects dates";
		}
	}
}

