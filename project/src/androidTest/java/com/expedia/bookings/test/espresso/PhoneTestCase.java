package com.expedia.bookings.test.espresso;

public class PhoneTestCase extends EspressoTestCase {

	public PhoneTestCase() {
		super();
	}

	public PhoneTestCase(Class cls) {
		super(cls);
	}

	@Override
	public void runTest() throws Throwable {
		if (Common.isPhone()) {
			super.runTest();
		}
	}
}
