package com.expedia.bookings.test.tests.pageModels.flights;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.widget.CalendarDatePicker;
import com.expedia.bookings.test.utils.CalendarTouchUtils;

public class FlightsSearchScreen extends ScreenActions {

	private static final int DEPARTURE_AIRPORT_EDIT_TEXT_ID = R.id.departure_airport_edit_text;
	private static final int ARRIVAL_AIRPORT_EDIT_TEXT_ID = R.id.arrival_airport_edit_text;
	private static final int SELECT_DEPARTURE_VIEW_ID = R.id.dates_button;
	private static final int CLEAR_DATE_BUTTON_ID = R.id.clear_dates_btn;
	private static final int PASSENGER_SELECTION_BUTTON_ID = R.id.num_travelers_button;
	private static final int NUMBER_OF_PASSENGERS_TEXT_VIEW_ID = R.id.num_travelers_text_view;
	private static final int CALENDAR_DATE_PICKER_ID = R.id.calendar_date_picker;
	private static final int SEARCH_BUTTON_ID = R.id.search_button;
	private static final int DEPARTURE_ARRIVAL_SAME_ERROR_MESSAGE_ID = R.string.error_same_flight_departure_arrival;
	private static final int ADULTS_PLURAL_STRING_FORMAT_ID = R.plurals.number_of_adults_TEMPLATE;
	private static final int OK_STRING_ID = R.string.ok;

	public FlightsSearchScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	//Object retrievers

	public EditText departureEditText() {
		return (EditText) getView(DEPARTURE_AIRPORT_EDIT_TEXT_ID);
	}

	public EditText arrivalEditText() {
		return (EditText) getView(ARRIVAL_AIRPORT_EDIT_TEXT_ID);
	}

	public EditText selectDepartureButton() {
		return (EditText) getView(SELECT_DEPARTURE_VIEW_ID);
	}

	public View clearSelectedDatesButton() {
		return getView(CLEAR_DATE_BUTTON_ID);
	}

	public View passengerSelectionButton() {
		return getView(PASSENGER_SELECTION_BUTTON_ID);
	}

	public TextView passengerNumberTextView() {
		return (TextView) getView(NUMBER_OF_PASSENGERS_TEXT_VIEW_ID);
	}

	public View searchButton() {
		return getView(SEARCH_BUTTON_ID);
	}

	public String departureAndArrivalAreTheSameErrorMessage() {
		return getString(DEPARTURE_ARRIVAL_SAME_ERROR_MESSAGE_ID);
	}

	public String getAdultsQuantityString(int quantityOfAdults) {
		return mRes.getQuantityString(ADULTS_PLURAL_STRING_FORMAT_ID, quantityOfAdults, quantityOfAdults);
	}

	public CalendarDatePicker calendarDatePicker() {
		return (CalendarDatePicker) getView(CALENDAR_DATE_PICKER_ID);
	}

	public String okString() {
		return getString(OK_STRING_ID);
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

	public void clickClearSelectedDatesButton() {
		clickOnView(clearSelectedDatesButton());
	}

	public void clickPassengerSelectionButton() {
		clickOnView(passengerSelectionButton());
	}

	public void clickSearchButton() {
		clickOnView(searchButton());
	}

	public void clickDate(int offset) {
		delay();
		CalendarTouchUtils.selectDay(this, offset, CALENDAR_DATE_PICKER_ID);
	}

	public void clickDate(Time time) {
		delay();
		CalendarTouchUtils.clickOnFutureMonthDay(this, calendarDatePicker(), time);
	}

}
