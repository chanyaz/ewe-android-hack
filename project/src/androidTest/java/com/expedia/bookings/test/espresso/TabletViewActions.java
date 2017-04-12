package com.expedia.bookings.test.espresso;

import android.view.View;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.time.widget.CalendarPicker;

import junit.framework.Assert;

public final class TabletViewActions {

	private TabletViewActions() {
		// ignore
	}

	public static ViewAction clickDates(LocalDate start, LocalDate end) {
		return new CalendarPickerClickDatesAction(start, end);
	}

	public static ViewAction validateDatesTooltip(String lineOne, String lineTwo) {
		return new CalendarPickerValidateTooltipAction(lineOne, lineTwo);
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

	public final static class CalendarPickerValidateTooltipAction implements ViewAction {
		private String mlineOne;
		private String mlineTwo;

		public CalendarPickerValidateTooltipAction(String lineOne, String lineTwo) {
			mlineOne = lineOne;
			mlineTwo = lineTwo;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Matcher<View> getConstraints() {
			return Matchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.isAssignableFrom(CalendarPicker.class));
		}

		@Override
		public void perform(UiController uiController, View view) {
			CalendarPicker cp = (CalendarPicker) view;
			ViewGroup toolTipContainer = cp.getTooltipContainer();
			TextView toolTipLineOne = (TextView) toolTipContainer.findViewById(R.id.tooltip_line_one);
			TextView toolTipLineTwo = (TextView) toolTipContainer.findViewById(R.id.tooltip_line_two);
			Assert.assertEquals(View.VISIBLE, toolTipContainer.getVisibility());
			Assert.assertEquals(mlineOne, toolTipLineOne.getText().toString());
			Assert.assertEquals(mlineTwo, toolTipLineTwo.getText().toString());
		}

		@Override
		public String getDescription() {
			return "validate date tooltip";
		}
	}
}
