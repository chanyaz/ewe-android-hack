package com.expedia.bookings.test.tests.pageModels.flights;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

public class FlightLegScreen extends ScreenActions {

	private static final int SELECT_FLIGHT_BUTTON_ID = R.id.select_text_view;
	private static final int LEFT_TEXT_VIEW = R.id.left_text_view;
	private static final int CANCEL_BUTTON_ID = R.id.cancel_button;
	private static final int BAGGAGE_FEE_INFO_VIEW_ID = R.id.fees_text_view;
	private static final int CHECKING_PRICE_CHANGES_VIEW_ID = R.string.loading_flight_details;

	public FlightLegScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public View selectFlightButton() {
		return getView(SELECT_FLIGHT_BUTTON_ID);
	}

	public View cancelButton() {
		return getView(CANCEL_BUTTON_ID);
	}

	public TextView baggageFeeInfoTextView() {
		return (TextView) getView(BAGGAGE_FEE_INFO_VIEW_ID);
	}

	public String checkingForPriceChangesString() {
		return mRes.getString(CHECKING_PRICE_CHANGES_VIEW_ID);
	}

	public TextView durationTextView() {
		return (TextView) getView(LEFT_TEXT_VIEW);
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
