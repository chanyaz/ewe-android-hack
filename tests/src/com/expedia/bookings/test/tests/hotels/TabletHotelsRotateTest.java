package com.expedia.bookings.test.tests.hotels;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TabletsRobotHelper;
import com.jayway.android.robotium.solo.Solo;

public class TabletHotelsRotateTest extends ActivityInstrumentationTestCase2<SearchActivity> {
	public TabletHotelsRotateTest() {
		super("com.expedia.bookings", SearchActivity.class);
	}

	private Solo mSolo;
	private static final String TAG = "Hotels Tablet Rotate Happy Path";
	private Resources mRes;
	DisplayMetrics mMetric;
	private TabletsRobotHelper mDriver;
	private HotelsUserData mUser;

	protected void setUp() throws Exception {
		super.setUp();

		mRes = getActivity().getBaseContext().getResources();
		mMetric = mRes.getDisplayMetrics();
		mUser = new HotelsUserData();
		mSolo = new Solo(getInstrumentation(), getActivity());
		mDriver = new TabletsRobotHelper(mSolo, mRes, mUser);

		mDriver.setScreenshotCount(1);
		mDriver.setAllowScreenshots(false);
		mDriver.setAllowOrientationChange(true);
		mDriver.setWriteEventsToFile(false);

		mUser.setHotelCityToRandomUSCity();
	}

	public void testMethod() throws Exception {
		try {
			mDriver.runHotelHappyPath();
		}
		catch (Error e) {
			mDriver.takeScreenshotUponFailure(e, TAG);
			throw e;
		}
		catch (Exception e) {
			mDriver.takeScreenshotUponFailure(e, TAG);
			throw e;
		}
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");
		mSolo.finishOpenedActivities();
	}

}
