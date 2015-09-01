package com.expedia.bookings.test.espresso;

import java.util.Locale;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

import android.app.Instrumentation;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.tablet.pagemodels.Settings;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

public class JUnitTestRunListener extends RunListener {

	public void testRunStarted(Description description) throws Exception {
		Log.d("testRunStarted:" + description.testCount());

		Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

		//clear private data
		Settings.clearPrivateData(instrumentation);

		//set US locale and POS
		setLocale(new Locale("en", "US"), instrumentation);
		setPOS(PointOfSaleId.UNITED_STATES, instrumentation);

	}

	public void setLocale(Locale loc, Instrumentation instrumentation) {
		Resources mRes = instrumentation.getTargetContext().getResources();

		Configuration conf = mRes.getConfiguration();
		ExpediaBookingApp app = (ExpediaBookingApp) instrumentation.getTargetContext().getApplicationContext();
		app.handleConfigurationChanged(conf, loc);
	}

	public void setPOS(PointOfSaleId pos, Instrumentation instrumentation) {
		SettingUtils.save(instrumentation.getTargetContext(), R.string.PointOfSaleKey, String.valueOf(pos.getId()));
		PointOfSale.onPointOfSaleChanged(instrumentation.getTargetContext());
	}
}
