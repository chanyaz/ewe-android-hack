package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.TabletViewActions;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class RailScreen {

	public static ViewInteraction calendarButton() {
		return onView(withId(R.id.calendar_card));
	}

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(TabletViewActions.clickDates(start, end));
	}

	public static ViewInteraction searchButton() {
		onView(withId(R.id.search_container)).perform(ViewActions.waitForViewToDisplay());
		return onView(withId(R.id.search_button));
	}

	public static ViewInteraction dialogDoneButton() {
		return onView(withId(android.R.id.button1));
	}
}
