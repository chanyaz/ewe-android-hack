package com.expedia.bookings.test.tests.full;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.flights.FlightsHappyPath;
import com.expedia.bookings.test.tests.hotels.HotelsHappyPath;
import com.expedia.bookings.test.utils.FlightsTestDriver;
import com.expedia.bookings.test.utils.HotelsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;

public class FullHappyPathTest extends ActivityInstrumentationTestCase2<SearchActivity> {

	public FullHappyPathTest() { //Default constructor
		super(SearchActivity.class);
	}

	private static final String TAG = "Full Happy Path Test";

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsTestDriver mHotelsDriver;
	private FlightsTestDriver mFlightsDriver;
	private HotelsUserData mUser;
	private TestPreferences mPreferences;

	protected void setUp() throws Exception {
		super.setUp();
		mRes = getActivity().getBaseContext().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mHotelsDriver = new HotelsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mFlightsDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser = new HotelsUserData(getActivity());
		mUser.setHotelCityToRandomUSCity();
	}

	public void testMethod() throws Exception {
		HotelsHappyPath.execute(mHotelsDriver, mUser, 1);
		FlightsHappyPath.execute(mFlightsDriver, mUser);
	}

	@Override
	protected void tearDown() throws Exception {
		mHotelsDriver.enterLog(TAG, "tearing down...");
		mHotelsDriver.finishOpenedActivities();
	}
}
