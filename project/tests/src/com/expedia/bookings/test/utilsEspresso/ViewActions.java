package com.expedia.bookings.test.utilsEspresso;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.RatingBar;
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

	//View Action to get the values for a view

	public static ViewAction storeValue(String value) {
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
			return Matchers.allOf(isAssignableFrom(TextView.class));
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
			return "store values";
		}

	}

	//View Action to get the count of list view items

	public static ViewAction getCount(String key, int code) {
		return new CountListItems(key, code);
	}

	public final static class CountListItems implements ViewAction {
		private String mKeyString;
		private int mCode;

		public CountListItems(String keystring, int code) {
			mKeyString = keystring;
			mCode = code;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Matcher<View> getConstraints() {
			return Matchers.allOf(ViewMatchers.isDisplayed(), isAssignableFrom(ListView.class));
		}

		@Override
		public void perform(UiController uiController, View view) {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
			SharedPreferences.Editor editor = prefs.edit();
			if (mCode == 1) {
				editor.putInt(mKeyString, ((ListView) view).getCount());
			}
			else {
				editor.putInt(mKeyString, ((ListView) view).getChildCount());
			}
			editor.commit();
		}

		@Override
		public String getDescription() {
			return "Get total list items count and list items per screen height count";
		}
	}

	//View Action to get the rating for RatingBar

	public static ViewAction getRating(String value) {
		return new getRatings(value);
	}

	public final static class getRatings implements ViewAction {
		private String mValueString;

		public getRatings(String value) {
			mValueString = value;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Matcher<View> getConstraints() {
			return Matchers.allOf(isAssignableFrom(RatingBar.class));
		}

		@Override
		public void perform(UiController uiController, View view) {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
			SharedPreferences.Editor editor = prefs.edit();
			editor.putFloat(mValueString, ((RatingBar) view).getRating());
			editor.commit();
		}

		@Override
		public String getDescription() {
			return "get ratings from RatingBar widget";
		}
	}
}

