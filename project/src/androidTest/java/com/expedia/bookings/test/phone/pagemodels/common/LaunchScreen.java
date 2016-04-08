package com.expedia.bookings.test.phone.pagemodels.common;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class LaunchScreen {

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

	public static ViewInteraction groundTransportLaunchButton() {
		return onView(allOf(withId(R.id.transport_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static ViewInteraction tripsButton() {
		return onView(withText(R.string.trips));
	}

	public static ViewInteraction shopButton() {
		return onView(withText(R.string.shop));
	}

	public static ViewInteraction accountButton() {
		return onView(withText(R.string.account_settings_menu_label));
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

	public static void launchGroundTransport() {
		groundTransportLaunchButton().perform(click());
	}

	public static void clickOnAirAttachBanner() {
		onView(withId(R.id.air_attach_banner)).perform(click());
	}

	public static ViewInteraction carLaunchButtonInDoubleRow() {
		return onView(allOf(withId(R.id.cars_button), isDescendantOfA(withId(R.id.double_row_lob_selector))));
	}

	public static ViewInteraction lxLaunchButtonInDoubleRow() {
		return onView(allOf(withId(R.id.activities_button), isDescendantOfA(withId(R.id.double_row_lob_selector))));
	}

	public static ViewInteraction carLaunchButtonInSingleRow() {
		return onView(allOf(withId(R.id.cars_button), isDescendantOfA(withId(R.id.lob_selector))));
	}

	public static ViewInteraction lxLaunchButtonInSingleRow() {
		return onView(allOf(withId(R.id.activities_button), isDescendantOfA(withId(R.id.lob_selector))));
	}

	public static ViewInteraction lobSingleRowWidget() {
		return onView(withId(R.id.lob_selector));
	}

	public static ViewInteraction lobDoubleRowWidget() {
		return onView(withId(R.id.double_row_lob_selector));
	}

	public static ViewInteraction fiveLOBDoubleRowWidget() {
		return onView(withId(R.id.double_row_five_lob_selector));
	}
}

