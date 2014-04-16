package com.expedia.bookings.test.tests.pageModelsEspresso.common;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.section.HotelSummarySection;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withContentDescription;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by dmadan on 4/7/14.
 */
public class LaunchScreen extends LaunchActionBar {
	private static final int sHotelsButtonID = R.id.hotels_button;
	private static final int sFlightsButtonID = R.id.flights_button;
	public LaunchActionBar mLaunchActionBar;

	public static ViewInteraction hotelLaunchButton() {
		return onView(withId(sHotelsButtonID));
	}

	public static ViewInteraction flightLaunchButton() {
		return onView(withId(sFlightsButtonID));
	}

	public static void launchHotels() {
		hotelLaunchButton().perform(click());
	}

	public static void launchFlights() {
		flightLaunchButton().perform(click());
	}

}

