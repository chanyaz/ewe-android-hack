package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

public class CommonSelectTravelerScreen extends ScreenActions {

	private static final int sEnterANewTraveler = R.id.enter_info_manually_button;

	public CommonSelectTravelerScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public View enterInfoManuallyButton() {
		return getView(sEnterANewTraveler);
	}

	// Object interaction

	public void clickEnterInfoManuallyButton() {
		clickOnView(enterInfoManuallyButton());
	}

}
