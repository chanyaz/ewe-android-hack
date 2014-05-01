package com.expedia.bookings.test.utilsEspresso;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import com.google.android.apps.common.testing.ui.espresso.ViewAction;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.mobiata.android.widget.CalendarDatePicker;
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers;

import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;

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
			return Matchers.allOf(ViewMatchers.isDisplayed(), isAssignableFrom(CalendarDatePicker.class));
		}

		@Override
		public void perform(UiController uiController, View view) {
			CalendarDatePicker cp = (CalendarDatePicker) view;
			cp.updateStartDate(mStartDate.getYear(), mStartDate.getMonthOfYear(), mStartDate.getDayOfMonth());
			if (mEndDate.getDayOfMonth() != 1) {
				cp.updateEndDate(mEndDate.getYear(), mEndDate.getMonthOfYear(), mEndDate.getDayOfMonth());
			}
		}

		@Override
		public String getDescription() {
			return "selects dates";
		}
	}

	//View Action to get the search result row values

	public static ViewAction storeResultListRowValue(String value) {
		return new SearchResultRow(value);
	}

	public final static class SearchResultRow implements ViewAction {
		private String mValueString;

		public SearchResultRow(String value) {
			mValueString = value;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Matcher<View> getConstraints() {
			return Matchers.allOf(ViewMatchers.isDisplayed(), isAssignableFrom(TextView.class));
		}

		@Override
		public void perform(UiController uiController, View view) {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(mValueString, ((TextView) view).getText().toString());
			editor.commit();
		}

		@Override
		public String getDescription() {
			return "store search result row values";
		}

	}
}

