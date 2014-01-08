package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;
import com.mobiata.android.widget.Spinner;

public class BillingAddressScreen extends ScreenActions {

	private static final int sAddressLine1EditTextID = R.id.edit_address_line_one;
	private static final int sAddressLine2EditTextID = R.id.edit_address_line_two;
	private static final int sAddressCityEditTextID = R.id.edit_address_city;
	private static final int sAddressStateEditTextID = R.id.edit_address_state;
	private static final int sAddressPostalCodeEditTextID = R.id.edit_address_postal_code;
	private static final int sAddressCountrySpinnerID = R.id.edit_country_spinner;
	private static final int sNextButtonID = R.id.menu_next;

	public BillingAddressScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	//Object access

	public EditText addressLineOneEditText() {
		return (EditText) getView(sAddressLine1EditTextID);
	}

	public EditText addressLineTwoEditText() {
		return (EditText) getView(sAddressLine2EditTextID);
	}

	public EditText cityEditText() {
		return (EditText) getView(sAddressCityEditTextID);
	}

	public EditText stateEditText() {
		return (EditText) getView(sAddressStateEditTextID);
	}

	public EditText postalCodeEditText() {
		return (EditText) getView(sAddressPostalCodeEditTextID);
	}

	public Spinner countrySpinner() {
		return (Spinner) getView(sAddressCountrySpinnerID);
	}

	public View nextButton() {
		return getView(sNextButtonID);
	}

	//Object interaction

	public void typeTextAddressLineOne(String text) {
		typeText(addressLineOneEditText(), text);
	}

	public void typeTextAddressLineTwo(String text) {
		typeText(addressLineTwoEditText(), text);
	}

	public void typeTextCity(String text) {
		typeText(cityEditText(), text);
	}

	public void typeTextState(String text) {
		typeText(stateEditText(), text);
	}

	public void typeTextPostalCode(String text) {
		typeText(postalCodeEditText(), text);
	}

	public void clickCountrySpinner() {
		clickOnView(countrySpinner());
	}

	public void clickNextButton() {
		clickOnView(nextButton());
	}
}
