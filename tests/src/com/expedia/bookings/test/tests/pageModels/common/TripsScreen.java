package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

public class TripsScreen extends LaunchActionBar {

	private static int sEnterItinNumberViewID = R.id.or_enter_itin_number_tv;
	private static int sLogInButtonID = R.id.login_button;

	public TripsScreen(Instrumentation instrumentation, Activity activity, Resources res, TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public View enterItinNumberView() {
		return getView(sEnterItinNumberViewID);
	}

	public View logInButton() {
		return getView(sLogInButtonID);
	}

	public void clickEnterItinNumber() {
		clickOnView(enterItinNumberView());
	}

	public void clickOnLogInButton() {
		clickOnView(logInButton());
	}

}
