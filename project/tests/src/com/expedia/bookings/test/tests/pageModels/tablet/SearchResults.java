package com.expedia.bookings.test.tests.pageModels.tablet;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withContentDescription;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static org.hamcrest.Matchers.instanceOf;
import static com.expedia.bookings.test.utils.EspressoUtils.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.allOf;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

public class SearchResults {

	public static ViewInteraction hotelList() {
		return onView(withId(R.id.trip_bucket_hotel_trip_swipeout));
	}

	public static ViewInteraction flightList() {
		return onView(withId(R.id.trip_bucket_flight_trip_swipeout));
	}

	public static void swipeUpHotelList() {
		onData(is(instanceOf(com.expedia.bookings.section.HotelSummarySection.class))).inAdapterView(allOf(
				withId(android.R.id.list), withContentDescription("Hotel List"))).atPosition(0)
				.perform(click());
	}

	public static ViewInteraction actionUpButton() {
		return onView(withId(android.R.id.home));
	}

	public static TripBucket tripBucket() {
		return TripBucket.getInstance();
	}

}
