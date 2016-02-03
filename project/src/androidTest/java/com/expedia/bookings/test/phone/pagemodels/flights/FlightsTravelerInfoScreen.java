package com.expedia.bookings.test.phone.pagemodels.flights;

import java.util.concurrent.atomic.AtomicReference;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.CommonTravelerInformationScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.getEmptyTravelerViewLayout;
import static com.expedia.bookings.test.espresso.ViewActions.getPopulatedTravelerViewLayout;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class FlightsTravelerInfoScreen extends CommonTravelerInformationScreen {

	public static ViewInteraction redressEditText() {
		return onView(withId(R.id.edit_redress_number));
	}

	public static void typeRedressText(String redressText) {
		redressEditText().perform(typeText(redressText));
	}

	public static void clickSetButton() {
		onView(withId(android.R.id.button1)).perform(click());
	}

	public static void clickEmptyTravelerDetails(int index) {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withId(R.id.traveler_container)).perform(waitForViewToDisplay(), getEmptyTravelerViewLayout(index, value));
		String filterValue = value.get();
		onView(withText(filterValue)).perform(click());
	}

	public static void clickPopulatedTravelerDetails(int index) {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withId(R.id.traveler_container)).perform(waitForViewToDisplay(), getPopulatedTravelerViewLayout(index, value));
		String filterValue = value.get();
		onView(withText(filterValue)).perform(click());
	}

	public static void clickEditTravelerInfo() {
		onView(withId(R.id.current_traveler_contact)).perform(click());
	}

	public static ViewInteraction nameMustMatchTextView() {
		return onView(withId(R.id.name_match_warning_text_view));
	}

	public static void assertEmptyTravelerDetailsLabel(int index, String assertString) {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withId(R.id.traveler_container)).perform(getEmptyTravelerViewLayout(index, value));
		String filterValue = value.get();
		onView(withText(filterValue)).check(matches(withText(assertString)));
	}
}
