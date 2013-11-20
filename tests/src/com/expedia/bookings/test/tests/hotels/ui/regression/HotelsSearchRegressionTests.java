package com.expedia.bookings.test.tests.hotels.ui.regression;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.mobiata.android.text.format.Time;

public class HotelsSearchRegressionTests extends CustomActivityInstrumentationTestCase<SearchActivity> {

	private static final String TAG = HotelsSearchRegressionTests.class.getSimpleName();

	public HotelsSearchRegressionTests() {
		super(SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mUser.setHotelCityToRandomUSCity();
		mDriver.launchScreen().openMenuDropDown();
		mDriver.launchScreen().pressSettings();
		mDriver.settingsScreen().clickToClearPrivateData();
		mDriver.settingsScreen().clickOKString();
		mDriver.settingsScreen().clickOKString();
		mDriver.goBack();
	}

   

	private void doASearchFor(int days) {
		Time now = new Time();
		now.setToNow();
		Time offsetTime = now;

		//setup
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickOnCalendarButton();
		mDriver.delay();
		mDriver.hotelsSearchScreen().clickDate(1);
		mDriver.hotelsSearchScreen().clickDate(0);

		// Do that search
		offsetTime.monthDay += days;
		offsetTime.normalize(false);
		mDriver.hotelsSearchScreen().clickDate(offsetTime);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
	}

	public void testNoSearchesLongerThan28Days() {
		String searchError = mRes.getString(R.string.search_error);
		String searchTooLong = mRes.getString(R.string.hotel_search_range_error_TEMPLATE, 28);
		String okString = mRes.getString(R.string.ok);
		doASearchFor(29);
		assertTrue(mDriver.searchText(searchError, true));
		assertTrue(mDriver.searchText(searchTooLong, true));
		mDriver.clickOnText(okString);
	}

	public void testThat28DaySearchesWork() {
		String searchError = mRes.getString(R.string.search_error);
		String searchTooLong = mRes.getString(R.string.hotel_search_range_error_TEMPLATE, 28);
		doASearchFor(28);
		assertFalse(mDriver.searchText(searchError, true));
		assertFalse(mDriver.searchText(searchTooLong, true));
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
