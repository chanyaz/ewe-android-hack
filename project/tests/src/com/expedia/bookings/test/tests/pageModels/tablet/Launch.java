package com.expedia.bookings.test.tests.pageModels.tablet;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.mobiata.android.time.widget.CalendarPicker;
import com.mobiata.android.util.Ui;

import static com.expedia.bookings.test.utils.ViewActions.clickDates;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class Launch {

	// Object access

	public static ViewInteraction searchButton() {
		return onView(withId(R.id.fake_search_bar_container));
	}

	public static ViewInteraction destinationEditText() {
		return onView(withId(R.id.waypoint_edit_text));
	}

	public static ViewInteraction calendarPicker() {
		return onView(withId(R.id.calendar_picker));
	}

	// Object interaction

	public static void clickSearchButton() {
		searchButton().perform(click());
	}

	public static void clickDestinationEditText() {
		destinationEditText().perform(click());
	}

	public static void clearDestinationEditText() {
		destinationEditText().perform(clearText());
	}

	public static void typeInDestinationEditText(String text) {
		destinationEditText().perform(typeText(text));
	}

	public static void clickSuggestion(String text) {
		onData(allOf(is(instanceOf(String.class)), equalTo(text))).usingAdapterViewProtocol(SuggestionAdapterViewProtocol.getInstance()).perform(click());
	}

	public static void clickDate(final LocalDate start, final LocalDate end) {
		calendarPicker().perform(clickDates(start, end));
	}
}
