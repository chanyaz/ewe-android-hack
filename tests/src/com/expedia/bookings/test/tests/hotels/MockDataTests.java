package com.expedia.bookings.test.tests.hotels;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class MockDataTests extends ActivityInstrumentationTestCase2<SearchActivity> {

	public MockDataTests() {
		super("com.expedia.bookings", SearchActivity.class);
	}

	private static final String TAG = "Mock Data Tests";

	private Solo mSolo;

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsRobotHelper mDriver;
	private HotelsUserData mUser;

	//SF-slave-android-3 is perpetually running E3MockServer at this IP
	private static final String SERVER_IP = "172.17.249.246";

	protected void setUp() throws Exception {
		super.setUp();
		mSolo = new Solo(getInstrumentation(), getActivity());
		mRes = getActivity().getBaseContext().getResources();
		mMetric = mRes.getDisplayMetrics();
		mDriver = new HotelsRobotHelper(mSolo, mRes);
		mUser = new HotelsUserData();
		mDriver.setUpMockServer(SERVER_IP);
		mDriver.clearPrivateData();
	}

	// Verify that hotel room with raised price shows proper error message
	public void testHotelRateUp() throws Exception {
		mDriver.launchHotels();
		mDriver.waitForStringToBeGone(R.string.progress_searching_hotels);
		mSolo.clickOnText("rateup");
		mSolo.clickOnButton(0);
		mDriver.selectRoom(0);
		mDriver.waitForStringToBeGone(R.string.calculating_taxes_and_fees);
		String alertText = (String) mSolo.getText(0).getText();
		String expectedText = mRes.getString(R.string.the_hotel_raised_the_total_price_TEMPLATE, "$400.65", "$440.65");
		mDriver.enterLog(TAG, "Expected text: " + expectedText);
		mDriver.enterLog(TAG, "Alert text: " + alertText);
		if (!expectedText.equals(alertText)) {
			fail();
		}
	}

	// Verify that hotel room with lowered price shows proper error message
	public void testHotelRateDown() throws Exception {
		mDriver.launchHotels();
		mDriver.waitForStringToBeGone(R.string.progress_searching_hotels);
		mSolo.clickOnText("ratedown");
		mSolo.clickOnButton(0);
		mDriver.selectRoom(0);
		mDriver.waitForStringToBeGone(R.string.calculating_taxes_and_fees);
		String alertText = (String) mSolo.getText(0).getText();
		String expectedText = mRes.getString(R.string.the_hotel_lowered_the_total_price_TEMPLATE, "$400.65", "$360.65");
		mDriver.enterLog(TAG, "Expected text: " + expectedText);
		mDriver.enterLog(TAG, "Alert text: " + alertText);
		if (!expectedText.equals(alertText)) {
			fail();
		}
	}

	// Verify sold out room shows correct error message
	public void testRoomNoLongerAvailable() throws Exception {
		mDriver.launchHotels();
		mDriver.waitForStringToBeGone(R.string.progress_searching_hotels);
		mSolo.clickOnText("no_rooms");
		mSolo.clickOnButton(0);
		mDriver.selectRoom(0);
		mDriver.waitForStringToBeGone(R.string.calculating_taxes_and_fees);
		String alertText = (String) mSolo.getText(0).getText();
		String expectedText = mRes.getString(R.string.error_hotel_is_now_sold_out);
		mDriver.enterLog(TAG, "Expected text: " + expectedText);
		mDriver.enterLog(TAG, "Alert text: " + alertText);
		if (!expectedText.equals(alertText)) {
			fail();
		}
		String ok = mRes.getString(R.string.ok);
		mSolo.clickOnText(ok);
	}

	// Go through each room in the list, verify the sold out message
	// then verify that the hotel is sold out
	public void testRoomsAndRatesNoMoreRooms() throws Exception {
		mDriver.launchHotels();
		mDriver.waitForStringToBeGone(R.string.progress_searching_hotels);
		mSolo.clickOnText("multi_room_unavailable");
		mSolo.clickOnButton(0);
		mDriver.waitForStringToBeGone(R.string.room_rates_loading);
		ListView lv = (ListView) mSolo.getView(android.R.id.list);
		int upperBound = lv.getChildCount();
		mDriver.enterLog(TAG, "Upper bounds: " + upperBound);
		String ok;
		for (int i = 0; i < upperBound; i++) {
			mSolo.clickInList(0);
			mDriver.waitForStringToBeGone(R.string.calculating_taxes_and_fees);
			String alertText = (String) mSolo.getText(0).getText();
			String expectedText = mRes.getString(R.string.e3_error_checkout_hotel_room_unavailable);
			mDriver.enterLog(TAG, "Expected text: " + expectedText);
			mDriver.enterLog(TAG, "Alert text: " + alertText);
			if (!expectedText.equals(alertText)) {
				fail();
			}
			ok = mRes.getString(R.string.ok);
			mSolo.clickOnText(ok);
			mDriver.delay(1);
		}
		mDriver.delay(3);
		String alertText = (String) mSolo.getText(0).getText();
		String expectedText = mRes.getString(R.string.error_hotel_is_now_sold_out);
		mDriver.enterLog(TAG, "Expected text: " + expectedText);
		mDriver.enterLog(TAG, "Alert text: " + alertText);
		if (!expectedText.equals(alertText)) {
			fail();
		}
		ok = mRes.getString(R.string.ok);
		mSolo.clickOnText(ok);
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");
		mSolo.finishOpenedActivities();
	}

}
