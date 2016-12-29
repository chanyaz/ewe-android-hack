package com.expedia.bookings.test.espresso;

import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.tablet.pagemodels.Settings;
import com.expedia.bookings.utils.ExpediaNetUtils;
import com.mobiata.android.Log;

import java.util.Locale;

public class PhoneTestCase extends EspressoTestCase {

	public PhoneTestCase() {
		super();
	}

	public PhoneTestCase(Class cls) {
		super(cls);
	}

	@Override
	public void runTest() throws Throwable {
		Log.d("PhoneTestCase", "testStarted");
		reset();
		if (Common.isPhone()) {
			super.runTest();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		Log.d("PhoneTestCase", "testEnded");
		super.tearDown();
	}

	private void reset() {
		ExpediaNetUtils.setFake(true, true);

		//clear private data
		Settings.clearPrivateData();

		Settings.setFakeCurrentLocation("0", "0");

		//set US locale and POS
		Common.setLocale(new Locale("en", "US"));
		Common.setPOS(PointOfSaleId.UNITED_STATES);

		Settings.setMockModeEndPoint();
	}
}
