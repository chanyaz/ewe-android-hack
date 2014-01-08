package com.expedia.bookings.test.tests.pageModels.tablet;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

public class SearchScreen {

	public SearchScreen() {
	}

	// Object access

	public static ViewInteraction startSearchButton() {
		return onView(withId(R.id.search_status_text_view));
	}

	public static ViewInteraction cancelButton() {
		return onView(withId(R.id.cancel_button));
	}

	public static ViewInteraction searchButton() {
		return onView(withId(R.id.search_button));
	}

	public static ViewInteraction destinationEditText() {
		return onView(withId(R.id.destination_edit_text));
	}

	public static ViewInteraction searchDatesTextView() {
		return onView(withId(R.id.search_dates_text_view));
	}

	public static ViewInteraction originEditText() {
		return onView(withId(R.id.origin_edit_text));
	}

	public static ViewInteraction guestsTextView() {
		return onView(withId(R.id.guests_text_view));
	}

	// Object interaction

	@SuppressWarnings("unchecked")
	public static void clickInListWithText(String text) {
		//TextView in suggestion row with the passed String as its text
		onView(allOf(withId(R.id.location), withText(text))).perform(click());
	}

	public static void clickToStartSearch() {
		startSearchButton().perform(click());
	}

	public static void clickCancelButton() {
		cancelButton().perform(click());
	}

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

	public static void clickOriginEditText() {
		originEditText().perform(click());
	}

	public static void clearOriginEditText() {
		originEditText().perform(clearText());
	}

	public static void typeInOriginEditText(String text) {
		originEditText().perform(typeText(text));
	}

	public static void clickGuestsButton() {
		guestsTextView().perform(click());
	}

}
