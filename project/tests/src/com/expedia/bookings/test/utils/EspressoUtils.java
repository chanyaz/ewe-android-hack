package com.expedia.bookings.test.utils;

import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.ViewAction;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.android.apps.common.testing.ui.espresso.action.GeneralLocation;
import com.google.android.apps.common.testing.ui.espresso.action.GeneralSwipeAction;
import com.google.android.apps.common.testing.ui.espresso.action.Press;
import com.google.android.apps.common.testing.ui.espresso.action.Swipe;

import static com.expedia.bookings.test.utilsEspresso.ViewActions.getCount;
import static com.expedia.bookings.test.utilsEspresso.ViewActions.storeValue;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

public class EspressoUtils {

	public static ViewAction swipeUp() {
		return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER,
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

	public static void getValues(String value, int id) {
		onView(withId(id)).perform(storeValue(value));
	}

	public static void getListItemValues(DataInteraction row, int id, String value) {
		row.onChildView(withId(id)).perform(storeValue(value));
	}

	public static void getListCount(ViewInteraction view, String key, int code) {
		view.perform(getCount(key, code));
	}
}
