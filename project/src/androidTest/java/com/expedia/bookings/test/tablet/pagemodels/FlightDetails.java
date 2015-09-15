package com.expedia.bookings.test.tablet.pagemodels;

import com.expedia.bookings.R;
import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 7/16/14.
 */
public class FlightDetails {

	public static ViewInteraction flightName() {
		return onView(withId(R.id.airline_and_cities_text_view));
	}

	public static ViewInteraction flightTime() {
		return onView(withId(R.id.details_time_header));
	}

	public static ViewInteraction flightPrice() {
		return onView(withId(R.id.details_price_text_view));
	}

	public static ViewInteraction flightArrivalTime() {
		return onView(withId(R.id.arrival_time_text_view));
	}

	public static ViewInteraction flightDepartureTime() {
		return onView(withId(R.id.departure_time_text_view));
	}
}
