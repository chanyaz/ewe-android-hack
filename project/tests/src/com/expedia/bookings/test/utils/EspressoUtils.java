package com.expedia.bookings.test.utils;

import java.util.concurrent.atomic.AtomicReference;

import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.ViewAction;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.android.apps.common.testing.ui.espresso.action.GeneralLocation;
import com.google.android.apps.common.testing.ui.espresso.action.GeneralSwipeAction;
import com.google.android.apps.common.testing.ui.espresso.action.Press;
import com.google.android.apps.common.testing.ui.espresso.action.Swipe;

import static com.expedia.bookings.test.utilsEspresso.ViewActions.getChildCount;
import static com.expedia.bookings.test.utilsEspresso.ViewActions.getCount;
import static com.expedia.bookings.test.utilsEspresso.ViewActions.getRating;
import static com.expedia.bookings.test.utilsEspresso.ViewActions.getString;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasSibling;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class EspressoUtils {

	public static ViewAction swipeUp() {
		return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER,
			GeneralLocation.TOP_CENTER, Press.FINGER);
	}

	public static ViewAction slowSwipeUp() {
		return new GeneralSwipeAction(Swipe.SLOW, GeneralLocation.BOTTOM_CENTER,
			GeneralLocation.TOP_CENTER, Press.FINGER);
	}

	public static ViewAction swipeDown() {
		return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.TOP_CENTER,
			GeneralLocation.BOTTOM_CENTER, Press.FINGER);
	}

	public static ViewAction swipeRight() {
		return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER_LEFT,
			GeneralLocation.CENTER_RIGHT, Press.FINGER);
	}

	public static void assertTrue(String text) {
		onView(withText(text)).check(matches(isDisplayed()));
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
}
