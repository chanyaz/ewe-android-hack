package com.expedia.bookings.test.ui.phone.pagemodels.common;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class LaunchScreen extends LaunchActionBar {

	public static ViewInteraction hotelLaunchButton() {
		return onView(allOf(withId(R.id.hotels_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static ViewInteraction flightLaunchButton() {
		return onView(allOf(withId(R.id.flights_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static ViewInteraction carLaunchButton() {
		return onView(allOf(withId(R.id.cars_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static ViewInteraction lxLaunchButton() {
		return onView(allOf(withId(R.id.activities_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static ViewInteraction tripsButton() {
		return onView(withText(R.string.Your_Trips));
	}

	public static ViewInteraction shopButton() {
		return onView(withText(R.string.shop));
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

	public static void launchActivities() {
		lxLaunchButton().perform(click());
	}

	public static ViewInteraction carLaunchButtonInDoubleRow() {
		return onView(allOf(withId(R.id.cars_button), isDescendantOfA(withId(R.id.double_row_lob_selector))));
	}

	public static ViewInteraction lxLaunchButtonInDoubleRow() {
		return onView(allOf(withId(R.id.activities_button), isDescendantOfA(withId(R.id.double_row_lob_selector))));
	}

	public static void checkCarsButtonNotDisplayed() {
		carLaunchButtonInDoubleRow().check(matches(not(isDisplayed())));
	}

	public static void checkLXButtonNotDisplayed() {
		lxLaunchButtonInDoubleRow().check(matches(not(isDisplayed())));
	}
}

