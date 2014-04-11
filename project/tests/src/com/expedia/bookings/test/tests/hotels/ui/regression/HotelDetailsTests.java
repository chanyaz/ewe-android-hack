package com.expedia.bookings.test.tests.hotels.ui.regression;

import java.util.HashMap;

import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.data.Property.Amenity;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelSearchResultRow;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;

public class HotelDetailsTests extends CustomActivityInstrumentationTestCase<PhoneSearchActivity> {

	private static final String TAG = HotelDetailsTests.class.getSimpleName();

	public HotelDetailsTests() {
		super(PhoneSearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	// Verify that the correct dialog appears after clicking the VIP Access image in 
	// on the image gallery
	public void testVIPAccessDialog() throws Exception {
		mUser.setHotelCityToRandomUSCity();
		mDriver.enterLog(TAG, "Testing VIP Access Dialog for hotels in city: " + mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());

		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickVIPAccessFilterButton();
		mDriver.goBack();
		int hotelsDisplayed = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 1;
		for (int i = 0; i < hotelsDisplayed; i++) {
			mDriver.hotelsSearchScreen().selectHotelFromList(i);
			mDriver.delay();
			String hotelName = mDriver.hotelsDetailsScreen().titleView().getText().toString();
			mDriver.enterLog(TAG, "Verifying VIP Dialog for hotel: " + hotelName);
			mDriver.hotelsDetailsScreen().clickVIPImageView();
			mDriver.delay(1);
			assertTrue(mDriver.searchText(mDriver.hotelsDetailsScreen().vipAccessMessage()));
			mDriver.hotelsDetailsScreen().clickOnButton(0);
			mDriver.goBack();
		}
	}

	// Verify that some UI Elements are present on the hotel details screen
	public void testDetailsUIElements() throws Exception {
		mUser.setHotelCityToRandomUSCity();
		mDriver.enterLog(TAG, "Search city is: " + mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());

		int hotelsDisplayed = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 1;
		for (int i = 0; i < hotelsDisplayed; i++) {
			HotelSearchResultRow row = mDriver.hotelsSearchScreen().getSearchResultRowModelFromIndex(i);
			String rowHotelName = row.getNameTextView().getText().toString();
			mDriver.hotelsSearchScreen().selectHotelFromList(i);
			mDriver.enterLog(TAG, "Verifying UI elements for details of: " + rowHotelName);
			mDriver.delay();
			if (!rowHotelName.isEmpty() && !rowHotelName.contains("...")) {
				String detailHotelsName = mDriver.hotelsDetailsScreen().titleView().getText().toString();
				mDriver.enterLog(TAG, "Testing that the hotel name: " + rowHotelName + " matches " + detailHotelsName);
				assertEquals(rowHotelName, detailHotelsName);
			}
			assertTrue(mDriver.hotelsDetailsScreen().ratingBar().isShown());
			assertTrue(mDriver.hotelsDetailsScreen().hotelGallery().isShown());
			mDriver.scrollToBottom();
			mDriver.delay();
			mDriver.scrollDown();
			assertTrue(mDriver.hotelsDetailsScreen().bookNowButton().isShown());
			mDriver.goBack();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
