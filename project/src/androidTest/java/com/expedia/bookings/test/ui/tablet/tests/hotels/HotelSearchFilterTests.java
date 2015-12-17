package com.expedia.bookings.test.ui.tablet.tests.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Settings;
import com.expedia.bookings.test.ui.tablet.pagemodels.SortFilter;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;
import android.support.test.espresso.DataInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 6/9/14.
 */
public class HotelSearchFilterTests extends TabletTestCase {

	private static final String TAG = HotelSearchFilterTests.class.getName();

	// Filter by stars test

	// Helper method for testing star filter
	private void assertStarRatings(int minimumStarRating) throws Exception {
		int currentHotelCount = EspressoUtils.getListCount(Results.hotelList());
		for (int j = 1; j < currentHotelCount - 1; j++) {
			Results.clickHotelAtIndex(j);
			float starRating = EspressoUtils.getRatingValue(onView(withId(R.id.star_rating_bar)));
			Common.enterLog(TAG, "Star rating and minimum rating:" + starRating + "," + minimumStarRating);
			if (starRating < minimumStarRating) {
				String hotelName = EspressoUtils.getText(R.id.title);
				throw new Exception("Star rating of hotel: " + hotelName + " is < " + minimumStarRating + " stars, despite the filter.");
			}
			Common.pressBack();
		}
	}

	public void testStarFilters() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Results.swipeUpHotelList();

		// Five star filter
		int initialHotelCount = EspressoUtils.getListCount(Results.hotelList());

		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickHighRatingFilterButton();
		Common.pressBack();
		assertStarRatings(5);

		// 4+ star filter
		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickMediumRatingFilterButton();
		Common.pressBack();
		assertStarRatings(4);

		// 3+ star filter
		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickLowRatingFilterButton();
		Common.pressBack();
		assertStarRatings(3);

		// All star filter
		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickAllRatingFilterButton();
		Common.pressBack();

		int currentHotelCount = EspressoUtils.getListCount(Results.hotelList());

		if (initialHotelCount != currentHotelCount) {
			throw new Exception(
				"The final hotel count after resetting the star filter doesn't equal the initial count.");
		}
	}

	// VIP Access filter text

	// Verify that if verify VIP Access is enabled, only VIP access hotels are shown
	// Verify by checking for VIP image view in hotels' galleries
	public void testVIPFilter() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Results.swipeUpHotelList();
		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickVIPAccessFilterButton();
		Common.pressBack();

		int currentHotelCount = EspressoUtils.getListCount(Results.hotelList()) - 1;
		for (int j = 1; j < currentHotelCount; j = j + 1) {
			Results.clickHotelAtIndex(j);
			Common.checkDisplayed(SortFilter.vipImageView());
			Common.pressBack();
		}
	}

	private float getCleanFloatFromTextView(String str) {
		str = str.replaceAll("[^\\d.]", "");
		return Float.parseFloat(str);
	}

	// Filter by distance test

	private void assertHotelDistances(double maximumDistance) throws Exception {
		int currentHotelCount = EspressoUtils.getListCount(Results.hotelList()) - 1;
		for (int j = 1; j < currentHotelCount; j++) {
			DataInteraction searchResultRow = Results.hotelAtIndex(j);
			String distanceString = EspressoUtils.getListItemValues(searchResultRow, R.id.proximity_text_view);
			float distance = getCleanFloatFromTextView(distanceString);
			Common.enterLog(TAG, "Distance and maximum distance:" + distance + "," + maximumDistance);
			if (distance > maximumDistance) {
				String hotelName = EspressoUtils.getListItemValues(searchResultRow, R.id.name_text_view);
				throw new Exception("Hotel " + hotelName + " had distance > " + maximumDistance
					+ " from search address.");
			}
		}
	}

	public void testDistanceFilter() throws Exception {
		Settings.setFakeCurrentLocation(getInstrumentation(), "37.7914", "-122.401");

		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.clickSuggestion("Current Location");
		Results.swipeUpHotelList();
		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickLargeRadiusFilterButton();
		Common.pressBack();
		assertHotelDistances(HotelFilter.SearchRadius.LARGE.getRadius(Distance.DistanceUnit.MILES));

		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickMediumRadiusFilterButton();
		Common.pressBack();
		assertHotelDistances(HotelFilter.SearchRadius.MEDIUM.getRadius(Distance.DistanceUnit.MILES));

		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickSmallRadiusFilterButton();
		Common.pressBack();
		assertHotelDistances(HotelFilter.SearchRadius.SMALL.getRadius(Distance.DistanceUnit.MILES));
	}

	// Filter by text test

	private static final String[] FILTER_STRINGS = {
		"a",
		"b",
		"Tuscan",
		"z",
	};

	private void assertHotelTitlesContains(String filterText) throws Exception {
		int currentHotelCount = EspressoUtils.getListCount(Results.hotelList()) - 1;
		Common.enterLog(TAG, "Hotel count after adding filter text :" + currentHotelCount);
		if (currentHotelCount != 0) {
			for (int j = 1; j < currentHotelCount - 1; j++) {
				DataInteraction searchResultRow = Results.hotelAtIndex(j);
				String hotelName = EspressoUtils.getListItemValues(searchResultRow, R.id.name_text_view).toLowerCase(getActivity().getResources().getConfiguration().locale);
				Common.enterLog(TAG, "Hotel name in text view:" + hotelName);

				// If hotel name contains "..." don't test because it could produce false negative
				if (!hotelName.contains("...")) {
					if (!hotelName.contains(filterText.toLowerCase(getActivity().getResources().getConfiguration().locale))) {
						throw new Exception("Test fails because hotel name " + hotelName
							+ " does not contain the filter text: " + filterText);
					}
				}
			}
		}
	}

	public void testTextFilter() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Results.swipeUpHotelList();

		for (int i = 0; i < FILTER_STRINGS.length; i++) {
			String filterString = FILTER_STRINGS[i];
			SortFilter.clickHotelSortFilterButton();
			SortFilter.clearFilterText();
			SortFilter.enterFilterText(filterString);
			Common.closeSoftKeyboard(SortFilter.filterEditText());
			Common.pressBack();
			assertHotelTitlesContains(filterString);
		}
	}
}

