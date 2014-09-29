package com.expedia.bookings.test.utils;

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
import com.expedia.bookings.test.tablet.pagemodels.Settings;
import com.mobiata.android.util.SettingUtils;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import static com.expedia.bookings.test.utils.SpoonScreenshotUtils.getCurrentActivity;

/**
 * Created by dmadan on 7/15/14.
 */
public class EspressoTestCase extends ActivityInstrumentationTestCase2 {
	public EspressoTestCase() {
		super(RouterActivity.class);
	}

	static final String TEST_CASE_CLASS = "android.test.InstrumentationTestCase";
	static final String TEST_CASE_METHOD = "runMethod";
	static final int PORTRAIT = 0;
	static final int LANDSCAPE = 1;

	protected MockWebServer mMockWebServer;
	protected FileOpener mFileOpener;
	protected Resources mRes;

	public void runTest() throws Throwable {

		Settings.clearPrivateData(getInstrumentation());

		// Get server value from config file deployed in devices,
		// if not defined in config defaults to MockWebServer.
		if (ConfigFileUtils.doesConfigFileExist()) {
			Settings.setServer(getInstrumentation(), new ConfigFileUtils().getConfigValue("Server"));
		}
		else {
			mMockWebServer = new MockWebServer();
			mMockWebServer.play();
			mFileOpener = new AndroidFileOpener(getInstrumentation().getContext());
			CustomDispatcher dispatcher = new CustomDispatcher(mFileOpener);
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

	public void screenshot(final String tag) throws Throwable {
		try {
			// Wait just a little for frames to settle
			Thread.sleep(200);
		}
		catch (Exception e) {
			// ignore
		}
		SpoonScreenshotUtils.screenshot(tag, getInstrumentation());
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

	protected void tearDown() throws Exception {
		if (mMockWebServer != null) {
			mMockWebServer.shutdown();
			mMockWebServer = null;
		}
		super.tearDown();
	}
}
