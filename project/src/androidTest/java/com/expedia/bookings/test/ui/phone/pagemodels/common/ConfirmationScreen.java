package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.expedia.bookings.R;

import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.action.ViewActions.click;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/7/14.
 */
public class ConfirmationScreen extends ScreenActions {
	private static final int DONE_BUTTON_ID = R.id.menu_done;

	// Object access

	public static ViewInteraction doneButton() {
		return onView(withId(DONE_BUTTON_ID));
	}

	// Object interaction

	public static void clickDoneButton() {
		doneButton().perform(click());
	}
}
