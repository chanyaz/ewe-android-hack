package com.expedia.bookings.test.tests.pageModels.flights;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.utils.TestPreferences;

public class FlightsTravelerInfoScreen extends CommonTravelerInformationScreen {

	private static final int REDRESS_EDITTEXT_ID = R.id.edit_redress_number;

	public FlightsTravelerInfoScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public EditText redressEditText() {
		return (EditText) getView(REDRESS_EDITTEXT_ID);
	}

	public void typeRedressText(String redressText) {
		typeText(redressEditText(), redressText);
	}

}
