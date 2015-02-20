package com.expedia.bookings.test.ui.utils;

import java.net.URL;
import java.util.Locale;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.view.Display;
import android.view.WindowManager;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.RouterActivity;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.tablet.pagemodels.Settings;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileOpener;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import static com.expedia.bookings.test.ui.utils.SpoonScreenshotUtils.getCurrentActivity;

public class EspressoTestCase extends ActivityInstrumentationTestCase2 {
	public EspressoTestCase() {
		super(RouterActivity.class);
	}

	public EspressoTestCase(Class cls) {
		super(cls);
	}

	static final String TEST_CASE_CLASS = "android.test.InstrumentationTestCase";
	static final String TEST_CASE_METHOD = "runMethod";
	static final int PORTRAIT = 0;
	static final int LANDSCAPE = 1;

	protected MockWebServer mMockWebServer;
	protected FileOpener mFileOpener;
	protected Resources mRes;
	protected String mLanguage;
	protected String mCountry;

	public void runTest() throws Throwable {

		Settings.clearPrivateData(getInstrumentation());

		// Get server value from config file deployed in devices,
		// if not defined in config defaults to MockWebServer.
		if (TestConfiguration.doesConfigFileExist()) {
			TestConfiguration.Config config = new TestConfiguration().getConfiguration();
			Settings.setServer(getInstrumentation(), config.server);
			mLanguage = config.language;
			mCountry = config.country;
		}
		else {
			mMockWebServer = new MockWebServer();
			mMockWebServer.play();
			mFileOpener = new AndroidFileOpener(getInstrumentation().getContext());
			ExpediaDispatcher dispatcher = new ExpediaDispatcher(mFileOpener);
			mMockWebServer.setDispatcher(dispatcher);

			//get mock web server address
			URL mockUrl = mMockWebServer.getUrl("");
			String server = mockUrl.getHost() + ":" + mockUrl.getPort();
			Settings.setCustomServer(getInstrumentation(), server);
		}

		mRes = getInstrumentation().getTargetContext().getResources();

		// Espresso will not launch our activity for us, we must launch it via getActivity().
		getActivity();

		try {
			super.runTest();
		}
		catch (Throwable t) {
			StackTraceElement testClass = findTestClassTraceElement(t);
			if (testClass != null) {
				String failedTestMethodName = testClass.getMethodName().replaceAll("[^A-Za-z0-9._-]", "_");
				String className = testClass.getClassName().replaceAll("[^A-Za-z0-9._-]", "_");
				String tag = failedTestMethodName + "--FAILURE";

				//takes a screenshot on test failure
				SpoonScreenshotUtils.screenshot(tag, getInstrumentation(), className, failedTestMethodName);
			}
			throw t;
		}
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

	public void rotateScreenTwice() throws Throwable {
		rotateScreen();

		//to rotate it back to original orientation
		rotateScreen();
	}

	public void rotateScreen() throws Throwable {
		Activity currentActivity = getCurrentActivity(getInstrumentation());
		Display display = ((WindowManager) currentActivity.getSystemService(getInstrumentation().getTargetContext().WINDOW_SERVICE)).getDefaultDisplay();
		int orientation = display.getOrientation();

		switch (orientation) {
		case PORTRAIT:
			currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		case LANDSCAPE:
			currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		}

		try {
			// Wait just a little for frames to settle
			Thread.sleep(1000);
		}
		catch (Exception e) {
			// ignore
		}
	}

	public void setLocale(Locale loc) throws Throwable {
		Configuration conf = mRes.getConfiguration();
		ExpediaBookingApp app = (ExpediaBookingApp) getInstrumentation().getTargetContext().getApplicationContext();
		app.handleConfigurationChanged(conf, loc);
	}

	public void setPOS(PointOfSaleId pos) {
		SettingUtils.save(getInstrumentation().getTargetContext(), R.string.PointOfSaleKey, String.valueOf(pos.getId()));
		PointOfSale.onPointOfSaleChanged(getInstrumentation().getTargetContext());
	}

	public Locale getLocale() {
		return new Locale(mLanguage, mCountry);
	}

	public String getPOS(Locale locale) {
		return locale.getDisplayCountry(new Locale("en", "US")).replace(" ", "_").toUpperCase();
	}

	protected void tearDown() throws Exception {
		if (mMockWebServer != null) {
			mMockWebServer.shutdown();
			mMockWebServer = null;
		}
		super.tearDown();
	}
}
