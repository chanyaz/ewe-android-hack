package com.expedia.bookings.test.ui.phone.tests.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsReviewsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;
import android.support.test.espresso.Espresso;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 5/12/14.
 */
public class ReviewsTests extends PhoneTestCase {

	private static final String TAG = ReviewsTests.class.getName();

	//tests number of reviews on Hotel reviews screen is equal to number of reviews on Hotel details screen
	public void testNumberOfReviews() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		ScreenActions.enterLog(TAG, "Clicking suggestion");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		int totalHotels = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView());

		//Number of times to scroll down the results list for another set of hotels
		for (int i = 1; i < totalHotels; i++) {
			HotelsSearchScreen.clickListItem(i);
			//Go back to search list if hotel sold out
			try {
				HotelsDetailsScreen.clickBannerView();

				// Go to Reviews screen for hotel and get the review count
				verifyReviewsActionBarContentsExist();
				String reviewsTitleString = EspressoUtils.getText(R.id.title);
				ScreenActions.enterLog(TAG, "reviewsTitleString: " + reviewsTitleString);
				String reviewsScreenNumber = reviewsTitleString.substring(0, reviewsTitleString.indexOf(' '));
				Espresso.pressBack();

				// Go back to the hotel details screen and get the review count
				String detailsReviewString = EspressoUtils.getText(R.id.user_rating_text_view);
				ScreenActions.enterLog(TAG, "detailsReviewString: " + detailsReviewString);
				String hotelDetailsNumber = detailsReviewString.substring(0, detailsReviewString.indexOf(' '));

				// test fails if the review counts are not equal
				ScreenActions.enterLog(TAG, "Hotel reviews showed: " + reviewsScreenNumber + " Hotel details showed: " + hotelDetailsNumber);
				assertEquals(reviewsScreenNumber, hotelDetailsNumber);
				Espresso.pressBack();
			}
			catch (Exception e) {
				onView(withText("OK")).perform(click());
			}
		}
		Espresso.pressBack();
	}

	public void testSelectButton() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		ScreenActions.enterLog(TAG, "Clicking suggestion");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		HotelsSearchScreen.clickListItem(1);
		HotelsDetailsScreen.clickReviewsTitle();
		HotelsDetailsScreen.clickSelectButton();
		try {
			HotelsRoomsRatesScreen.hotelRatingBar().check(matches(isDisplayed()));
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "Did not make it to rooms and rates screen.");
			fail();
		}
	}

	private void verifyReviewsActionBarContentsExist() throws Exception {
		HotelsReviewsScreen.clickCriticalTab();
		HotelsReviewsScreen.clickFavorableTab();
		HotelsReviewsScreen.clickRecentTab();
	}
}
