package com.expedia.bookings.test.espresso;

import com.expedia.bookings.test.espresso.IdlingResources.SuggestionResource;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;

public class TabletTestCase extends EspressoTestCase {

	private SuggestionResource mSuggestionResource;

	@Override
	public void runTest() throws Throwable {
		if (Common.isTablet(getInstrumentation())) {
			mSuggestionResource = new SuggestionResource();
			mSuggestionResource.register();
			super.runTest();
		}
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		if (Common.isTablet(getInstrumentation())) {
			mSuggestionResource.unregister();
			Common.pressBackOutOfApp(getInstrumentation());
		}
	}
}
