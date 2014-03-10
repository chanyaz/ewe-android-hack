package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.test.tests.pageModels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.utils.TestPreferences;

public class HotelsTravelerInfoScreen extends CommonTravelerInformationScreen {

	public HotelsTravelerInfoScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

}
