package com.expedia.bookings.test.utils;

import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.espresso.IdlingResources;
import com.expedia.bookings.test.espresso.IdlingResources.SuggestionResource;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Settings;

public class PhoneTestCase extends ActivityInstrumentationTestCase2 {

	public PhoneTestCase() {
		super(SearchActivity.class);
	}

	@Override
	public void runTest() throws Throwable {
		if (Common.isPhone(getInstrumentation())) {
			Settings.clearPrivateData(getInstrumentation());
			Settings.setCustomServer(getInstrumentation(), "mocke3.mobiata.com");

			// Espresso will not launch our activity for us, we must launch it via getActivity().
			getActivity();

			super.runTest();
		}
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		if (Common.isPhone(getInstrumentation())) {
			Common.pressBackOutOfApp();
		}
	}
}
