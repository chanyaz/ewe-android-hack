package com.expedia.bookings.test.ui.phone.pagemodels.common;

import android.content.res.Resources;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class LaunchScreen extends LaunchActionBar {

	public static ViewInteraction hotelLaunchWithContentDescription(Resources res) {
		return onView(withContentDescription(res.getString(R.string.cd_launch_hotels)));
	}

	public static ViewInteraction flightLaunchWithContentDescription(Resources res) {
		return onView(withContentDescription(res.getString(R.string.cd_launch_flights)));
	}

	public static ViewInteraction hotelLaunchButton() {
		return onView(withId(R.id.hotels_button));
	}

	public static ViewInteraction flightLaunchButton() {
		return onView(withId(R.id.flights_button));
	}

	public static ViewInteraction tripsButton() {
		return onView(withText(R.string.trips));

	}

	public static void launchHotels() {
		hotelLaunchButton().perform(click());
	}

	public static void launchFlights() {
		flightLaunchButton().perform(click());
	}
}

