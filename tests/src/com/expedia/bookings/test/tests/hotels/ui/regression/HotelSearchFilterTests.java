package com.expedia.bookings.test.tests.hotels.ui.regression;

import android.widget.TextView;

import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelSearchResultRow;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;

public class HotelSearchFilterTests extends CustomActivityInstrumentationTestCase<PhoneSearchActivity> {

	private static final String TAG = HotelSearchFilterTests.class.getSimpleName();

	public HotelSearchFilterTests() {
		super(PhoneSearchActivity.class);
	}

	// Filter by stars test

	//	 Helper method for testing star filter
	private void assertStarRatings(int minimumStarRating) throws Exception {
		int currentHotelCount = mDriver.hotelsSearchScreen().hotelResultsListView().getCount() - 1;
		int hotelsPerScreenHeight = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 1;
		for (int j = 0; j < currentHotelCount / hotelsPerScreenHeight; j++) {
			for (int i = 0; i < hotelsPerScreenHeight - 1; i++) {
				mDriver.hotelsSearchScreen().selectHotelFromList(i);
				mDriver.delay(1);
				float starRating = mDriver.hotelsDetailsScreen().ratingBar().getRating();
				if (starRating < minimumStarRating) {
					String hotelName = mDriver.hotelsDetailsScreen().titleView().getText().toString();
					throw new Exception("Star rating of hotel: " + hotelName + " is < " + minimumStarRating
							+ " stars, despite the filter.");
				}
				mDriver.delay(1);
				mDriver.goBack();
				mDriver.delay(1);
			}
			mDriver.scrollDown();
		}
	}

	public void testStarFilters() throws Exception {
		mUser.setHotelSearchCity("Los Angeles, CA");
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());

		// Five star filter
		int initialHotelCount = mDriver.hotelsSearchScreen().hotelResultsListView().getCount();
		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickHighRatingFilterButton();
		mDriver.goBack();
		mDriver.delay(1);

		assertStarRatings(5);

		// 4+ star filter
		mDriver.scrollToTop();
		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickMediumRatingFilterButton();
		mDriver.goBack();
		mDriver.delay(1);

		assertStarRatings(4);

		// 3+ star filter
		mDriver.scrollToTop();
		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickLowRatingFilterButton();
		mDriver.goBack();
		mDriver.delay(1);

		assertStarRatings(3);

		// All star filter
		mDriver.scrollToTop();
		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickAllRatingFilterButton();
		mDriver.goBack();
		mDriver.delay(1);

