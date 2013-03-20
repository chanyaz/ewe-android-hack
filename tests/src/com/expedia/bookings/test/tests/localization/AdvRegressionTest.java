package com.expedia.bookings.test.tests.localization;

import java.util.ArrayList;
import java.util.Locale;

import android.content.res.Resources;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.util.Log;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.jayway.android.robotium.solo.Solo;

public class AdvRegressionTest extends ActivityInstrumentationTestCase2<SearchActivity> {

	public AdvRegressionTest() { //Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}

	private static final String TAG = AdvRegressionTest.class.getSimpleName();
	private static final String mInputFileDirectory = "/sdcard/advTestInput.txt";
	private Solo mSolo;

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsRobotHelper mDriver;

	protected void setUp() throws Exception {
		super.setUp();
		mSolo = new Solo(getInstrumentation(), getActivity());
		//Log.configureLogging("ExpediaBookings", true);

		mRes = getActivity().getBaseContext().getResources();
		mMetric = mRes.getDisplayMetrics();
		mDriver = new HotelsRobotHelper(mSolo, mRes);
		mDriver.setAllowScreenshots(false);
		mDriver.setAllowOrientationChange(false);
	}

	public void testBookingsAPAC() throws Exception {
		testMethod(mDriver.APAC_LOCALES);
	}

	public void testBookingsWestern() throws Exception {
		testMethod(mDriver.WESTERN_LOCALES);
	}

	public void testBookingsAmericas() throws Exception {
		testMethod(mDriver.AMERICAN_LOCALES);
	}

	private void testMethod(Locale[] locales) throws Exception {
		mDriver.createFileWriter();
		mDriver.readInstructionsToOutFile(mInputFileDirectory);
		Locale currentLocale;

		for (int i = 0; i < locales.length; i++) {
			currentLocale = locales[i];
			mDriver.setLocale(currentLocale);
			mDriver.changePOS(currentLocale);
			mDriver.setSpoofBookings();
			
			mDriver.launchHotels();
			mDriver.delay();
			mDriver.browseRooms(1, "NYC", true);
			mDriver.flushFileWriter();
		}

		//Must close the fileWriter to write text to file.
		mDriver.closeFileWriter();
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");
		mSolo.finishOpenedActivities();
	}

}
