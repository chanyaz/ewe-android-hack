package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

public class LogInScreen extends ScreenActions {

	private static final int EMAIL_ADDRESS_ET_STRING_ID = R.id.username_edit_text;
	private static final int PASSWORD_ET_STRING_ID = R.id.password_edit_text;
	private static final int FACEBOOK_BTN_ID = R.id.log_in_with_facebook_btn;
	private static final int LOGIN_BTN_ID = R.id.log_in_with_expedia_btn;
	private static final int FORGOT_PASSWORD_LINK_ID = R.id.forgot_your_password_link;
	private static final int LOGGING_IN_STRING_ID = R.string.logging_in;
	private static final int FETCHING_ITINERARIES_STRING_ID = R.string.fetching_your_itinerary;
	private static final int ERROR_FETCHING_ITINERARIES_STRING_ID = R.string.itinerary_fetch_error;
	private static final int NO_TRIPS_STRING = R.string.no_upcoming_trips;

	public LogInScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public EditText emailAddressEditText() {
		return (EditText) getView(EMAIL_ADDRESS_ET_STRING_ID);
	}

	public EditText passwordEditText() {
		return (EditText) getView(PASSWORD_ET_STRING_ID);
	}

	public View facebookButton() {
		return getView(FACEBOOK_BTN_ID);
	}

	public View logInButton() {
		return getView(LOGIN_BTN_ID);
	}

	public View forgotPasswordLink() {
		return getView(FORGOT_PASSWORD_LINK_ID);
	}

	public String loggingInDialogString() {
		return mRes.getString(LOGGING_IN_STRING_ID);
	}

	public String fetchingYourItineraries() {
		return mRes.getString(FETCHING_ITINERARIES_STRING_ID);
	}

	public String errorFetchingYourItineraries() {
		return mRes.getString(ERROR_FETCHING_ITINERARIES_STRING_ID);
	}

	public String noUpcomingTrips() {
		return mRes.getString(NO_TRIPS_STRING);
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
