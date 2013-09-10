package com.expedia.bookings.test.tests.pageModels.flights;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;
import com.mobiata.testutils.CalendarTouchUtils;

public class FlightsSearchScreen extends ScreenActions {

	private static final int sDepartureEditTextID = R.id.departure_airport_edit_text;
	private static final int sArrivalEditTextID = R.id.arrival_airport_edit_text;
	private static final int sSelectDepartureViewID = R.id.dates_button;
	private static final int sPassengerSelectionButtonID = R.id.num_travelers_button;
	private static final int sCalendarDatePickerID = R.id.calendar_date_picker;
	private static final int sSearchButtonID = R.id.search_button;

	public FlightsSearchScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	//Object retrievers

	public EditText departureEditText() {
		return (EditText) getView(sDepartureEditTextID);
	}

	public EditText arrivalEditText() {
		return (EditText) getView(sArrivalEditTextID);
	}

	public View selectDepartureButton() {
		return getView(sSelectDepartureViewID);
	}

	public View passengerSelectionButton() {
		return getView(sPassengerSelectionButtonID);
	}

	public View searchButton() {
		return getView(sSearchButtonID);
	}

	// Object interactions 

	public void enterDepartureAirport(String text) {
		typeText(departureEditText(), text);
	}

	public void enterArrivalAirport(String text) {
		typeText(arrivalEditText(), text);
	}

	public void clickDepartureAirportField() {
		clickOnView(departureEditText());
	}

	public void clickArrivalAirportField() {
		clickOnView(arrivalEditText());
	}

	public void clearDepartureAirportField() {
		clearEditText(departureEditText());
	}

	public void clearArrivalAirportField() {
		clearEditText(arrivalEditText());
	}

	public void clickSelectDepartureButton() {
		clickOnView(selectDepartureButton());
	}

	public void clickPassengerSelectionButton() {
		clickOnView(passengerSelectionButton());
	}

	public void clickSearchButton() {
		clickOnView(searchButton());
	}

	public void clickDate(int offset) {
		delay();
		CalendarTouchUtils.selectDay(this, offset, sCalendarDatePickerID);
	}

}
