package com.expedia.bookings.test.ui.utils;

import com.expedia.bookings.test.ui.tablet.pagemodels.Common;

public class PhoneTestCase extends EspressoTestCase {

	public PhoneTestCase() {
		super();
	}

	public PhoneTestCase(Class cls) {
		super(cls);
	}

	@Override
	public void runTest() throws Throwable {
		if (Common.isPhone(getInstrumentation())) {
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
