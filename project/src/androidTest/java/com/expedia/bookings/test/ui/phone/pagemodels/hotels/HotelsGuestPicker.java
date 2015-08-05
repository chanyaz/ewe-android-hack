package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import java.util.concurrent.atomic.AtomicReference;

import android.content.res.Resources;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.getString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class HotelsGuestPicker {

	private static final int SEARCH_BUTTON_ID = R.id.search_button;
	private static final int INCREMENT_BUTTON_ID = R.id.increment;
	private static final int DECREMENT_BUTTON_ID = R.id.decrement;
	public static final int ADULT_PICKER_VIEW_ID = R.id.adults_number_picker;
	public static final int CHILD_PICKER_VIEW_ID = R.id.children_number_picker;
	private static final int SELECT_CHILD_AGE_PLURAL_ID = R.plurals.select_each_childs_age;
	private static final int NUMBER_OF_ADULTS_PLURAL_ID = R.plurals.number_of_adults_TEMPLATE;
	private static final int NUMBER_OF_CHILDREN_PLURAL_ID = R.plurals.number_of_children;

	// Object access

	public static ViewInteraction searchButton() {
		return onView(withId(SEARCH_BUTTON_ID));
	}

	public static void incrementChildrenButton() {
		onView(allOf(withId(INCREMENT_BUTTON_ID), withParent(withId(CHILD_PICKER_VIEW_ID)))).perform(click());
	}

	public static void incrementAdultsButton() {
		onView(allOf(withId(INCREMENT_BUTTON_ID), withParent(withId(ADULT_PICKER_VIEW_ID)))).perform(click());

	}

	public static void decrementChildrenButton() {
		onView(allOf(withId(DECREMENT_BUTTON_ID), withParent(withId(R.id.children_number_picker)))).perform(click());
	}

	public static void decrementAdultsButton() {
		onView(allOf(withId(DECREMENT_BUTTON_ID), withParent(withId(R.id.adults_number_picker)))).perform(click());
	}

	public static String selectChildAgePlural(int quantity, Resources res) {
		return res.getQuantityString(SELECT_CHILD_AGE_PLURAL_ID, quantity);
	}

	public static String childPickerStringPlural(int numberOfChildren, Resources res) {
		return res.getQuantityString(NUMBER_OF_CHILDREN_PLURAL_ID, numberOfChildren, numberOfChildren);
	}

	public static String adultPickerStringPlural(int numberOfAdults, Resources res) {
		return res.getQuantityString(NUMBER_OF_ADULTS_PLURAL_ID, numberOfAdults, numberOfAdults);
	}

	public static String getGuestTextViewValue(int level, int resID) {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(allOf(withId(level), withParent(withId(resID)))).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}

	public static void childPickerIncrementIsDisabled() {
		onView(allOf(withId(INCREMENT_BUTTON_ID), withParent(withId(CHILD_PICKER_VIEW_ID)))).check(matches(
			not(isEnabled())));
	}

	public static void adultPickerIncrementIsDisabled() {
		onView(allOf(withId(INCREMENT_BUTTON_ID), withParent(withId(ADULT_PICKER_VIEW_ID)))).check(matches(
			not(isEnabled())));
	}

	public static void childPickerDecrementIsDisabled() {
		onView(allOf(withId(DECREMENT_BUTTON_ID), withParent(withId(CHILD_PICKER_VIEW_ID)))).check(matches(
			not(isEnabled())));
	}

	public static void adultPickerDecrementIsDisabled() {
		onView(allOf(withId(DECREMENT_BUTTON_ID), withParent(withId(ADULT_PICKER_VIEW_ID)))).check(matches(
			not(isEnabled())));
	}

	public static void guestsIndicatorTextMatches(String expectedCount) {
		onView(withId(R.id.guests_text_view)).check(matches(withText(expectedCount)));
	}

	public static ViewInteraction guestLayout() {
		return onView(withId(R.id.refinements_layout));
	}

	// Object interaction

	public static void clickOnSearchButton() {
		(searchButton()).perform(click());
	}
}
