package com.expedia.bookings.test.tests.pageModels.flights;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.test.tests.pageModels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.utils.TestPreferences;

public class FlightsCheckoutScreen extends CommonCheckoutScreen {

	public FlightsCheckoutScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

}
