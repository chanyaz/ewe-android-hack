package com.expedia.bookings.test.ui.phone.tests.hotels;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.Espresso;

import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

/**
 * Created by dmadan on 5/15/14.
 */
public class HotelSearchFilterTests extends PhoneTestCase {

	private static final String TAG = HotelSearchFilterTests.class.getName();

	// Filter by stars test

	//	 Helper method for testing star filter
	private void assertStarRatings(int minimumStarRating) throws Exception {
		int currentHotelCount = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView());
		for (int j = 1; j < currentHotelCount - 1; j = j + 3) {
			HotelsSearchScreen.clickListItem(j);
			float starRating = EspressoUtils.getRatingValue(HotelsDetailsScreen.ratingBar());
			ScreenActions.enterLog(TAG, "Star rating and minimum rating:" + starRating + "," + minimumStarRating);
			if (starRating < minimumStarRating) {
				String hotelName = EspressoUtils.getText(R.id.title);
				throw new Exception("Star rating of hotel: " + hotelName + " is < " + minimumStarRating + " stars, despite the filter.");
			}
			Espresso.pressBack();
		}
	}

	public void testStarFilters() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Boston, MA");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		// Five star filter
		int initialHotelCount = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView());

		HotelsSearchScreen.clickOnFilterButton();
		HotelsSearchScreen.filterMenu().clickHighRatingFilterButton();
		Espresso.pressBack();
		assertStarRatings(5);

		// 4+ star filter
		HotelsSearchScreen.clickOnFilterButton();
		HotelsSearchScreen.filterMenu().clickMediumRatingFilterButton();
		Espresso.pressBack();
		assertStarRatings(4);

		// 3+ star filter
		HotelsSearchScreen.clickOnFilterButton();
		HotelsSearchScreen.filterMenu().clickLowRatingFilterButton();
		Espresso.pressBack();
		assertStarRatings(3);

		// All star filter
		HotelsSearchScreen.clickOnFilterButton();
		HotelsSearchScreen.filterMenu().clickAllRatingFilterButton();
		Espresso.pressBack();

		int currentHotelCount = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView());

		if (initialHotelCount != currentHotelCount) {
			throw new Exception(
				"The final hotel count after resetting the star filter doesn't equal the initial count.");
		}
	}

	// VIP Access filter text

	// Verify that if verify VIP Access is enabled, only VIP access hotels are shown
	// Verify by checking for VIP image view in hotels' galleries
	public void testVIPFilter() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Boston, MA");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		HotelsSearchScreen.clickOnFilterButton();
		HotelsSearchScreen.filterMenu().clickVIPAccessFilterButton();
		Espresso.pressBack();

		int currentHotelCount = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView()) - 1;
		for (int j = 1; j < currentHotelCount; j = j + 1) {
			HotelsSearchScreen.clickListItem(j);
			try {
				HotelsDetailsScreen.vipImageView().check(matches(isDisplayed()));
			}
			catch (Exception e) {
				String hotelName = EspressoUtils.getText(R.id.title);
				ScreenActions.enterLog(TAG, "VIP Image View not shown for hotel: " + hotelName);
				fail();
			}
			Espresso.pressBack();
		}
	}

	private float getCleanFloatFromTextView(String str) {
		str = str.replaceAll("[^\\d.]", "");
		return Float.parseFloat(str);
	}

	// Filter by distance test

	private void assertHotelDistances(double maximumDistance) throws Exception {
		int currentHotelCount = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView()) - 1;
		for (int j = 1; j < currentHotelCount; j++) {
			DataInteraction searchResultRow = HotelsSearchScreen.hotelListItem().atPosition(j);
			String distanceString = EspressoUtils.getListItemValues(searchResultRow, R.id.proximity_text_view);
			float distance = getCleanFloatFromTextView(distanceString);
			ScreenActions.enterLog(TAG, "Distance and maximum distance:" + distance + "," + maximumDistance);
			if (distance > maximumDistance) {
				String hotelName = EspressoUtils.getListItemValues(searchResultRow, R.id.name_text_view);
				throw new Exception("Hotel " + hotelName + " had distance > " + maximumDistance
					+ " from search address.");
			}
		}
	}

	public void testDistanceFilter() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("114 Sansome St., San Francisco, CA 94104");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);

		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickOnFilterButton();
		HotelsSearchScreen.filterMenu().clickLargeRadiusFilterButton();
		Espresso.pressBack();
		assertHotelDistances(HotelFilter.SearchRadius.LARGE.getRadius(Distance.DistanceUnit.MILES));

		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickOnFilterButton();
		HotelsSearchScreen.filterMenu().clickMediumRadiusFilterButton();
		Espresso.pressBack();
		assertHotelDistances(HotelFilter.SearchRadius.MEDIUM.getRadius(Distance.DistanceUnit.MILES));

		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickOnFilterButton();
		HotelsSearchScreen.filterMenu().clickSmallRadiusFilterButton();
		Espresso.pressBack();
		assertHotelDistances(HotelFilter.SearchRadius.SMALL.getRadius(Distance.DistanceUnit.MILES));
	}

// Filter by text test

	private static final String[] FILTER_STRINGS = {
		"a",
		"b",
		"Marriott",
		"z",
		"Hilton",
	};

	private void assertHotelTitlesContains(String filterText) throws Exception {
		int currentHotelCount = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView()) - 1;
		ScreenActions.enterLog(TAG, "Hotel count after adding filter text :" + currentHotelCount);
		if (currentHotelCount != 0) {
			for (int j = 1; j < currentHotelCount - 1; j++) {
				DataInteraction searchResultRow = HotelsSearchScreen.hotelListItem().atPosition(j);
				String hotelName = EspressoUtils.getListItemValues(searchResultRow, R.id.name_text_view).toLowerCase(getActivity().getResources().getConfiguration().locale);
				ScreenActions.enterLog(TAG, "Hotel name in text view:" + hotelName);

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
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		for (int i = 0; i < FILTER_STRINGS.length; i++) {
			String filterString = FILTER_STRINGS[i];
			HotelsSearchScreen.clickOnFilterButton();
			HotelsSearchScreen.filterMenu().filterEditText().perform(clearText());
			HotelsSearchScreen.filterMenu().enterFilterText(filterString);
			Espresso.pressBack();
			assertHotelTitlesContains(filterString);
		}
	}
}
