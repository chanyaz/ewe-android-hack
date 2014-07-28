package com.expedia.bookings.test.utils;

import java.net.URL;

import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModels.tablet.Settings;
import com.squareup.okhttp.mockwebserver.MockWebServer;

/**
 * Created by dmadan on 7/15/14.
 */
public class EspressoTestCase extends ActivityInstrumentationTestCase2 {

	public EspressoTestCase() {
		super(SearchActivity.class);
	}

	static final String TEST_CASE_CLASS = "android.test.InstrumentationTestCase";
	static final String TEST_CASE_METHOD = "runMethod";
	protected MockWebServer mMockWebServer;

	public void runTest() throws Throwable {

		Settings.clearPrivateData(getInstrumentation());

		//get server value from config file deployed in devices
		if (ConfigFileUtils.doesConfigFileExist()) {
			Settings.setServer(getInstrumentation(), new ConfigFileUtils().getConfigValue("Server"));
		}
		else {
			mMockWebServer = new MockWebServer();
			mMockWebServer.play();
			CustomDispatcher dispatcher = new CustomDispatcher(getInstrumentation());
			mMockWebServer.setDispatcher(dispatcher);

			//get mock web server address
			URL mockUrl = mMockWebServer.getUrl("");
			String server = mockUrl.getHost() + ":" + mockUrl.getPort();
			Settings.setCustomServer(getInstrumentation(), server);
		}

		// Espresso will not launch our activity for us, we must launch it via getActivity().
		getActivity();
		try {
			super.runTest();
		}
		catch (Throwable t) {
			StackTraceElement testClass = findTestClassTraceElement(t);
			String failedTestMethodName = testClass.getMethodName().replaceAll("[^A-Za-z0-9._-]", "_");
			String className = testClass.getClassName().replaceAll("[^A-Za-z0-9._-]", "_");
			String tag = failedTestMethodName + "--FAILURE";

			//takes a screenshot on test failure
			SpoonScreenshotUtils.screenshot(tag, getInstrumentation(), className, failedTestMethodName);
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

		throw new IllegalArgumentException("Could not find test class!", throwable);
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

	protected void tearDown() throws Exception {
		if (mMockWebServer != null) {
			mMockWebServer.shutdown();
			mMockWebServer = null;
		}
		super.tearDown();
	}
}
