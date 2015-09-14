package com.expedia.bookings.test.espresso;

import java.util.Locale;

import android.content.Intent;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.RouterActivity;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.tablet.pagemodels.Settings;

public class EspressoTestCase extends ActivityInstrumentationTestCase2 {
	public EspressoTestCase() {
		super(RouterActivity.class);
	}

	public EspressoTestCase(Class cls) {
		super(cls);
	}

	static final String TEST_CASE_CLASS = "android.test.InstrumentationTestCase";
	static final String TEST_CASE_METHOD = "runMethod";

	protected Resources mRes;
	protected String mLanguage;
	protected String mCountry;

	@Override
	public void runTest() throws Throwable {
		Settings.clearPrivateData(getInstrumentation());

		Settings.setFakeCurrentLocation(getInstrumentation(), "0", "0");
		// Get server value from config file deployed in devices,
		// if not defined in config defaults to MockWebServer.
		if (TestConfiguration.doesConfigFileExist()) {
			TestConfiguration.Config config = new TestConfiguration().getConfiguration();
			Settings.setServer(getInstrumentation(), config.server);
			mLanguage = config.language;
			mCountry = config.country;
		}
		else {
			Settings.setMockModeEndPoint(getInstrumentation());
		}

		mRes = getInstrumentation().getTargetContext().getResources();

		// Espresso will not launch our activity for us, we must launch it via getActivity().
		Intent clearingIntent = new Intent();
		clearingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		setActivityIntent(clearingIntent);
		getActivity();

		try {
			super.runTest();
		}
		catch (Throwable t) {
			StackTraceElement testClass = findTestClassTraceElement(t);
			if (testClass != null) {
				String failedTestMethodName = testClass.getMethodName().replaceAll("[^A-Za-z0-9._-]", "_");
				String tag = failedTestMethodName + "--FAILURE";

				//takes a screenshot on test failure
				SpoonScreenshotUtils.screenshot(tag, getInstrumentation(), testClass);
			}
			throw t;
		}
	}

	@Override
	protected void tearDown() throws Exception {
		getApplication().setLXTestComponent(null);
		super.tearDown();
	}

	public ExpediaBookingApp getApplication() {
		return Common.getApplication();
	}

	// Returns the test class element by looking at the method InstrumentationTestCase invokes.
	static StackTraceElement findTestClassTraceElement(Throwable throwable) {
		StackTraceElement[] trace = throwable.getStackTrace();
		for (int i = trace.length - 1; i >= 0; i--) {
			StackTraceElement element = trace[i];
			if (TEST_CASE_CLASS.equals(element.getClassName()) //
				&& TEST_CASE_METHOD.equals(element.getMethodName())) {
				return trace[i - 3];
			}
		}

		return null;
	}

	public void screenshot(String tag) throws Throwable {
		final String cleanTag = tag.replace(" ", "_");
		try {
			// Wait just a little for frames to settle
			Thread.sleep(200);
		}
		catch (Exception e) {
			// ignore
		}
		SpoonScreenshotUtils.screenshot(cleanTag, getInstrumentation());
	}

	public void setLocale(Locale loc) throws Throwable {
		Common.setLocale(loc);
	}

	public void setPOS(PointOfSaleId pos) {
		Common.setPOS(pos);
	}

	public Locale getLocale() {
		return new Locale(mLanguage, mCountry);
	}

	public String getPOS(Locale locale) {
		return locale.getDisplayCountry(new Locale("en", "US")).replace(" ", "_").toUpperCase();
	}
}
