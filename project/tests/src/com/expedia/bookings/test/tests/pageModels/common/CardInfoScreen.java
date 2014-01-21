package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

public class CardInfoScreen extends ScreenActions {

	private static final int CREDIT_CARD_NUMBER_EDIT_TEXT_ID = R.id.edit_creditcard_number;
	private static final int EXPIRATION_DATE_BUTTON_ID = R.id.edit_creditcard_exp_text_btn;
	private static final int NAME_ON_CARD_EDIT_TEXT_ID = R.id.edit_name_on_card;
	private static final int EMAIL_ADDRESS_EDIT_TEXT_ID = R.id.edit_email_address;
	private static final int POSTAL_CODE_EDIT_TEXT_ID = R.id.edit_address_postal_code;
	private static final int DONE_BUTTON_ID = R.id.menu_done;
	private static final int NEXT_BUTTON_ID = R.id.menu_next;

	private static final int MONTH_UP_BUTTON_ID = R.id.month_up;
	private static final int MONTH_DOWN_BUTTON_ID = R.id.month_down;
	private static final int YEAR_UP_BUTTON_ID = R.id.year_up;
	private static final int YEAR_DOWN_BUTTON_ID = R.id.year_down;

	public CardInfoScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public EditText creditCardNumberEditText() {
		return (EditText) getView(CREDIT_CARD_NUMBER_EDIT_TEXT_ID);
	}

	public View expirationDateButton() {
		return getView(EXPIRATION_DATE_BUTTON_ID);
	}

	public EditText nameOnCardEditText() {
		return (EditText) getView(NAME_ON_CARD_EDIT_TEXT_ID);
	}

	public View doneButton() {
		return getView(DONE_BUTTON_ID);
	}

	public String noThanksButtonString() {
		return mRes.getString(R.string.no_thanks);
	}

	public String saveButtonString() {
		return mRes.getString(R.string.save);
	}

	public EditText emailEditText() {
		return (EditText) getView(EMAIL_ADDRESS_EDIT_TEXT_ID);
	}

	public EditText postalCodeEditText() {
		return (EditText) getView(POSTAL_CODE_EDIT_TEXT_ID);
	}

	// Object access expiration date dialog
	public View monthUpButton() {
		return getView(MONTH_UP_BUTTON_ID);
	}

	public View monthDownButton() {
		return getView(MONTH_DOWN_BUTTON_ID);
	}

	public View yearUpButton() {
		return getView(YEAR_UP_BUTTON_ID);
	}

	public View yearDownButton() {
		return getView(YEAR_DOWN_BUTTON_ID);
	}

	public View setButton() {
		return positiveButton();
	}

	public View cancelButton() {
		return negativeButton();
	}

	public View nextButton() {
		return getView(NEXT_BUTTON_ID);
	}

	// Object interaction

	public void typeTextCreditCardEditText(String text) {
		typeText(creditCardNumberEditText(), text);
	}

	public void clickOnExpirationDateButton() {
		clickOnView(expirationDateButton());
	}

	public void typeTextNameOnCardEditText(String text) {
		typeText(nameOnCardEditText(), text);
	}

	public void typeTextEmailEditText(String emailAddress) {
		typeText(emailEditText(), emailAddress);
	}

	public void typeTextPostalCode(String postalCode) {
		typeText(postalCodeEditText(), postalCode);
	}

	public void clickOnDoneButton() {
		clickOnView(doneButton());
	}

	// Object interaction expiration date dialog
	public void clickMonthUpButton() {
		clickOnView(monthUpButton());
	}

	public void clickMonthDownButton() {
		clickOnView(monthDownButton());
	}

	public void clickYearUpButton() {
		clickOnView(yearUpButton());
	}

	public void clickYearDownButton() {
		clickOnView(yearDownButton());
	}

	public void clickSetButton() {
		clickOnView(setButton());
	}

	public void clickCancelButton() {
		clickOnView(cancelButton());
	}

	public void clickNoThanksButton() {
		clickOnText(noThanksButtonString());
	}

	public void clickSaveButton() {
		clickOnText(saveButtonString());
	}

	public void clickNextButton() {
		clickOnView(nextButton());
	}
}
