package com.expedia.bookings.test.phone.pagemodels.flights;

import java.util.concurrent.atomic.AtomicReference;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.ViewActions.getString;

public class FlightsSearchScreen {

	public static ViewInteraction departureEditText() {
		return onView(withId(R.id.departure_airport_edit_text));
	}

	public static ViewInteraction arrivalEditText() {
		return onView(withId(R.id.arrival_airport_edit_text));
	}

	public static ViewInteraction searchButton() {
		return onView(withId(R.id.search_button));
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

	public static void clickSearchButton() {
		searchButton().perform(click());
	}

	public static void incrementAdultsButton() {
		onView(withId(R.id.adults_plus)).perform(click());
	}

	public static void incrementChildrenButton() {
		onView(withId(R.id.children_plus)).perform(click());
	}

	public static String getTravelerNumberText() {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withId(R.id.adult_count_text)).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}

	public static String getAdultTravelerNumberText() {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withId(R.id.adult)).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}

	public static String getChildTravelerNumberText() {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withId(R.id.children)).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}

	public static ViewInteraction actionBarUp() {
		return onView(withId(android.R.id.home));
	}
}
