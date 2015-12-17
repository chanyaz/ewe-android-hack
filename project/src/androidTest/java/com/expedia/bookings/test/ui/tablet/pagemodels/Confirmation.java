package com.expedia.bookings.test.ui.tablet.pagemodels;

import com.expedia.bookings.R;
import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

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
