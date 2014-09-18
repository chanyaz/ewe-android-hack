package com.expedia.bookings.test.tablet.pagemodels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.SuggestionAdapterViewProtocol;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDescendantOfA;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
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

	public static void clearDestinationEditText() {
		destinationEditText().perform(clearText());
	}

	public static void clickDestinationSearchButton() {
		destinationSearchButton().perform(click());
	}

	public static void typeInDestinationEditText(String text) {
		destinationEditText().perform(typeText(text));
	}

	public static void clickSuggestionAtPosition(int index) {
		onData(anything()) //
			.inAdapterView(allOf(withId(android.R.id.list), withParent(withParent(withId(R.id.suggestions_container))))) //
			.usingAdapterViewProtocol(SuggestionAdapterViewProtocol.getInstance()) //
			.atPosition(index) //
			.perform(click());
	}

	public static void clickSuggestion(String text) {
		onData(allOf(is(instanceOf(String.class)), equalTo(text))) //
			.inAdapterView(allOf(withId(android.R.id.list), isDescendantOfA(withId(R.id.suggestions_container)))) //
			.usingAdapterViewProtocol(SuggestionAdapterViewProtocol.getInstance()) //
			.perform(click());
	}
}
