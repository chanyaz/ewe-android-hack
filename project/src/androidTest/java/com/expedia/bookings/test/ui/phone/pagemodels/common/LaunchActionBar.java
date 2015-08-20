package com.expedia.bookings.test.ui.phone.pagemodels.common;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressMenuKey;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class LaunchActionBar {

	public static ViewInteraction settingsString() {
		return onView(withText(R.string.Settings));
	}

	public static ViewInteraction tripsString() {
		return onView(withText(R.string.Your_Trips));
	}

	/*
	 * ActionBar tabs
	 * They don't have IDs, so we must reference by string ID
	 */

	public static void pressTrips() {
		tripsString().perform(click());
	}

	/*
	 * Menu interactions
	 */

	public static void openMenuDropDown() {
		onView(isRoot()).perform(pressMenuKey());
	}

	public static void pressSettings() {
		settingsString().perform(click());
	}

}
