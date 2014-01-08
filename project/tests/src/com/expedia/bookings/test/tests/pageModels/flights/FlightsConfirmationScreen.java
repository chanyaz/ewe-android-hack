package com.expedia.bookings.test.tests.pageModels.flights;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ConfirmationScreen;
import com.expedia.bookings.test.utils.TestPreferences;
import com.expedia.bookings.widget.TextView;

public class FlightsConfirmationScreen extends ConfirmationScreen {

	private static final int GOING_TO_TEXT_VIEW_ID = R.id.going_to_text_view;
	private static final int GET_A_ROOM_TEXT_VIEW_ID = R.id.hotels_action_text_view;
	private static final int FLIGHT_TRACK_ACTION_VIEW_ID = R.id.flighttrack_action_text_view;

	public FlightsConfirmationScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public TextView goingToTextView() {
		return (TextView) getView(GOING_TO_TEXT_VIEW_ID);
	}

	public TextView getARoomTextView() {
		return (TextView) getView(GET_A_ROOM_TEXT_VIEW_ID);
	}

	public TextView trackWithFlightTrackTextView() {
		return (TextView) getView(FLIGHT_TRACK_ACTION_VIEW_ID);
	}

	// Object interaction

	public void clickOnGetARoomInTextView() {
		clickOnView(getARoomTextView());
	}

	public void clickOnTrackWithFlightTrackTextView() {
		clickOnView(trackWithFlightTrackTextView());
	}
}
