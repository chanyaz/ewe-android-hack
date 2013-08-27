package com.expedia.bookings.test.tests.pageModels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class LogInScreen extends ScreenActions {

	private static int sEmailAddressEditTextID = R.id.username_edit_text;
	private static int sPasswordEditTextID = R.id.password_edit_text;
	private static int sLogInWithFacebookButtonID = R.id.log_in_with_facebook_btn;
	private static int sLogInButtonID = R.id.log_in_with_expedia_btn;
	private static int sForgotPasswordLink = R.id.forgot_your_password_link;
	
	public LogInScreen(Instrumentation instrumentation, Activity activity, Resources res) {
		super(instrumentation, activity, res);
	}

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

	public void clickOnFacebookButton() {
		clickOnView(facebookButton());
	}

	public void clickOnLoginButton() {
		clickOnView(logInButton());
	}
	
	public void clickOnForgotPasswordLink() {
		clickOnView(forgotPasswordLink());
	}

}
