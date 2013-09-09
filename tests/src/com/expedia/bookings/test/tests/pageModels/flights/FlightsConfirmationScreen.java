package com.expedia.bookings.test.tests.pageModels.flights;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ConfirmationScreen;
import com.expedia.bookings.test.utils.TestPreferences;
import com.expedia.bookings.widget.TextView;

public class FlightsConfirmationScreen extends ConfirmationScreen {

	private static final int sGoingToTextViewID = R.id.going_to_text_view;
	private static final int sGetARoomTextViewID = R.id.hotels_action_text_view;
	private static final int sFlightTrackActionViewID = R.id.flighttrack_action_text_view;

	public FlightsConfirmationScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public TextView goingToTextView() {
		return (TextView) getView(sGoingToTextViewID);
	}

	public TextView getARoomTextView() {
		return (TextView) getView(sGetARoomTextViewID);
	}

	public TextView trackWithFlightTrackTextView() {
		return (TextView) getView(sFlightTrackActionViewID);
	}

	// Object interaction

	public void clickOnGetARoomInTextView() {
		clickOnView(getARoomTextView());
	}

	public void clickOnTrackWithFlightTrackTextView() {
		clickOnView(trackWithFlightTrackTextView());
	}
}
