package com.expedia.bookings.test.utils;

import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.espresso.IdlingResources;
import com.expedia.bookings.test.espresso.IdlingResources.SuggestionResource;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Settings;

public class TabletTestCase extends ActivityInstrumentationTestCase2 {

	public TabletTestCase() {
		super(SearchActivity.class);
	}

	private SuggestionResource mSuggestionResource;

	@Override
	public void runTest() throws Throwable {
		if (Common.isTablet(getInstrumentation())) {
			mSuggestionResource = new SuggestionResource();
			IdlingResources.registerSuggestionResource(mSuggestionResource);

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
		if (Common.isTablet(getInstrumentation())) {
			if (mSuggestionResource != null) {
				IdlingResources.unregisterSuggestionResource(mSuggestionResource);
			}
			Common.pressBackOutOfApp();
		}
	}
}
