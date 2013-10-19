package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

public class CommonTravelerInformationScreen extends ScreenActions {

	private static final int FIRST_NAME_EDIT_TEXT_ID = R.id.edit_first_name;
	private static final int MIDDLE_NAME_EDIT_TEXT_ID = R.id.edit_middle_name;
	private static final int LAST_NAME_EDIT_TEXT_ID = R.id.edit_last_name;
	private static final int PHONE_NUMBER_EDIT_TEXT_ID = R.id.edit_phone_number;
	private static final int EMAIL_EDIT_TEXT_ID = R.id.edit_email_address;
	private static final int BIRTHDATE_SPINNER_BUTTON_ID = R.id.edit_birth_date_text_btn;
	private static final int DONE_STRING_ID = R.string.done;
	private static final int NEXT_BUTTON_ID = R.id.menu_next;
	private static final int DONE_BUTTON_ID = R.id.menu_done;
	private static final int ENTER_A_NEW_TRAVELER_STRING_ID = R.string.enter_new_traveler;
	private static final int SET_STRING_ID = R.string.btn_set;

	public CommonTravelerInformationScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public EditText firstNameEditText() {
		return (EditText) getView(FIRST_NAME_EDIT_TEXT_ID);
	}

	public EditText middleNameEditText() {
		return (EditText) getView(MIDDLE_NAME_EDIT_TEXT_ID);
	}

	public EditText lastNameEditText() {
		return (EditText) getView(LAST_NAME_EDIT_TEXT_ID);
	}

	public EditText phoneNumberEditText() {
		return (EditText) getView(PHONE_NUMBER_EDIT_TEXT_ID);
	}

	public View birthDateSpinnerButton() {
		return getView(BIRTHDATE_SPINNER_BUTTON_ID);
	}

	public String done() {
		return getString(DONE_STRING_ID);
	}

	public View doneButton() {
		return getView(DONE_BUTTON_ID);
	}

	public EditText emailEditText() {
		return (EditText) getView(EMAIL_EDIT_TEXT_ID);
	}

	public View nextButton() {
		return getView(NEXT_BUTTON_ID);
	}

	public String enterANewTraveler() {
		return getString(ENTER_A_NEW_TRAVELER_STRING_ID);
	}

	public String set() {
		return getString(SET_STRING_ID);
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

	public void clickEnterANewTraveler() {
		clickOnText(enterANewTraveler());
	}

	public void enterEmailAddress(String emailAddress) {
		typeText(emailEditText(), emailAddress);
	}

	public void clickNextButton() {
		clickOnView(nextButton());
	}

	public void clickSetButton() {
		clickOnButton(set());
	}

}
