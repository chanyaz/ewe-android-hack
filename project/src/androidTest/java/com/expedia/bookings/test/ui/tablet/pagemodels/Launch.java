package com.expedia.bookings.test.ui.tablet.pagemodels;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

public class Launch {

	// Object access

	public static ViewInteraction searchButton() {
		return onView(withId(R.id.fake_search_bar_container));
	}

	public static ViewInteraction destinationEditText() {
		return onView(withId(R.id.waypoint_edit_text));
	}

	public static ViewInteraction destinationSearchButton() {
		return onView(withId(R.id.dest_btn));
	}

	// Object interaction

	public static void clickSearchButton() {
		searchButton().perform(click());
	}

	public static void clickDestinationEditText() {
		destinationEditText().perform(click());
	}

	public static void clickDestinationSearchButton() {
		destinationSearchButton().perform(click());
	}

	public static void typeInDestinationEditText(String text) {
		destinationEditText().perform(typeText(text));
	}

	public static void clickSuggestionAtPosition(int index) {
		onData(anything()) //
			.inAdapterView(allOf(withId(android.R.id.list), isDescendantOfA(withId(R.id.suggestions_container)))) //
			.atPosition(index) //
			.perform(click());
	}

	public static void clickSuggestion(String text) {
		onData(allOf(hasToString(is(text))))
			.inAdapterView(allOf(withId(android.R.id.list), isDescendantOfA(withId(R.id.suggestions_container)))) //
			.perform(click());
	}

}
