package com.expedia.bookings.test.ui.utils;

import java.util.concurrent.atomic.AtomicReference;

import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.expedia.bookings.test.ui.espresso.CustomMatchers.withImageDrawable;
import static com.expedia.bookings.test.ui.espresso.ViewActions.getChildCount;
import static com.expedia.bookings.test.ui.espresso.ViewActions.getCount;
import static com.expedia.bookings.test.ui.espresso.ViewActions.getRating;
import static com.expedia.bookings.test.ui.espresso.ViewActions.getString;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasSibling;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

public class EspressoUtils {

	public static void clear(ViewInteraction view) {
		view.perform(clearText());
	}

	public static void assertViewWithTextIsDisplayed(String text) {
		onView(withText(text)).check(matches(isDisplayed()));
	}

	public static void assertViewIsDisplayed(int id) {
		onView(withId(id)).check(matches(isDisplayed()));
	}

	public static void assertViewWithSubstringIsDisplayed(String substring) {
		onView(withText(containsString(substring))).check(matches(isDisplayed()));
	}

	public static void assertContains(ViewInteraction view, String str) {
		view.check(matches(withText(containsString(str))));
	}

	public static String getText(int id) {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withId(id)).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}

	// to avoid multiple matches to a view
	public static String getTextWithSibling(int id, int siblingId) {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(allOf(withId(id), hasSibling(withId(siblingId)), isDisplayed())).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}

	public static String getListItemValues(DataInteraction row, int id) {
		final AtomicReference<String> value = new AtomicReference<String>();
		row.onChildView(withId(id)).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}

	public static int getListCount(ViewInteraction view) {
		final AtomicReference<Integer> count = new AtomicReference<Integer>();
		view.perform(getCount(count));
		int numberCount = count.get();
		return numberCount;
	}

	public static int getListChildCount(ViewInteraction view) {
		final AtomicReference<Integer> count = new AtomicReference<Integer>();
		view.perform(getChildCount(count));
		int numberCount = count.get();
		return numberCount;
	}

	public static float getRatingValue(ViewInteraction view) {
		final AtomicReference<Float> rating = new AtomicReference<Float>();
		view.perform(getRating(rating));
		float ratingValue = rating.get();
		return ratingValue;
	}

	public static void assertContainsImageDrawable(int viewID, int imageID) {
		onView(allOf(withId(viewID), isDisplayed())).check(matches(withImageDrawable(imageID)));
	}

	public static void assertContainsImageDrawable(int viewID, int imageID, int siblingID) {
		onView(allOf(withId(viewID), hasSibling(withId(siblingID)), isDisplayed())).check(matches(withImageDrawable(imageID)));
	}
}
