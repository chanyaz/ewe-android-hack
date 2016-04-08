package com.expedia.bookings.test.espresso;

import android.support.annotation.IdRes;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class EspressoUser {
	public static void clickOnView(@IdRes int viewId) {
		onView(withId(viewId)).perform(click());
	}

	public static void clickOnText(String text) {
		onView(withText(text)).perform(click());
	}
}
