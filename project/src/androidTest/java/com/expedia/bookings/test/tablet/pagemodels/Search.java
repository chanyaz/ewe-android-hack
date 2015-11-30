package com.expedia.bookings.test.tablet.pagemodels;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.TabletViewActions.clickDates;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;
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
			.inAdapterView(allOf(withId(android.R.id.list), isDescendantOfA(withId(R.id.suggestions_container)))) //
			.atPosition(index) //
			.perform(click());
	}

	public static void clickSuggestion(String text) {
		onData(allOf(hasToString(is(text))))
			.inAdapterView(allOf(withId(android.R.id.list), isDescendantOfA(withId(R.id.suggestions_container)))) //
				.perform(click());
	}

	public static ViewInteraction tripBucketDuration() {
		return onView(allOf(withId(R.id.trip_duration_text_view), isDisplayed()));
	}
}
