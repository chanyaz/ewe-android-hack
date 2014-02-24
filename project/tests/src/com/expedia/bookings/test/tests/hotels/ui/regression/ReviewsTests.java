package com.expedia.bookings.test.tests.hotels.ui.regression;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;

public class ReviewsTests extends ActivityInstrumentationTestCase2<SearchActivity> {

	public ReviewsTests() { //Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}

	private static final String TAG = ReviewsTests.class.getSimpleName();

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsTestDriver mDriver;
	private HotelsUserData mUser;
	private TestPreferences mPreferences;

	protected void setUp() throws Exception {
		super.setUp();
		mRes = getActivity().getBaseContext().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mDriver = new HotelsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser = new HotelsUserData(getActivity());
		mUser.setHotelCityToRandomUSCity();
	}

	public void testNumberOfReviews() throws Exception {
		String hotelSoldOut = mRes.getString(R.string.error_hotel_is_now_sold_out);
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().selectHotelFromList(0);
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());

		//Number of times to scroll down the results list for another set of hotels
		int numberOfHotelSets = 4;
		for (int i = 0; i < numberOfHotelSets; i++) {
			for (int j = 1; j <= 4; j++) {
				mDriver.delay(1);
				mDriver.hotelsSearchScreen().selectHotelFromList(j);
				mDriver.delay(5);
				//Go back to search list if hotel sold out
				if (!mDriver.searchText(hotelSoldOut, 1, false, true)) {
					mDriver.hotelsDetailsScreen().clickBannerView();
					mDriver.waitForStringToBeGone(mDriver.hotelsReviewsScreen().loadingUserReviews());

					// Go to Reviews screen for hotel and get the review count
					verifyReviewsActionBarContentsExist();

					String reviewsTitleString = (String) mDriver.hotelsReviewsScreen().titleView().getText();
					String reviewsScreenNumber = reviewsTitleString.substring(0, reviewsTitleString.indexOf(' '));
					mDriver.hotelsReviewsScreen().clickBackButton();

					// Go back to the hotel details screen and get the review count
					String detailsReviewString = (String) mDriver.hotelsDetailsScreen().reviewsTitle().getText();
					String hotelDetailsNumber = detailsReviewString.substring(0, detailsReviewString.indexOf(' '));

					// test fails if the review counts are not equal
					mDriver.enterLog(TAG, "Hotel reviews showed: " + reviewsScreenNumber
							+ " Hotel details showed: " + hotelDetailsNumber);
					assertEquals(reviewsScreenNumber, hotelDetailsNumber);
					mDriver.goBack();
				}
				else {
					mDriver.clickOnButton(0);
				}
			}
			mDriver.scrollDown();
		}
		mDriver.goBack();
		mDriver.goBack();
	}

	public void testSelectButton() throws Exception {
		mDriver.delay(1);
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.clickInList(1);
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().selectHotelFromList(1);

		mDriver.hotelsDetailsScreen().clickReviewsTitle();

		mDriver.hotelsDetailsScreen().clickSelectButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsRoomsRatesScreen().findingAvailableRooms());
		if (!mDriver.hotelsRoomsRatesScreen().hotelRatingBar().isShown()) {
			mDriver.enterLog(TAG, "Did not make it to rooms and rates screen.");
			fail();
		}
	}

	private void verifyReviewsActionBarContentsExist() throws Exception {
		mDriver.delay();
		mDriver.waitForStringToBeGone(mDriver.hotelsReviewsScreen().loadingUserReviews());
		mDriver.hotelsReviewsScreen().clickCriticalTab();
		mDriver.hotelsReviewsScreen().clickFavorableTab();
		mDriver.hotelsReviewsScreen().clickRecentTab();
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
