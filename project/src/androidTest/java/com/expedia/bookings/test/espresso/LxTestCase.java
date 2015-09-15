package com.expedia.bookings.test.espresso;

import com.expedia.ui.LXBaseActivity;
import com.expedia.bookings.test.espresso.IdlingResources.LxIdlingResource;

public class LxTestCase extends PhoneTestCase {

	private LxIdlingResource mLxIdlingResource;

	public LxIdlingResource getLxIdlingResource() {
		return mLxIdlingResource;
	}

	public LxTestCase() {
		super(LXBaseActivity.class);
	}

	@Override
	public void runTest() throws Throwable {
		if (Common.isPhone()) {
			mLxIdlingResource = new LxIdlingResource();
			mLxIdlingResource.register();
		}
		super.runTest();
	}

	@Override
	public void tearDown() throws Exception {
		if (Common.isPhone()) {
			mLxIdlingResource.unregister();
			mLxIdlingResource = null;
		}
		super.tearDown();
	}
}
