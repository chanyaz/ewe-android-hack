package com.expedia.bookings.test.tests.hotels;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class ReviewsTests extends ActivityInstrumentationTestCase2<SearchActivity> {

	private Solo mSolo;
	private static final String TAG = "Reviews Tests";
	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsRobotHelper mDriver;
	private HotelsUserData mUser;

	public ReviewsTests() { //Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mSolo = new Solo(getInstrumentation(), getActivity());
		mUser = new HotelsUserData();
		mRes = getActivity().getBaseContext().getResources();
		mMetric = mRes.getDisplayMetrics();
		mDriver = new HotelsRobotHelper(mSolo, mRes, mUser);

		mDriver.setScreenshotCount(1);
		mDriver.setAllowOrientationChange(false);
		mDriver.setWriteEventsToFile(false);
		mUser.setHotelCityToRandomUSCity();
	}

	public void testNumberOfReviews() throws Exception {
		View user_rating_text_view;
		TextView reviewsTitleView;
		String reviewsTitleString;
		String reviewsScreenNumber;
		
		TextView detailsReviewTextView;
		String detailsReviewString;
		String hotelDetailsNumber;

		String hotelSoldOut = mRes.getString(R.string.error_hotel_is_now_sold_out);
		mDriver.launchHotels();
		mDriver.selectLocation(mUser.mHotelSearchCity);
		for (int i = 0; i < 4; i++) {
			for (int j = 2; j <= 4; j++) {
				mSolo.clickInList(j);
				mDriver.delay(5);
				//Go back to search list if hotel sold out
				if (!mSolo.searchText(hotelSoldOut, 1, false, true)) {
					user_rating_text_view =
							mSolo.getCurrentActivity().findViewById(R.id.user_rating_text_view);
					mSolo.clickOnView(user_rating_text_view);
					mDriver.delay();

					// Go to Reviews screen for hotel and get the review count
					verifyReviewsActionBarContentsExist();
					reviewsTitleView = (TextView) mSolo.getView(R.id.title);
					reviewsTitleString = (String) reviewsTitleView.getText();
					reviewsScreenNumber = reviewsTitleString.substring(0, reviewsTitleString.indexOf(' '));
					mSolo.goBack();

					// Go back to the hotel details screen and get the review count
					detailsReviewTextView = (TextView) mSolo.getView(R.id.user_rating_text_view);
					detailsReviewString = (String) detailsReviewTextView.getText();
					hotelDetailsNumber = detailsReviewString.substring(0, detailsReviewString.indexOf(' '));

					// test fails if the review counts are not equal
					if (!reviewsScreenNumber.equals(hotelDetailsNumber)) {
						mDriver.enterLog(TAG, "Test failed: hotel reviews screen showed " + detailsReviewString
								+ ". Hotel details showed " + hotelDetailsNumber);
						fail();
					}
					mSolo.goBack();
				}
				else {
					mSolo.clickOnButton(0);
				}
			}
			mSolo.scrollDown();
		}
	}

	private void verifyReviewsActionBarContentsExist() {
		String critical = mRes.getString(R.string.user_review_sort_button_critical);
		String favorable = mRes.getString(R.string.user_review_sort_button_favorable);
		String recent = mRes.getString(R.string.user_review_sort_button_recent);
		String select = mRes.getString(R.string.select);

		mDriver.delay();
		if (!mSolo.searchText(critical, 1, false, true)) {
			mDriver.enterLog(TAG, "Couldn't find 'Critical' string");
			fail();
		}
		if (!mSolo.searchText(favorable, 1, false, true)) {
			mDriver.enterLog(TAG, "Couldn't find 'Favorable' string");
			fail();
		}
		if (!mSolo.searchText(recent, 1, false, true)) {
			mDriver.enterLog(TAG, "Couldn't find 'Recent' string");
			fail();
		}
		if (!mSolo.searchText(select, 1, false, true)) {
			mDriver.enterLog(TAG, "Couldn't find 'Select' string");
			fail();
		}
	}
}
