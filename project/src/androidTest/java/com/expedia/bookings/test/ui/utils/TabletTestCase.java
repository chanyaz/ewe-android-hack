package com.expedia.bookings.test.ui.utils;

import com.expedia.bookings.test.ui.espresso.IdlingResources;
import com.expedia.bookings.test.ui.espresso.IdlingResources.SuggestionResource;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;

public class TabletTestCase extends EspressoTestCase {

	private SuggestionResource mSuggestionResource;

	@Override
	public void runTest() throws Throwable {
		if (Common.isTablet(getInstrumentation())) {
			mSuggestionResource = new SuggestionResource();
			IdlingResources.registerSuggestionResource(mSuggestionResource);
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
			Common.pressBackOutOfApp(getInstrumentation());
		}
	}
}
