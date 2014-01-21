package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

public class LogInScreen extends ScreenActions {

	private static final int sEmailAddressEditTextID = R.id.username_edit_text;
	private static final int sPasswordEditTextID = R.id.password_edit_text;
	private static final int sLogInWithFacebookButtonID = R.id.log_in_with_facebook_btn;
	private static final int sLogInButtonID = R.id.log_in_with_expedia_btn;
	private static final int sForgotPasswordLink = R.id.forgot_your_password_link;
	private static final int sLoggingInStringID = R.string.logging_in;

	public LogInScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public EditText emailAddressEditText() {
		return (EditText) getView(sEmailAddressEditTextID);
	}

	public EditText passwordEditText() {
		return (EditText) getView(sPasswordEditTextID);
	}

	public View facebookButton() {
		return getView(sLogInWithFacebookButtonID);
	}

	public View logInButton() {
		return getView(sLogInButtonID);
	}

	public View forgotPasswordLink() {
		return getView(sForgotPasswordLink);
	}

	public String loggingInDialogString() {
		return mRes.getString(sLoggingInStringID);
	}

	// Object interaction

	public void clickOnFacebookButton() {
		clickOnView(facebookButton());
	}

	public void clickOnLoginButton() {
		clickOnView(logInButton());
	}

	public void clickOnForgotPasswordLink() {
		clickOnView(forgotPasswordLink());
	}

	public void typeTextEmailEditText(String text) {
		typeText(emailAddressEditText(), text);
	}

	public void typeTextPasswordEditText(String text) {
		typeText(passwordEditText(), text);
	}

}
