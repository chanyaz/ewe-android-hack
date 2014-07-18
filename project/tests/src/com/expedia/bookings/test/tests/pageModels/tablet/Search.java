package com.expedia.bookings.test.tests.pageModels.tablet;

import android.app.Activity;

import com.expedia.bookings.R;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.matcher.RootMatchers.withDecorView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by dmadan on 7/18/14.
 */
public class Search {

	public static void incrementChildButton() {
		onView(withId(R.id.children_plus)).perform(click());
	}

	public static void decrementChildButton() {
		onView(withId(R.id.children_minus)).perform(click());
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

	public static void scrollToInfantAlert() {
		onView(withId(R.id.tablet_lap_infant_alert)).perform(scrollTo());
	}
}
