package com.expedia.bookings.test.espresso;

import java.util.concurrent.atomic.AtomicReference;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;

import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable;
import static com.expedia.bookings.test.espresso.ViewActions.getChildCount;
import static com.expedia.bookings.test.espresso.ViewActions.getCount;
import static com.expedia.bookings.test.espresso.ViewActions.getRating;
import static com.expedia.bookings.test.espresso.ViewActions.getStarRating;
import static com.expedia.bookings.test.espresso.ViewActions.getString;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class EspressoUtils {

	public static void assertViewWithTextIsDisplayed(String text) {
		onView(withText(text)).check(matches(isDisplayed()));
	}

	public static void assertViewWithTextIsDisplayed(int id, String text) {
		onView(allOf(withId(id), withText(text))).check(matches(isDisplayed()));
	}

	public static void assertViewWithTextIsNotDisplayed(int id, String text) {
		onView(allOf(withId(id), withText(text))).check(matches(not(isDisplayed())));
	}

	public static void assertViewIsDisplayed(int id) {
		onView(withId(id)).check(matches(isDisplayed()));
	}

	public static void assertViewIsNotDisplayed(int id) {
		onView(withId(id)).check(matches(not(isDisplayed())));
	}

	public static void assertViewWithSubstringIsDisplayed(String substring) {
		onView(withText(containsString(substring))).check(matches(isDisplayed()));
	}

	public static void assertViewWithSubstringIsDisplayed(int id, String substring) {
		onView(allOf(withId(id), withText(containsString(substring))))
				.check(matches(isDisplayed()));
	}

	public static void assertContains(ViewInteraction view, String str) {
		view.check(matches(withText(containsString(str))));
	}

	public static void assertTextWithChildrenIsDisplayed(int id, String text) {
		onView(allOf(withId(id), withChild(withText(text)), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(
			matches(isDisplayed()));
	}

	public static void viewHasDescendantsWithText(int id, String text) {
		onView(allOf(withId(id), hasDescendant(withText(text)), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(matches(isDisplayed()));
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

	public static float getStarRatingValue(ViewInteraction view) {
		final AtomicReference<Float> rating = new AtomicReference<Float>();
		view.perform(getStarRating(rating));
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
