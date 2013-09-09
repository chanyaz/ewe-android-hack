package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

public class CommonTravelerInformationScreen extends ScreenActions {

	private static final int sFirstNameEditTextID = R.id.edit_first_name;
	private static final int sMiddleNameEditTextID = R.id.edit_middle_name;
	private static final int sLastNameEditTextID = R.id.edit_last_name;
	private static final int sPhoneNumberEditTextID = R.id.edit_phone_number;
	private static final int sBirthDateSpinnerButtonID = R.id.edit_birth_date_text_btn;
	private static final int sDoneStringID = R.string.done;
	private static final int sNextButtonID = R.id.menu_next;
	private static final int sDoneButtonID = R.id.menu_done;

	public CommonTravelerInformationScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public EditText firstNameEditText() {
		return (EditText) getView(sFirstNameEditTextID);
	}

	public EditText middleNameEditText() {
		return (EditText) getView(sMiddleNameEditTextID);
	}

	public EditText lastNameEditText() {
		return (EditText) getView(sLastNameEditTextID);
	}

	public EditText phoneNumberEditText() {
		return (EditText) getView(sPhoneNumberEditTextID);
	}

	public View birthDateSpinnerButton() {
		return getView(sBirthDateSpinnerButtonID);
	}

	public String done() {
		return getString(sDoneStringID);
	}

	public View doneButton() {
		return getView(sDoneButtonID);
	}

	public View nextButton() {
		return getView(sNextButtonID);
	}

	// Object interaction

	public void enterFirstName(String firstName) {
		typeText(firstNameEditText(), firstName);
	}

	public void enterMiddleName(String middleName) {
		typeText(firstNameEditText(), middleName);
	}

	public void enterLastName(String lastName) {
		typeText(lastNameEditText(), lastName);
	}

	public void enterPhoneNumber(String phoneNumber) {
		typeText(phoneNumberEditText(), phoneNumber);
	}

	public void clickBirthDateButton() {
		clickOnView(birthDateSpinnerButton());
	}

	public void clickDoneString() {
		clickOnText(done());
	}

	public void clickDoneButton() {
		clickOnView(doneButton());
	}
}
