package com.expedia.bookings.test.tests.pageModels.flights;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;
import com.expedia.bookings.widget.TextView;

public class FlightLegScreen extends ScreenActions {

	private static final int sSelectFlightButton = R.id.select_text_view;
	private static final int sCancelButton = R.id.cancel_button;
	private static final int sBaggageFeeInfoTextViewID = R.id.fees_text_view;
	private static final int sCheckingForPriceChangesStringID = R.string.loading_flight_details;

	public FlightLegScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public View selectFlightButton() {
		return getView(sSelectFlightButton);
	}

	public View cancelButton() {
		return getView(sCancelButton);
	}

	public TextView baggageFeeInfoTextView() {
		return (TextView) getView(sBaggageFeeInfoTextViewID);
	}

	public String checkingForPriceChangesString() {
		return mRes.getString(sCheckingForPriceChangesStringID);
	}

	// Object interaction

	public void clickSelectFlightButton() {
		clickOnView(selectFlightButton());
	}

	public void clickCancelButton() {
		clickOnView(cancelButton());
	}

	public void clickBaggageInfoView() {
		clickOnView(baggageFeeInfoTextView());
	}
}
