package com.expedia.bookings.test.component.cars;

import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;
import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.espresso.TabletViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public final class CarSearchParamsModel {
	public static ViewInteraction dropOffButton() {
		return onView(withId(R.id.dropoff_datetime));
	}

	public static ViewInteraction pickUpButton() {
		return onView(withId(R.id.pickup_datetime));
	}

	public static ViewInteraction calendarContainer() {
		return onView(withId(R.id.calendar_container));
	}

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static ViewInteraction calendarActionButton() {
		return onView(withId(R.id.calendar_action_button));
	}

	public static ViewInteraction changeTime() {
		return onView(withId(R.id.change_time));
	}

	public static ViewInteraction timeContainer() {
		return onView(withId(R.id.time_container));
	}

	public static ViewInteraction timePicker() {
		return onView(withId(R.id.time_picker));
	}

	public static ViewInteraction timeConfirm() {
		return onView(withId(R.id.time_confirm_btn));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(TabletViewActions.clickDates(start, end));
	}
}
