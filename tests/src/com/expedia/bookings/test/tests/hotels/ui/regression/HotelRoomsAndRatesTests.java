package com.expedia.bookings.test.tests.hotels.ui.regression;

import android.widget.TextView;

import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.test.tests.pageModels.hotels.RoomsAndRatesRow;
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
				checkAdditionalFees();
				checkRenovationNotice();

				int numberOfRooms = mDriver.hotelsRoomsRatesScreen().roomList().getChildCount() - 1;
				for (int k = 0; k < numberOfRooms; k++) {
					mDriver.enterLog(TAG, "Checking room listing at index: " + k);
					RoomsAndRatesRow roomsRatesRow = mDriver.hotelsRoomsRatesScreen().getRowModelAtIndex(k);
					TextView bedsTextView;
					TextView roomTextView;

					try {
						bedsTextView = roomsRatesRow.bedsTextView();
					}
					catch (Exception e) {
						mDriver.enterLog(TAG, "Bed Type text view wasn't accessible. Set to null.");
						bedsTextView = null;
					}

					try {
						roomTextView = roomsRatesRow.roomTypeTextView();
					}
					catch (Exception e) {
						mDriver.enterLog(TAG, "Room Type text view wasn't accessible. Set to null.");
						roomTextView = null;
					}

					mDriver.scrollToTop();
					if (bedsTextView != null && roomTextView != null) {
						assertFalse(roomsRatesRow.bedsTextView().getText().equals("")
								&& roomsRatesRow.roomTypeTextView().getText().equals(""));
						mDriver.enterLog(TAG,
								"Asserted that one of bed type or room type textviews contained text for this room listing.");
					}
					assertFalse(roomsRatesRow.priceTextView().getText().equals(""));
					mDriver.enterLog(TAG, "Asserted that a price was present for this room listing");

					if (roomsRatesRow.saleTextView() != null) {
						if (!roomsRatesRow.saleTextView().getText().equals("")) {
							assertFalse(roomsRatesRow.priceExplanationTextView().getText().equals(""));
							mDriver.enterLog(TAG,
									"Asserted that if there is a sale price for this room, the original price is displayed with a strikethrough");
						}
					}
				}
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
		assertNotNull(mDriver.hotelsRoomsRatesScreen().thumbnailImageView().getDrawable());
	}

	private void checkAdditionalFees() {
		mDriver.scrollToTop();
		if (mDriver.searchText(mDriver.hotelsRoomsRatesScreen().additionalFeesString(), 1, false, true)) {
			assertTrue(mDriver.searchText(mDriver.hotelsRoomsRatesScreen().feesNotIncludedString(), 1, false, true));
			mDriver.hotelsRoomsRatesScreen().clickAdditionalFeesInfoButton();
			mDriver.delay();
			assertTrue(mDriver.searchText(mDriver.hotelsRoomsRatesScreen().additionalFeesString(), 1, false, true));
			mDriver.goBack();
		}
	}

	private void checkRenovationNotice() {
		mDriver.scrollToTop();
		if (mDriver.searchText(mDriver.hotelsRoomsRatesScreen().renovationNoticeString(), 1, false, true)) {
			assertTrue(mDriver.searchText(mDriver.hotelsRoomsRatesScreen().propertyRenovationString(), 1, false, true));
			mDriver.hotelsRoomsRatesScreen().clickRenovationInfoButton();
			mDriver.delay();
			assertTrue(mDriver.searchText(mDriver.hotelsRoomsRatesScreen().renovationNoticeString(), 1, false, true));
			mDriver.goBack();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
