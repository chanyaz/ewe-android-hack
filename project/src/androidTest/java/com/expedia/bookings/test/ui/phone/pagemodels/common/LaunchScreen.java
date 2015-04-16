package com.expedia.bookings.test.ui.phone.pagemodels.common;

import android.content.res.Resources;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class LaunchScreen extends LaunchActionBar {

	public static ViewInteraction hotelLaunchWithContentDescription(Resources res) {
		return onView(withContentDescription(res.getString(R.string.cd_launch_hotels)));
	}

	public static ViewInteraction flightLaunchWithContentDescription(Resources res) {
		return onView(withContentDescription(res.getString(R.string.cd_launch_flights)));
	}

	public static ViewInteraction hotelLaunchButton() {
		return onView(allOf(withId(R.id.hotels_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static ViewInteraction flightLaunchButton() {
		return onView(allOf(withId(R.id.flights_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static ViewInteraction carLaunchButton() {
		return onView(allOf(withId(R.id.cars_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static ViewInteraction tripsButton() {
		return onView(withText(R.string.Your_Trips));

	}

	public static void launchHotels() {
		hotelLaunchButton().perform(click());
	}

	public static void launchFlights() {
		flightLaunchButton().perform(click());
	}

	public static void launchCars() {
		carLaunchButton().perform(click());
	}
}

