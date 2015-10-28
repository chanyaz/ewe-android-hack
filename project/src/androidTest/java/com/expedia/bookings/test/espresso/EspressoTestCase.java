package com.expedia.bookings.test.espresso;

import java.util.Locale;

import android.content.Intent;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.activity.RouterActivity;
import com.expedia.bookings.test.tablet.pagemodels.Settings;

public class EspressoTestCase extends ActivityInstrumentationTestCase2 {
	public EspressoTestCase() {
		super(RouterActivity.class);
	}

	public EspressoTestCase(Class cls) {
		super(cls);
	}

	protected Resources mRes;
	protected String mLanguage;
	protected String mCountry;

	@Override
	public void runTest() throws Throwable {
		// Get server value from config file deployed in devices,
		// if not defined in config defaults to MockWebServer.
		if (TestConfiguration.exists()) {
			TestConfiguration.Config config = new TestConfiguration().getConfiguration();
			Settings.setServer(config.server);
			mLanguage = config.language;
			mCountry = config.country;
		}

		mRes = getInstrumentation().getTargetContext().getResources();

		// Espresso will not launch our activity for us, we must launch it via getActivity().
		Intent clearingIntent = new Intent();
		clearingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		setActivityIntent(clearingIntent);
		getActivity();

		super.runTest();
	}

	public void screenshot(String tag) throws Throwable {
		if (TestConfiguration.exists()) {
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
	}

	public Locale getLocale() {
		return new Locale(mLanguage, mCountry);
	}

	public String getPOS(Locale locale) {
		return locale.getDisplayCountry(new Locale("en", "US")).replace(" ", "_").toUpperCase();
	}
}
