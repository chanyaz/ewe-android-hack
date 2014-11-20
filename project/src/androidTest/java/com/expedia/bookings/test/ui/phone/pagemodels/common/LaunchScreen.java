package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withContentDescription;

/**
 * Created by dmadan on 4/7/14.
 */
public class LaunchScreen extends LaunchActionBar {

	public static ViewInteraction hotelLaunchButton() {
		return onView(withContentDescription("Launch Hotels"));
	}

	public static ViewInteraction flightLaunchButton() {
		return onView(withContentDescription("Launch Flights"));
	}

	public static void launchHotels() {
		hotelLaunchButton().perform(click());
	}

	public static void launchFlights() {
		flightLaunchButton().perform(click());
	}
}

