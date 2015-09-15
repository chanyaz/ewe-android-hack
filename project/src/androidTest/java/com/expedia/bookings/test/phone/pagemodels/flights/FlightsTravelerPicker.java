package com.expedia.bookings.test.phone.pagemodels.flights;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.StrUtils;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class FlightsTravelerPicker {

	//Object retrievers

	public static ViewInteraction adultIncrementButton() {
		return onView(withId(R.id.adults_plus));
	}

	public static ViewInteraction adultDecrementButton() {
		return onView(withId(R.id.adults_minus));
	}

	public static ViewInteraction childIncrementButton() {
		return onView(withId(R.id.children_plus));
	}

	public static ViewInteraction childDecrementButton() {
		return onView(withId(R.id.children_minus));
	}

	public static ViewInteraction refinementInfoTextView() {
		return onView(withId(R.id.refinement_info_text_view));
	}

	public static ViewInteraction child1Spinner() {
		return onView(withId(R.id.child_1_age_layout));
	}

	public static ViewInteraction child2Spinner() {
		return onView(withId(R.id.child_2_age_layout));
	}

	public static ViewInteraction child3Spinner() {
		return onView(withId(R.id.child_3_age_layout));
	}

	public static ViewInteraction child4Spinner() {
		return onView(withId(R.id.child_4_age_layout));
	}

	public static ViewInteraction infantInLapRadioButton() {
		return onView(withId(R.id.infant_in_lap));
	}

	public static ViewInteraction infantInSeatRadioButton() {
		return onView(withId(R.id.infant_in_seat));
	}

	public static ViewInteraction adultCountTextView() {
		return onView(withId(R.id.adult_count_text));
	}

	public static ViewInteraction childCountTextView() {
		return onView(withId(R.id.child_count_text));
	}

	public static ViewInteraction infantSeatingLayout() {
		return onView(withId(R.id.infant_preference_seating_layout));
	}

	public static ViewInteraction infantAlertTextView() {
		return onView(withId(R.id.infant_alert_text_view));
	}

	// Object interaction

	public static void incrementAdultCount() {
		adultIncrementButton().perform(click());
	}

	public static void decrementAdultCount() {
		adultDecrementButton().perform(click());
	}

	public static void incrementChildCount() {
		childIncrementButton().perform(click());
	}

	public static void decrementChildCount() {
		childDecrementButton().perform(click());
	}

	public static ViewInteraction childSpinner(int number) {
		ViewInteraction spinner = null;
		switch (number) {
		case 1:
			spinner = child1Spinner();
			break;
		case 2:
			spinner = child2Spinner();
			break;
		case 3:
			spinner = child3Spinner();
			break;
		case 4:
			spinner = child4Spinner();
			break;
		}
		return spinner;
	}

	public static String childPickerStringPlural(int numberOfChildren, Resources res) {
		return res.getQuantityString(R.plurals.number_of_children, numberOfChildren, numberOfChildren);
	}

	public static String adultPickerStringPlural(int numberOfAdults, Resources res) {
		return res.getQuantityString(R.plurals.number_of_adults, numberOfAdults, numberOfAdults);
	}

	public static void adultIndicatorTextMatches(int numberOfAdults, Resources res) {
		adultCountTextView().check(matches(withText(adultPickerStringPlural(numberOfAdults, res))));
	}

	public static void childrenIndicatorTextMatches(int numberOfChildren, Resources res) {
		childCountTextView().check(matches(withText(childPickerStringPlural(numberOfChildren, res))));
	}

	public static void refinementInfoTextMatches(int numberOfAdults, int numberOfChildren, Context context) {
		adultIndicatorTextMatches(numberOfAdults, context.getResources());
		childrenIndicatorTextMatches(numberOfChildren, context.getResources());
		refinementInfoTextView().check(matches(withText(StrUtils.formatGuests(context, numberOfAdults, numberOfChildren))));
	}

	public static void checkChildAgeOptions(int childNumber, Activity activity) {
		childSpinner(childNumber).perform(click());
		for (int i = 0; i < 18; i++) {
			onData(anything()).inRoot(withDecorView(not(is(activity.getWindow().getDecorView())))).atPosition(i).check(matches(withText(StrUtils.getChildTravelerAgeText(activity.getResources(), i))));
		}
	}

	public static void selectChildAge(Activity activity, int childNumber, int position) {
		childSpinner(childNumber).perform(click());
		onData(anything()).inRoot(withDecorView(not(is(activity.getWindow().getDecorView())))).atPosition(position).perform(click());
	}

	public static void isInfantInLapChecked(boolean checked) {
		isInfantSeatingLayoutVisible(true);
		if (checked) {
			isInfantInLapEnabled(true);
			infantInLapRadioButton().check(matches(isChecked()));
		}
		else {
			infantInLapRadioButton().check(matches(not(isChecked())));
		}
	}

	public static void isInfantInLapEnabled(boolean enabled) {
		if (enabled) {
			infantInLapRadioButton().check(matches(isEnabled()));
		}
		else {
			infantInLapRadioButton().check(matches(not(isEnabled())));
		}
	}

	public static void isInfantInSeatChecked(boolean checked) {
		isInfantSeatingLayoutVisible(true);
		if (checked) {
			isInfantInSeatEnabled(true);
			infantInSeatRadioButton().check(matches(isChecked()));
		}
		else {
			infantInSeatRadioButton().check(matches(not(isChecked())));
		}
	}

	public static void isInfantInSeatEnabled(boolean enabled) {
		if (enabled) {
			infantInSeatRadioButton().check(matches(isEnabled()));
		}
		else {
			infantInSeatRadioButton().check(matches(not(isEnabled())));
		}
	}

	public static void isInfantSeatingLayoutVisible(boolean visible) {
		if (visible) {
			infantSeatingLayout().check(matches(isDisplayed()));
		}
		else {
			infantSeatingLayout().check(matches(not(isDisplayed())));
		}

	}

	public static void isInfantAlertShown(boolean shown) {
		isInfantInLapEnabled(!shown);
		if (shown) {
			isInfantInSeatChecked(true);
			infantAlertTextView().check(matches(isDisplayed()));
		}
		else {
			infantAlertTextView().check(matches(not(isDisplayed())));
		}
	}

	public static void isAdultButtonIncrementDisabled() {
		adultIncrementButton().check(matches(not(isEnabled())));
	}

	public static void isChildIncrementButtonIsDisabled() {
		childIncrementButton().check(matches(not(isEnabled())));
	}

	public static void isAdultButtonDecrementDisabled() {
		adultDecrementButton().check(matches(not(isEnabled())));
	}

	public static void isChildDecrementButtonIsDisabled() {
		childDecrementButton().check(matches(not(isEnabled())));
	}

}
