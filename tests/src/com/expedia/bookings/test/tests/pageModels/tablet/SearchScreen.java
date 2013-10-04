package com.expedia.bookings.test.tests.pageModels.tablet;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

public class SearchScreen extends ScreenActions {

	private static final int CANCEL_BUTTON_ID = R.id.cancel_button;
	private static final int SEARCH_BUTTON_ID = R.id.search_button;
	private static final int DESINATION_EDIT_TEXT_ID = R.id.search_edit_text;
	private static final int SEARCH_DATES_TEXT_VIEW_ID = R.id.search_dates_text_view;
	private static final int ORIGIN_TEXT_EDIT_TEXT_ID = R.id.origin_edit_text;
	private static final int GUESTS_TEXT_VIEW_ID = R.id.guests_text_view;

	public SearchScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public View cancelButton() {
		return getView(CANCEL_BUTTON_ID);
	}

	public View searchButton() {
		return getView(SEARCH_BUTTON_ID);
	}

	public EditText destinationEditText() {
		return (EditText) getView(DESINATION_EDIT_TEXT_ID);
	}

	public TextView searchDatesTextView() {
		return (TextView) getView(SEARCH_DATES_TEXT_VIEW_ID);
	}

	public TextView originEditText() {
		return (TextView) getView(ORIGIN_TEXT_EDIT_TEXT_ID);
	}

	public TextView guestsTextView() {
		return (TextView) getView(GUESTS_TEXT_VIEW_ID);
	}

	// Object interaction

	public void clickCancelButton() {
		clickOnView(cancelButton());
	}

	public void clickSearchButton() {
		clickOnView(searchButton());
	}

	public void clickDestinationEditText() {
		clickOnView(destinationEditText());
	}

	public void clearDestinationEditText() {
		clearEditText(destinationEditText());
	}

	public void typeInDestinationEditText(String text) {
		typeText(destinationEditText(), text);
	}

	public void clickOriginEditText() {
		clickOnView(destinationEditText());
	}

	public void clearOriginEditText() {
		clearEditText(destinationEditText());
	}

	public void typeInOriginEditText(String text) {
		typeText(destinationEditText(), text);
	}

	public void clickGuestsButton() {
		clickOnView(guestsTextView());
	}

}
