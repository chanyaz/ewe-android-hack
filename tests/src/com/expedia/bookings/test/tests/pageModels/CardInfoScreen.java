package com.expedia.bookings.test.tests.pageModels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;

public class CardInfoScreen extends ScreenActions {

	private static int sCreditCardNumberEditTextID = R.id.edit_creditcard_number;
	private static int sExpirationDateButtonID = R.id.edit_creditcard_exp_text_btn;
	private static int sNameOnCardEditTextID = R.id.edit_name_on_card;
	private static int sDoneButtonID = R.id.menu_done;

	private static int sMonthUpButtonID = R.id.month_up;
	private static int sMonthDownButtonID = R.id.month_down;
	private static int sYearUpButtonID = R.id.year_up;
	private static int sYearDownButtonID = R.id.year_down;

	public CardInfoScreen(Instrumentation instrumentation, Activity activity, Resources res) {
		super(instrumentation, activity, res);
	}

	// Object access

	public EditText creditCardNumberEditText() {
		return (EditText) getView(sCreditCardNumberEditTextID);
	}

	public View expirationDateButton() {
		return getView(sExpirationDateButtonID);
	}

	public EditText nameOnCardEditText() {
		return (EditText) getView(sNameOnCardEditTextID);
	}
	
	public View doneButton() {
		return getView(sDoneButtonID);
	}
	
	public String noThanksButtonString() {
		return mRes.getString(R.string.no_thanks);
	}
	
	public String saveButtonString() {
		return mRes.getString(R.string.save);
	}
	
	// Object access expiration date dialog
	public View monthUpButton() {
		return getView(sMonthUpButtonID);
	}

	public View monthDownButton() {
		return getView(sMonthDownButtonID);
	}

	public View yearUpButton() {
		return getView(sYearUpButtonID);
	}

	public View yearDownButton() {
		return getView(sYearDownButtonID);
	}

	public View setButton() {
		return positiveButton();
	}

	public View cancelButton() {
		return negativeButton();
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
}
