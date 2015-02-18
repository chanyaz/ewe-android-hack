package com.expedia.bookings.test.component.lx;

import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.espresso.TabletViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class LXViewModel {
	public static ViewInteraction calendar() {
		return onView(withId(R.id.search_calendar));
	}

	public static ViewInteraction closeButton() {
		return onView(withId(R.id.search_params_close));
	}

	public static ViewInteraction doneButton() {
		return onView(withId(R.id.search_params_done));
	}

	public static ViewInteraction header() {
		return onView(withId(R.id.search_header));
	}

	public static ViewInteraction location() {
		return onView(withId(R.id.search_location));
	}

	public static ViewInteraction selectDateButton() {
		return onView(withId(R.id.select_dates));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(TabletViewActions.clickDates(start, end));
	}

	public static ViewInteraction alertDialogMessage() {
		return onView(withId(android.R.id.message));
	}
	
	public static ViewInteraction alertDialogNeutralButton() {
		return onView(withId(android.R.id.button3));
	}
}
