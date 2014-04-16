package com.expedia.bookings.test.utilsEspresso;

import android.view.View;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasSibling;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withChild;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import java.lang.reflect.*;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewAction;
import com.google.android.apps.common.testing.ui.espresso.action.GeneralLocation;
import com.google.android.apps.common.testing.ui.espresso.action.GeneralSwipeAction;
import com.google.android.apps.common.testing.ui.espresso.action.Press;
import com.google.android.apps.common.testing.ui.espresso.action.Swipe;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.mobiata.android.widget.CalendarDatePicker;
import com.expedia.bookings.widget.SlideToWidget;
import com.expedia.bookings.widget.SimpleNumberPicker;

import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers;

import com.mobiata.android.Log;

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

	//View Action for "Slide to purchase."

	public static ViewAction slidePurchase() {
		return new SlideToPurchase();
	}

	public final static class SlideToPurchase implements ViewAction {
		public SlideToPurchase() {

		}

		@SuppressWarnings("unchecked")
		@Override
		public Matcher<View> getConstraints() {
			return Matchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.isAssignableFrom(SlideToWidget.class));
		}

		@Override
		public void perform(UiController uiController, View view) {
			SlideToWidget sp = (SlideToWidget) view;
			Log.d("HERE slide all the way");
			sp.fireSlideAllTheWay();
		}

		@Override
		public String getDescription() {
			return "Slide to purchase";
		}

	}

	//View Action for Increment
	public static ViewAction increment() {
		return new Increment();
	}

	public final static class Increment implements ViewAction {
		public Increment() {

		}

		@SuppressWarnings("unchecked")
		@Override
		public Matcher<View> getConstraints() {
			return Matchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.isAssignableFrom(SimpleNumberPicker.class));
		}

		@Override
		public void perform(UiController uiController, View view) {
			onView(withId(R.id.increment)).perform();
		}

		@Override
		public String getDescription() {
			return "increment";
		}
	}

	//View Action for Decrement
	public static ViewAction decrement() {
		return new Decrement();
	}

	public final static class Decrement implements ViewAction {
		public Decrement() {

		}

		@SuppressWarnings("unchecked")
		@Override
		public Matcher<View> getConstraints() {
			return Matchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.isAssignableFrom(SimpleNumberPicker.class));
		}

		@Override
		public void perform(UiController uiController, View view) {
			onView(withId(R.id.decrement)).perform();
		}

		@Override
		public String getDescription() {
			return "increment";
		}
	}
}

