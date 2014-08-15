package com.expedia.bookings.test.tablet.pagemodels;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.app.Instrumentation;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.SuggestionAdapterViewProtocol;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.expedia.bookings.test.espresso.TabletViewActions.clickDates;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.RootMatchers.withDecorView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by dmadan on 7/18/14.
 */
public class Search {

	public static ViewInteraction calendarPicker() {
		return onView(withId(R.id.calendar_picker));
	}

	public static void clickTravelerButton() {
		onView(withId(R.id.traveler_btn)).perform(click());
	}

	public static void clickDate(final LocalDate start, final LocalDate end) {
		calendarPicker().perform(clickDates(start, end));
	}

	public static ViewInteraction originButton() {
		return onView(withId(R.id.origin_btn));
	}

	public static void clickOriginButton() {
		originButton().perform(click());
	}

	public static ViewInteraction originEditText() {
		return onView(withId(R.id.waypoint_edit_text));
	}

	public static void typeInOriginEditText(String text) {
		originEditText().perform(typeText(text));
	}

	public static void clickSelectFlightDates() {
		onView(withId(R.id.calendar_btn)).perform(click());
	}

	public static void clickSearchPopupDone() {
		onView(withId(R.id.search_popup_done)).perform(click());
	}

	public static void incrementChildButton() {
		onView(withId(R.id.children_plus)).perform(click());
	}

	public static void decrementChildButton() {
		onView(withId(R.id.children_minus)).perform(click());
	}

	public static void incrementAdultButton() {
		onView(withId(R.id.adults_plus)).perform(click());
	}

	public static void decrementAdultButton() {
		onView(withId(R.id.adults_minus)).perform(click());
	}

	public static void selectChildTravelerAgeAt(int index, Activity activity) {
		onData(anything()).inRoot(withDecorView(not(is(activity.getWindow().getDecorView())))).atPosition(index).perform(click());
	}

	public static void clickChild1Spinner() {
		onView(withId(R.id.child_1_age_layout)).perform(click());
	}

	public static void clickChild2Spinner() {
		onView(withId(R.id.child_2_age_layout)).perform(click());
	}

	public static ViewInteraction lapInfantAlert() {
		return onView(withId(R.id.tablet_lap_infant_alert));
	}

	public static ViewInteraction adultCountText() {
		return onView(withId(R.id.adult_count_text));
	}

	public static ViewInteraction childCountText() {
		return onView(withId(R.id.child_count_text));
	}

	public static String childPickerStringPlural(int numberOfChildren, Instrumentation instrumentation) {
		return instrumentation //
			.getTargetContext() //
			.getResources() //
			.getQuantityString(R.plurals.number_of_children, numberOfChildren, numberOfChildren);
	}

	public static String adultPickerStringPlural(int numberOfAdults, Instrumentation instrumentation) {
		return instrumentation //
			.getTargetContext() //
			.getResources() //
			.getQuantityString(R.plurals.number_of_adults_TEMPLATE, numberOfAdults, numberOfAdults);
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
			.inAdapterView(allOf(withId(android.R.id.list), withParent(withParent(withId(R.id.suggestions_container))))) //
			.usingAdapterViewProtocol(SuggestionAdapterViewProtocol.getInstance()) //
			.perform(click());
	}

	public static ViewInteraction selectAirportButton() {
		return onView(allOf(withId(R.id.action_button), isDisplayed()));
	}
}
