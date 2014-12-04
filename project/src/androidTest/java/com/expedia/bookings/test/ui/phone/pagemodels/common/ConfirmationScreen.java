package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

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
