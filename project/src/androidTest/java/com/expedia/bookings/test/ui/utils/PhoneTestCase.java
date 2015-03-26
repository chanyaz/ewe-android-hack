package com.expedia.bookings.test.ui.utils;

import com.expedia.bookings.test.ui.espresso.IdlingResources;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;

public class PhoneTestCase extends EspressoTestCase {

	private IdlingResources.LxSearchResource mIdlingSearchResource;

	public IdlingResources.LxSearchResource getLxIdlingResource() {
		return mIdlingSearchResource;
	}

	public PhoneTestCase() {
		super();
	}

	public PhoneTestCase(Class cls) {
		super(cls);
	}

	@Override
	public void runTest() throws Throwable {
		if (Common.isPhone(getInstrumentation())) {
			mIdlingSearchResource = new IdlingResources.LxSearchResource();
			IdlingResources.registerLxSearchLoadResource(mIdlingSearchResource);
			super.runTest();
		}
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		if (Common.isPhone(getInstrumentation())) {
			IdlingResources.unregisterLxSearchLoadResource(mIdlingSearchResource);
			Common.pressBackOutOfApp(getInstrumentation());
		}
	}
}
