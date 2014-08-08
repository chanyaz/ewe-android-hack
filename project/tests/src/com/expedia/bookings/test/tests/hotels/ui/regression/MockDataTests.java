package com.expedia.bookings.test.tests.hotels.ui.regression;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.ConfigFileUtils;
import com.expedia.bookings.test.utils.HotelsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;

public class MockDataTests extends ActivityInstrumentationTestCase2<SearchActivity> {

	public MockDataTests() {
		super("com.expedia.bookings", SearchActivity.class);
	}

	private static final String TAG = "Mock Data Tests";

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsTestDriver mDriver;
	private HotelsUserData mUser;
	private TestPreferences mPreferences;
	private ConfigFileUtils mConfigFileUtils;

	protected void setUp() throws Exception {
		super.setUp();
		mRes = getActivity().getBaseContext().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mDriver = new HotelsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser = new HotelsUserData(getInstrumentation());
		mConfigFileUtils = new ConfigFileUtils();

		mUser.setBookingServer("Mock Server");
		mUser.setServerIP(mConfigFileUtils.getConfigValue("Mock Server IP"));
		mUser.setServerPort(mConfigFileUtils.getConfigValue("Mock Server Port"));

		mDriver.delay();
		mDriver.launchScreen().openMenuDropDown();
		mDriver.launchScreen().pressSettings();
		mDriver.settingsScreen().clickToClearPrivateData();
		mDriver.settingsScreen().clickOKString();
		mDriver.settingsScreen().clickOKString();

		mDriver.settingsScreen().clickSelectAPIString();
		mDriver.settingsScreen().clickOnText(mUser.getBookingServer());
		mDriver.settingsScreen().clickServerProxyAddressString();
		mDriver.settingsScreen().clearServerEditText();
		mDriver.settingsScreen().enterServerText(mUser.getServerIP() + ":" + mUser.getServerPort());
		mDriver.settingsScreen().clickOKString();

		mDriver.settingsScreen().goBack();
	}

	// Verify that hotel room with raised price shows proper error message
	public void testHotelRateUp() throws Exception {
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickInList(1);
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.clickOnText("rateup");
		mDriver.hotelsDetailsScreen().clickSelectButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsRoomsRatesScreen().findingAvailableRooms());
		mDriver.hotelsRoomsRatesScreen().selectRoom(0);
		mDriver.waitForStringToBeGone(mDriver.hotelsCheckoutScreen().calculatingTaxesAndFees());
		String alertText = (String) mDriver.getText(0).getText();
		String expectedText = mRes.getString(R.string.the_hotel_raised_the_total_price_TEMPLATE, "$400.65", "$440.65");
		mDriver.enterLog(TAG, "Expected text: " + expectedText);
		mDriver.enterLog(TAG, "Alert text: " + alertText);
		assertEquals(expectedText, alertText);
	}

	// Verify that hotel room with lowered price shows proper error message
	public void testHotelRateDown() throws Exception {
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickInList(1);
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.clickOnText("ratedown");
		mDriver.hotelsDetailsScreen().clickSelectButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsRoomsRatesScreen().findingAvailableRooms());
		mDriver.hotelsRoomsRatesScreen().selectRoom(0);
		mDriver.waitForStringToBeGone(mDriver.hotelsCheckoutScreen().calculatingTaxesAndFees());
		String alertText = (String) mDriver.getText(0).getText();
		String expectedText = mRes.getString(R.string.the_hotel_lowered_the_total_price_TEMPLATE, "$400.65", "$360.65");
		mDriver.enterLog(TAG, "Expected text: " + expectedText);
		mDriver.enterLog(TAG, "Alert text: " + alertText);
		assertEquals(expectedText, alertText);
	}

	// Verify sold out room shows correct error message
	public void testRoomNoLongerAvailable() throws Exception {
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickInList(1);
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.clickOnText("no_rooms");
		mDriver.hotelsDetailsScreen().clickSelectButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsRoomsRatesScreen().findingAvailableRooms());
		mDriver.hotelsRoomsRatesScreen().selectRoom(0);
		mDriver.waitForStringToBeGone(mDriver.hotelsCheckoutScreen().calculatingTaxesAndFees());
		String alertText = (String) mDriver.getText(0).getText();
		String expectedText = mRes.getString(R.string.e3_error_checkout_hotel_room_unavailable);
		mDriver.enterLog(TAG, "Expected text: " + expectedText);
		mDriver.enterLog(TAG, "Alert text: " + alertText);
		assertEquals(expectedText, alertText);
		String ok = mRes.getString(R.string.ok);
		mDriver.clickOnText(ok);
	}

	// Go through each room in the list, verify the sold out message
	// then verify that the hotel is sold out multi_room_unavailable
	public void testRoomsAndRatesNoMoreRooms() throws Exception {
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickInList(1);
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.clickOnText("multi_room_unavailable");
		mDriver.hotelsDetailsScreen().clickSelectButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsRoomsRatesScreen().findingAvailableRooms());

		//number of rooms is the list's child count - 1, for the header
		int upperBound = mDriver.hotelsRoomsRatesScreen().roomList().getChildCount();
		mDriver.enterLog(TAG, "Upper bounds: " + upperBound);
		String ok;
		for (int i = 0; i < upperBound; i++) {
			mDriver.hotelsRoomsRatesScreen().selectRoom(0);
			mDriver.waitForStringToBeGone(mDriver.hotelsCheckoutScreen().calculatingTaxesAndFees());
			String alertText = (String) mDriver.getText(0).getText();
			String expectedText = mRes.getString(R.string.e3_error_checkout_hotel_room_unavailable);
			mDriver.enterLog(TAG, "Expected text: " + expectedText);
			mDriver.enterLog(TAG, "Alert text: " + alertText);
			if (!expectedText.equals(alertText)) {
				fail();
			}
			ok = mRes.getString(R.string.ok);
			mDriver.clickOnText(ok);
			mDriver.delay(1);
			// The upperBound can very based upon the size of the phone screen
			// so this will ensure that we break out of the loop if there
			// are no more hotel rooms
			if (mDriver.searchText(mRes.getString(R.string.error_hotel_is_now_sold_out_expedia))) {
				break;
			}
		}
		String alertText = (String) mDriver.getText(0).getText();
		String expectedText = mRes.getString(R.string.error_hotel_is_now_sold_out_expedia);
		mDriver.enterLog(TAG, "Expected text: " + expectedText);
		mDriver.enterLog(TAG, "Alert text: " + alertText);
		assertEquals(expectedText, alertText);
		ok = mRes.getString(R.string.ok);
		mDriver.clickOnText(ok);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
