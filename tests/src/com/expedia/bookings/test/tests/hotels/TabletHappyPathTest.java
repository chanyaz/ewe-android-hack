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

public class TabletHappyPathTest extends ActivityInstrumentationTestCase2<SearchActivity> {
	public TabletHappyPathTest() {
		super("com.expedia.bookings", SearchActivity.class);
	}

	private Solo mSolo;
	private static final String TAG = "TabletHappyPath";
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
		mDriver.setAllowOrientationChange(false);
		mDriver.setWriteEventsToFile(false);

		mUser.setHotelCityToRandomUSCity();
	}

	public void testMethod() {
		mDriver.delay(5);
		mDriver.setSpoofBookings();
		mDriver.selectLocation(mUser.mHotelSearchCity);
		mDriver.pressToSearch();
		mDriver.delay(10);

		mDriver.selectHotel(3);
		mDriver.delay();

		mDriver.pressSeeDetails();
		mSolo.scrollDown();
		mSolo.scrollToTop();

		mDriver.pressSelectRoom();
		mDriver.pressBookingInfo();
		mDriver.delay();

		mDriver.pressLoginButton();
		mDriver.enterLoginCredentials();
		mDriver.enterCCVAndZIP();
		mDriver.pressToConfirmAndBook();
		mDriver.delay(10);
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");
		mSolo.finishOpenedActivities();
	}

}
