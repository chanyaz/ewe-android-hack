package com.expedia.bookings.test.ui.tablet.pagemodels;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 7/14/14.
 */
public class Confirmation {

	public static ViewInteraction confirmationSummary() {
		return onView(withId(R.id.confirmation_summary_text));
	}

	public static ViewInteraction confirmationItinerary() {
		return onView(withId(R.id.confirmation_itinerary_text_view));
	}
}
