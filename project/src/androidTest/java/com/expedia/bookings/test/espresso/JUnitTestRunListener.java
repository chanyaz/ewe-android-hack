package com.expedia.bookings.test.espresso;

import java.util.Locale;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;

import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.tablet.pagemodels.Settings;
import com.mobiata.android.Log;

public class JUnitTestRunListener extends RunListener {

	public void testRunStarted(Description description) throws Exception {
		Log.d("testRunStarted:" + description.testCount());

		Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

		//clear private data
		Settings.clearPrivateData(instrumentation);

		//set US locale and POS
		Common.setLocale(new Locale("en", "US"));
		Common.setPOS(PointOfSaleId.UNITED_STATES);
	}
}
