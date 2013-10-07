package com.expedia.bookings.test.tests.hotels.ui.regression;

import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;

public class HotelRoomsAndRatesTests extends CustomActivityInstrumentationTestCase<PhoneSearchActivity> {

	private static final String TAG = HotelRoomsAndRatesTests.class.getSimpleName();

	public HotelRoomsAndRatesTests() {
		super(PhoneSearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void initiateSearch() throws Exception {
		mUser.setHotelCityToRandomUSCity();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnCalendarButton();
		mDriver.hotelsSearchScreen().clickDate(2);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
	}

	public void testRNRHeaderInfo() throws Exception {
		initiateSearch();
		int numberOfHotelsToCheck = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 1;
		final int totalNumberOfHotels = mDriver.hotelsSearchScreen().hotelResultsListView().getCount();
		for (int i = 0; i < (totalNumberOfHotels / numberOfHotelsToCheck); i++) {
			for (int j = 0; j < numberOfHotelsToCheck; j++) {
				mDriver.hotelsSearchScreen().selectHotelFromList(j);
				mDriver.delay();
				String detailsHotelName = mDriver.hotelsDetailsScreen().titleView().getText().toString();
				float detailsHotelRating = mDriver.hotelsDetailsScreen().ratingBar().getRating();
				mDriver.hotelsDetailsScreen().clickSelectButton();
				String roomsRatesHotelName = mDriver.hotelsRoomsRatesScreen().hotelNameTextView().getText().toString();
				float roomsRatesHotelRating = mDriver.hotelsRoomsRatesScreen().hotelRatingBar().getRating();
				mDriver.enterLog(TAG, "Hotel details showed name as: " + detailsHotelName
						+ " - Rooms & Rates showed name as: " + roomsRatesHotelName);
				mDriver.enterLog(TAG, "Hotel details showed rating as: " + detailsHotelRating
						+ " - Rooms & Rates showed rating as: " + roomsRatesHotelRating);
				assertEquals(detailsHotelName, roomsRatesHotelName);
				assertEquals(detailsHotelRating, roomsRatesHotelRating);
				assertUIElementsAreVisible();
				mDriver.delay(1);
				mDriver.goBack();
				mDriver.goBack();
				mDriver.delay(1);
			}
			mDriver.scrollDown();
			mDriver.delay(1);
			numberOfHotelsToCheck = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 1;
		}
	}

	private void assertUIElementsAreVisible() {
		assertTrue(mDriver.searchText(mDriver.hotelsRoomsRatesScreen().selectARoom()));
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
