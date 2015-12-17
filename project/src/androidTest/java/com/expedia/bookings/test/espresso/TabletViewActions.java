package com.expedia.bookings.test.espresso;

import android.view.View;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import com.mobiata.android.time.widget.CalendarPicker;

public final class TabletViewActions {

	private TabletViewActions() {
		// ignore
	}

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
			return Matchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.isAssignableFrom(CalendarPicker.class));
		}

		@Override
		public void perform(UiController uiController, View view) {
			CalendarPicker cp = (CalendarPicker) view;
			cp.setSelectedDates(mStartDate, mEndDate);
		}

		@Override
		public String getDescription() {
			return "selects dates";
		}
	}
}