		int currentHotelCount = mDriver.hotelsSearchScreen().hotelResultsListView().getCount();
		if (initialHotelCount != currentHotelCount) {
			throw new Exception(
					"The final hotel count after resetting the star filter doesn't equal the initial count.");
		}
	}

	// TODO: Verify price filter
	// Verify that there are at least as many hotels for bottom filter as there are when filtering
	// for more scrilla

	public void testPriceFilters() throws Exception {
		final int lowerBoundListIndex = 1;
		mUser.setHotelCityToRandomUSCity();
		mUser.setHotelSearchCity(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());

		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickLowPriceFilterButton();
		mDriver.goBack();
		mDriver.hotelsSearchScreen().clickOnSortButton();
		mDriver.hotelsSearchScreen().sortMenu().clickSortByPriceString();

		mDriver.delay();
		mDriver.scrollToBottom();
		mDriver.delay();
		int upperBoundListIndex = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 2;
		HotelSearchResultRow lowPricedBoundRow = mDriver.hotelsSearchScreen().getSearchResultRowModelFromIndex(
				upperBoundListIndex);
		float lowPricedHotelsUpperBound = getCleanFloatFromTextView(lowPricedBoundRow.getPriceTextView());

		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickModeratePriceFilterButton();
		mDriver.goBack();
		HotelSearchResultRow midPricedLowerBoundRow = mDriver.hotelsSearchScreen().getSearchResultRowModelFromIndex(
				lowerBoundListIndex);
		float midPricedHotelsLowerBound = getCleanFloatFromTextView(midPricedLowerBoundRow.getPriceTextView());
		if (midPricedHotelsLowerBound < lowPricedHotelsUpperBound) {
			throw new Exception(
					"The mid priced hotel range lower bound is lower than the low range hotels' upper bound.");
		}
		mDriver.scrollToBottom();

		upperBoundListIndex = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 2;
		HotelSearchResultRow midPricedHotelsUpperBoundRow = mDriver.hotelsSearchScreen()
				.getSearchResultRowModelFromIndex(
						upperBoundListIndex);
		float midPricedHotelsUpperBoundPrice = getCleanFloatFromTextView(midPricedHotelsUpperBoundRow
				.getPriceTextView());

		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickExpensivePriceFilterButton();
		mDriver.goBack();
		mDriver.delay();

		HotelSearchResultRow highPricedLowerBoundRow = mDriver.hotelsSearchScreen().getSearchResultRowModelFromIndex(
				lowerBoundListIndex);
		float highPricedHotelsLowerBound = getCleanFloatFromTextView(highPricedLowerBoundRow.getPriceTextView());

		if (highPricedHotelsLowerBound < midPricedHotelsUpperBoundPrice) {
			throw new Exception(
					"The high priced hotel range lower bound is lower than the mid range hotels' upper bound.");
		}
	}

	// VIP Access filter text

	// Verify that if verify VIP Access is enabled, only VIP access hotels are shown
	// Verify by checking for VIP image view in hotels' galleries
	public void testVIPFilter() throws Exception {
		mUser.setHotelSearchCity("Los Angeles, CA");
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickVIPAccessFilterButton();
		mDriver.goBack();

		int currentHotelCount = mDriver.hotelsSearchScreen().hotelResultsListView().getCount() - 1;
		int hotelsPerScreenHeight = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 1;
		for (int j = 0; j < currentHotelCount / hotelsPerScreenHeight; j++) {
			for (int i = 0; i < hotelsPerScreenHeight - 1; i++) {
				mDriver.hotelsSearchScreen().selectHotelFromList(i);
				mDriver.delay(1);

				if (!mDriver.hotelsDetailsScreen().vipImageView().isShown()) {
					String hotelName = mDriver.hotelsDetailsScreen().titleView().getText().toString();
					throw new Exception("VIP Image View not shown for hotel: " + hotelName);
				}

				mDriver.delay(1);
				mDriver.goBack();
				mDriver.delay(1);
			}
			mDriver.scrollDown();
		}
	}

	private float getCleanFloatFromTextView(TextView t) {
		String str = t.getText().toString();
		str = str.replaceAll("[^\\d.]", "");
		return Float.parseFloat(str);
	}

	// Filter by distance test

	private void assertHotelDistances(double maximumDistance) throws Exception {
		int currentHotelCount = mDriver.hotelsSearchScreen().hotelResultsListView().getCount() - 1;
		int hotelsPerScreenHeight = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 1;
		for (int j = 0; j < currentHotelCount / hotelsPerScreenHeight; j++) {
			for (int i = 0; i < hotelsPerScreenHeight - 1; i++) {
				HotelSearchResultRow row = mDriver.hotelsSearchScreen().getSearchResultRowModelFromIndex(i);
				float distance = getCleanFloatFromTextView(row.getProximityTextView());
				if (distance > maximumDistance) {
					String hotelName = row.getNameTextView().getText().toString();
					throw new Exception("Hotel " + hotelName + " had distance > " + maximumDistance
							+ " from search address.");
				}
			}
			mDriver.scrollDown();
		}
	}

	public void testDistanceFilter() throws Exception {
		mUser.setHotelSearchCity("114 Sansome St., San Francisco, CA 94104");
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());

		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickLargeRadiusFilterButton();
		mDriver.goBack();
		assertHotelDistances(HotelFilter.SearchRadius.LARGE.getRadius(DistanceUnit.MILES));

		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickMediumRadiusFilterButton();
		mDriver.goBack();
		assertHotelDistances(HotelFilter.SearchRadius.MEDIUM.getRadius(DistanceUnit.MILES));

		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickSmallRadiusFilterButton();
		mDriver.goBack();
		assertHotelDistances(HotelFilter.SearchRadius.SMALL.getRadius(DistanceUnit.MILES));
	}

	// Filter by text test

	private static final String[] FILTER_STRINGS = {
		"a",
		"b",
		"Westin",
		"Omni",
		"Marriott",
		"z",
		"Hilton",
	};

	private void assertHotelTitlesContains(String filterText) throws Exception {
		int currentHotelCount = mDriver.hotelsSearchScreen().hotelResultsListView().getCount() - 1;
		int hotelsPerScreenHeight = mDriver.hotelsSearchScreen().hotelResultsListView().getChildCount() - 1;
		if (currentHotelCount != 0 && hotelsPerScreenHeight != 0) {
			for (int j = 0; j < currentHotelCount / hotelsPerScreenHeight; j++) {
				for (int i = 1; i < hotelsPerScreenHeight - 1; i++) {
					HotelSearchResultRow row = mDriver.hotelsSearchScreen().getSearchResultRowModelFromIndex(i);
					String hotelName = row.getNameTextView().getText().toString()
							.toLowerCase(mRes.getConfiguration().locale);
					// If hotel name contains "..." don't test because it could produce false negative
					if (!hotelName.contains("...")) {
						if (!hotelName.contains(filterText.toLowerCase(mRes.getConfiguration().locale))) {
							throw new Exception("Test fails because hotel name " + hotelName
									+ " does not contain the filter text: " + filterText);
						}
					}
				}
				mDriver.scrollDown();
			}
			mDriver.scrollToTop();
		}
	}

	public void testTextFilter() throws Exception {
		mUser.setHotelCityToRandomUSCity();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());

		for (int i = 0; i < FILTER_STRINGS.length; i++) {
			String filterString = FILTER_STRINGS[i];
			mDriver.hotelsSearchScreen().clickOnFilterButton();
			mDriver.clearEditText(mDriver.hotelsSearchScreen().filterMenu().filterEditText());
			mDriver.hotelsSearchScreen().filterMenu().enterFilterText(filterString);
			mDriver.goBack();
			mDriver.delay();
			assertHotelTitlesContains(filterString);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
