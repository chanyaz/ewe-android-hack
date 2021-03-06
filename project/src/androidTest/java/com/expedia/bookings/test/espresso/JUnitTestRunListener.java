package com.expedia.bookings.test.espresso;

import java.util.Locale;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.tablet.pagemodels.Settings;
import com.expedia.bookings.utils.ExpediaNetUtils;
import com.mobiata.android.Log;

public class JUnitTestRunListener extends RunListener {
	@Override
	public void testStarted(Description description) throws Exception {
		Log.d("RunListener", "testStarted: " + description);
		reset();
	}

	@Override
	public void testFinished(Description description) throws Exception {
		Log.d("RunListener", "testFinished: " + description);
		reset();
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
