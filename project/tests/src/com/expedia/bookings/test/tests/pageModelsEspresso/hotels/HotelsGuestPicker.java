package com.expedia.bookings.test.tests.pageModelsEspresso.hotels;


import static com.expedia.bookings.test.utilsEspresso.ViewActions.increment;
import static com.expedia.bookings.test.utilsEspresso.ViewActions.decrement;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

/**
 * Created by dmadan on 4/10/14.
 */
public class HotelsGuestPicker extends ScreenActions {

	private static final int SEARCH_BUTTON_ID = R.id.search_button;

	private static final int INCREMENT_BUTTON_ID = R.id.increment;
	private static final int DECREMENT_BUTTON_ID = R.id.decrement;

	private static final int ADULT_PICKER_VIEW_ID = R.id.adults_number_picker;
	private static final int CHILD_PICKER_VIEW_ID = R.id.children_number_picker;

	private static final int LOWER_TEXT_VIEW_ID = R.id.text_lower;
	private static final int CURRENT_TEXT_VIEW_ID = R.id.text_current;
	private static final int HIGHER_TEXT_VIEW_ID = R.id.text_higher;

	private static final int SELECT_CHILD_AGE_PLURAL_ID = R.plurals.select_each_childs_age;
	private static final int NUMBER_OF_ADULTS_PLURAL_ID = R.plurals.number_of_adults_TEMPLATE;
	private static final int NUMBER_OF_CHILDREN_PLURAL_ID = R.plurals.number_of_children;

	// Object access

	public static ViewInteraction searchButton() {
		return onView(withId(SEARCH_BUTTON_ID));
	}

	public static void incrementChildrenButton() {
		onView(((withId(CHILD_PICKER_VIEW_ID)))).perform(increment());
	}

	public static void incrementAdultsButton() {
		onView(((withId(ADULT_PICKER_VIEW_ID)))).perform(increment());
	}

	public static void decrementChildrenButton() {
		onView(((withId(CHILD_PICKER_VIEW_ID)))).perform(decrement());
	}

	public static void decrementAdultsButton() {
		onView(((withId(ADULT_PICKER_VIEW_ID)))).perform(decrement());
	}

	public static String selectChildAgePlural(int quantity) {
		return mRes.getQuantityString(SELECT_CHILD_AGE_PLURAL_ID, quantity);
	}

	public static String childPickerStringPlural(int numberOfChildren) {
		return mRes.getQuantityString(NUMBER_OF_CHILDREN_PLURAL_ID, numberOfChildren, numberOfChildren);
	}

	public static String adultPickerStringPlural(int numberOfAdults) {
		return mRes.getQuantityString(NUMBER_OF_ADULTS_PLURAL_ID, numberOfAdults, numberOfAdults);
	}

	public static ViewInteraction adultPicker() {
		return onView(withId(ADULT_PICKER_VIEW_ID));
	}

	public static ViewInteraction childrenPicker() {
		return onView(withId(CHILD_PICKER_VIEW_ID));
	}

	// Object interaction

	public void clickOnSearchButton() {
		(searchButton()).perform(click());
	}

	public void clickIncrementChildrenButton() {
		incrementChildrenButton();
	}

	public static void clickDecrementAdultsButton() {
		decrementAdultsButton();
	}

	public static void clickDecrementChildrenButton() {
		decrementChildrenButton();
	}

	public void clickIncrementAdultsButton() {
		incrementAdultsButton();
	}

}
