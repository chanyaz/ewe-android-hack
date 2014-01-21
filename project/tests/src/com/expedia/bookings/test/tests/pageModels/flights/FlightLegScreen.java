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
	private static final int LEFT_TEXT_VIEW_ID = R.id.left_text_view;
	private static final int RIGHT_TEXT_VIEW_ID = R.id.right_text_view;
	private static final int AIRLINE_TEXT_VIEW_ID = R.id.airline_text_view;
	private static final int DEPARTURE_TIME_TEXT_VIEW_ID = R.id.departure_time_text_view;
	private static final int ARRIVAL_TIME_TEXT_VIEW_ID = R.id.arrival_time_text_view;
	private static final int PRICE_TEXT_VIEW_ID = R.id.price_text_view;
	private static final int CANCEL_BUTTON_ID = R.id.cancel_button;
	private static final int BAGGAGE_FEE_INFO_VIEW_ID = R.id.fees_text_view;
	private static final int CHECKING_PRICE_CHANGES_VIEW_ID = R.string.loading_flight_details;
	private static final int BAGGAGE_FEES_STRING_ID = R.string.baggage_fees;
	private static final int DETAILS_TEXT_VIEW_ID = R.id.details_text_view;

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
		return (TextView) getView(LEFT_TEXT_VIEW_ID);
	}

	public TextView rightHeaderView() {
		return (TextView) getView(RIGHT_TEXT_VIEW_ID);
	}

	public TextView airlineTextView() {
		return (TextView) getView(AIRLINE_TEXT_VIEW_ID);
	}

	public TextView departureTimeTextView() {
		return (TextView) getView(DEPARTURE_TIME_TEXT_VIEW_ID);
	}

	public TextView arrivalTimeTextView() {
		return (TextView) getView(ARRIVAL_TIME_TEXT_VIEW_ID);
	}

	public TextView priceTextView() {
		return (TextView) getView(PRICE_TEXT_VIEW_ID);
	}

	public String baggageFees() {
		return getString(BAGGAGE_FEES_STRING_ID);
	}

	public TextView detailsTextView() {
		return (TextView) getView(DETAILS_TEXT_VIEW_ID);
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
