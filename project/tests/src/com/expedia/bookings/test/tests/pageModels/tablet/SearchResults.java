package com.expedia.bookings.test.tests.pageModels.tablet;

import com.expedia.bookings.R;
import com.expedia.bookings.section.HotelSummarySection;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.expedia.bookings.test.utils.EspressoUtils.swipeUp;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withContentDescription;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

public class SearchResults {

	public static ViewInteraction hotelList() {
		return onView(withContentDescription("Hotel Search Results"));
	}

	public static ViewInteraction flightList() {
		return onView(withContentDescription("Flight Search Results"));
	}

	public static void swipeUpHotelList() {
		hotelList().perform(swipeUp());
	}

	public static ViewInteraction sortAndFilterButton() {
		return onView(allOf(withText(R.string.Sort_and_Filter), isCompletelyDisplayed()));
	}

	public static void swipeUpFlightList() {
		flightList().perform(swipeUp());
	}

	public static ViewInteraction actionUpButton() {
		return onView(withId(android.R.id.home));
	}

	public static TripBucket tripBucket() {
		return TripBucket.getInstance();
	}

	public static DataInteraction getHotelFromList(int i) {
		return onData(not(is(HotelSummarySection.class))).inAdapterView(withContentDescription("Hotel Search Results")).atPosition(i);
	}
}
