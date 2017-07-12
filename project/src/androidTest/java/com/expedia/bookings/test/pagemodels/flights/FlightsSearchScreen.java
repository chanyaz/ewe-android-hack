package com.expedia.bookings.test.pagemodels.flights;

import java.util.concurrent.atomic.AtomicReference;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.ViewActions.getString;

public class FlightsSearchScreen {

	public static ViewInteraction searchButton() {
		return onView(withId(R.id.search_button));
	}


	public static String getAdultTravelerNumberText() {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withId(R.id.adult)).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}

	public static String getChildTravelerNumberText() {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withId(R.id.children)).perform(getString(value));
		String stringValue = value.get();
		return stringValue;
	}
}
