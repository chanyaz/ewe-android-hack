package com.expedia.bookings.test.espresso;

import com.expedia.bookings.test.espresso.IdlingResources.SuggestionResource;

public class TabletTestCase extends EspressoTestCase {

	private SuggestionResource mSuggestionResource;

	@Override
	public void runTest() throws Throwable {
		if (Common.isTablet()) {
			mSuggestionResource = new SuggestionResource();
			mSuggestionResource.register();
			super.runTest();
		}
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		if (Common.isTablet()) {
			mSuggestionResource.unregister();
			Common.pressBackOutOfApp();
		}
	}
}
