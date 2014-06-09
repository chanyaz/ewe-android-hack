package com.expedia.bookings.test.utilsEspresso;

import java.util.concurrent.atomic.AtomicReference;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import com.expedia.bookings.R;
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

	public static ViewAction getString(final AtomicReference<String> value) {
		return new ViewAction() {

			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(TextView.class));
			}

			@Override
			public void perform(UiController uiController, View view) {

				value.set(((TextView) view).getText().toString());
			}

			@Override
			public String getDescription() {
				return "get text from Text View";
			}
		};
	}

	//View Action to get the rating for RatingBar

	public static ViewAction getRating(final AtomicReference<Float> value) {
		return new ViewAction() {

			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(RatingBar.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				value.set(((RatingBar) view).getRating());
			}

			@Override
			public String getDescription() {
				return "get ratings from RatingBar widget";
			}
		};
	}

	//View Action to get the airline checkbox text and count in Tablet flight results filter

	public static ViewAction getChildViewText(final int index, final AtomicReference<String> value) {
		return new ViewAction() {

			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(ViewGroup.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
				SharedPreferences.Editor editor = prefs.edit();
				if (index == -1) {
					value.set(Integer.toString(((LinearLayout) view).getChildCount()));
				}
				else {
					View childView = ((LinearLayout) view).getChildAt(index);
					value.set(((CheckBox) childView.findViewById(R.id.filter_refinement_checkbox)).getText().toString());
				}
				editor.commit();
			}

			@Override
			public String getDescription() {
				return "get the  airline checkbox text";
			}
		};
	}

	//View Action to get the count of list view items

	public static ViewAction getCount(final AtomicReference<Integer> count) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(AdapterView.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				count.set(((AdapterView) view).getCount());
			}

			@Override
			public String getDescription() {
				return "Get total list items count";
			}
		};
	}

	//View Action to get the child count of list view items

	public static ViewAction getChildCount(final AtomicReference<Integer> count) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(AdapterView.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				count.set(((AdapterView) view).getChildCount());
			}

			@Override
			public String getDescription() {
				return "Get list items per screen height count";
			}
		};
	}
}

