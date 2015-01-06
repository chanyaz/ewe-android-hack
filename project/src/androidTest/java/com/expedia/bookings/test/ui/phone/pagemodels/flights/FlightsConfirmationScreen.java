package com.expedia.bookings.test.ui.phone.pagemodels.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ConfirmationScreen;
import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


/**
 * Created by dmadan on 4/7/14.
 */
public class FlightsConfirmationScreen extends ConfirmationScreen {
	private static final int GET_A_ROOM_TEXT_VIEW_ID = R.id.hotels_action_text_view;
	private static final int FLIGHT_TRACK_ACTION_VIEW_ID = R.id.flighttrack_action_text_view;

	// Object access

	public static ViewInteraction getARoomTextView() {
		return onView(withId(GET_A_ROOM_TEXT_VIEW_ID));
	}

	public static ViewInteraction trackWithFlightTrackTextView() {
		return onView(withId(FLIGHT_TRACK_ACTION_VIEW_ID));
	}

	// Object interaction

	public static void clickOnGetARoomInTextView() {
		getARoomTextView().perform(click());
	}

	public static void clickOnTrackWithFlightTrackTextView() {
		trackWithFlightTrackTextView().perform(click());
	}
}
