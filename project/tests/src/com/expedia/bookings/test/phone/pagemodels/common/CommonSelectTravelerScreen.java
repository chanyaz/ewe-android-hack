package com.expedia.bookings.test.phone.pagemodels.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/7/14.
 */
public class CommonSelectTravelerScreen extends ScreenActions {
	private static final int sEnterANewTraveler = R.id.enter_info_manually_button;

	// Object access
	public static ViewInteraction enterInfoManuallyButton() {
		return onView(withId(sEnterANewTraveler));
	}

	// Object interaction
	public static void clickEnterInfoManuallyButton() {
		enterInfoManuallyButton().perform(click());
	}

}
