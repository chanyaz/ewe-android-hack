package com.expedia.bookings.test.utils;

import com.expedia.bookings.test.espresso.IdlingResources;
import com.expedia.bookings.test.espresso.IdlingResources.SuggestionResource;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;

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
			Common.pressBackOutOfApp();
		}
	}
}
