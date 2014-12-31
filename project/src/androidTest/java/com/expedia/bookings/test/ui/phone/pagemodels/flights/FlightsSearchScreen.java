package com.expedia.bookings.test.ui.phone.pagemodels.flights;

import java.util.concurrent.atomic.AtomicReference;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.espresso.ViewActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.expedia.bookings.test.ui.espresso.CustomMatchers.withHint;
import static com.expedia.bookings.test.ui.espresso.ViewActions.clickDates;
import static com.expedia.bookings.test.ui.espresso.ViewActions.getString;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.closeSoftKeyboard;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/7/14.
 */
public class FlightsSearchScreen extends ScreenActions {
	private static final int DEPARTURE_AIRPORT_EDIT_TEXT_ID = R.id.departure_airport_edit_text;
	private static final int ARRIVAL_AIRPORT_EDIT_TEXT_ID = R.id.arrival_airport_edit_text;
	private static final int SELECT_DEPARTURE_VIEW_ID = R.id.dates_button;
	private static final int CLEAR_DATE_BUTTON_ID = R.id.clear_dates_btn;
	private static final int PASSENGER_SELECTION_BUTTON_ID = R.id.num_travelers_button;
	private static final int CALENDAR_DATE_PICKER_ID = R.id.calendar_date_picker;
	private static final int SEARCH_BUTTON_ID = R.id.search_button;
	private static final int OK_STRING_ID = R.string.ok;

	//Object retrievers

	public static ViewInteraction departureEditText() {
		return onView(withId(DEPARTURE_AIRPORT_EDIT_TEXT_ID));
	}

	public static ViewInteraction arrivalEditText() {
		return onView(withId(ARRIVAL_AIRPORT_EDIT_TEXT_ID));
	}

	public static ViewInteraction selectDepartureButton() {
		return onView(withId(SELECT_DEPARTURE_VIEW_ID));
	}

	public static ViewInteraction clearSelectedDatesButton() {
		return onView(withId(CLEAR_DATE_BUTTON_ID));
	}

	public static ViewInteraction passengerSelectionButton() {
		return onView(withId(PASSENGER_SELECTION_BUTTON_ID));
	}

	public static ViewInteraction searchButton() {
		return onView(withId(SEARCH_BUTTON_ID));
	}

	public static ViewInteraction calendarDatePicker() {
		return onView(withId(CALENDAR_DATE_PICKER_ID));
	}

	public static ViewInteraction okString() {
		return onView(withText(OK_STRING_ID));
	}

	// Object interactions

	public static void enterDepartureAirport(String text) {
		departureEditText().perform(typeText(text), closeSoftKeyboard());
	}

	public static void enterArrivalAirport(String text) {
		arrivalEditText().perform(typeText(text), closeSoftKeyboard());
	}

	public static void clickDepartureAirportField() {
		departureEditText().perform(click());
	}

	public static void clickArrivalAirportField() {
		arrivalEditText().perform(click());
	}

	public static void clickSelectDepartureButton() {
		selectDepartureButton().perform(click());
	}

	public static void clickClearSelectedDatesButton() {
		clearSelectedDatesButton().perform(click());
	}

	public static void clickPassengerSelectionButton() {
		passengerSelectionButton().perform(click());
	}

	public static void clickSearchButton() {
		searchButton().perform(click());
	}

	public static void clickDate(final LocalDate start, final LocalDate end) {
		calendarDatePicker().perform(clickDates(start, end));
	}

	public static void clickDate(final LocalDate start) {
		calendarDatePicker().perform(ViewActions.clickDate(start));
	}

	public static void incrementAdultsButton() {
		onView(withId(R.id.adults_plus)).perform(click());
	}

	public static String getTravelerNumberText() {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withId(R.id.adult_count_text)).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}

	public static void checkHint(String hintText) {
		selectDepartureButton().check(matches(withHint(hintText)));
	}
}
