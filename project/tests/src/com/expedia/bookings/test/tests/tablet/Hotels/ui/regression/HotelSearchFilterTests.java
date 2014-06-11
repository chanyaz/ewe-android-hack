package com.expedia.bookings.test.tests.tablet.Hotels.ui.regression;

import android.content.Context;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.tests.pageModels.tablet.SortFilter;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.mobiata.android.util.SettingUtils;

import static com.expedia.bookings.test.tests.pageModels.tablet.Common.pressBack;
import static com.expedia.bookings.test.utils.EspressoUtils.slowSwipeUp;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 6/9/14.
 */
public class HotelSearchFilterTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	public HotelSearchFilterTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = HotelSearchFilterTests.class.getName();

	Context mContext;
	Resources mRes;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mRes = mContext.getResources();
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		getActivity();
	}

	// Filter by stars test

	//	 Helper method for testing star filter
	private void assertStarRatings(int minimumStarRating) throws Exception {
		int currentHotelCount = EspressoUtils.getListCount(Results.hotelList());
		for (int j = 1; j < currentHotelCount - 1; j++) {
			Results.clickHotelAtIndex(j);
			float starRating = EspressoUtils.getRatingValue(onView(withId(R.id.star_rating_bar)));
			ScreenActions.enterLog(TAG, "Star rating and minimum rating:" + starRating + "," + minimumStarRating);
			if (starRating < minimumStarRating) {
				String hotelName = EspressoUtils.getText(R.id.title);
				throw new Exception("Star rating of hotel: " + hotelName + " is < " + minimumStarRating + " stars, despite the filter.");
			}
			pressBack();
		}
	}

	public void testStarFilters() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Results.hotelList().perform(slowSwipeUp());

		// Five star filter
		int initialHotelCount = EspressoUtils.getListCount(Results.hotelList());

		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickHighRatingFilterButton();
		pressBack();
		assertStarRatings(5);

		// 4+ star filter
		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickMediumRatingFilterButton();
		pressBack();
		assertStarRatings(4);

		// 3+ star filter
		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickLowRatingFilterButton();
		pressBack();
		assertStarRatings(3);

		// All star filter
		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickAllRatingFilterButton();
		pressBack();

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
		Results.hotelList().perform(slowSwipeUp());
		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickVIPAccessFilterButton();
		pressBack();

		int currentHotelCount = EspressoUtils.getListCount(Results.hotelList()) - 1;
		for (int j = 1; j < currentHotelCount; j = j + 1) {
			Results.clickHotelAtIndex(j);
			Common.checkDisplayed(SortFilter.vipImageView());
			pressBack();
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
			ScreenActions.enterLog(TAG, "Distance and maximum distance:" + distance + "," + maximumDistance);
			if (distance > maximumDistance) {
				String hotelName = EspressoUtils.getListItemValues(searchResultRow, R.id.name_text_view);
				throw new Exception("Hotel " + hotelName + " had distance > " + maximumDistance
					+ " from search address.");
			}
		}
	}

	public void testDistanceFilter() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.clickSuggestion("Current Location");
		Results.hotelList().perform(slowSwipeUp());
		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickLargeRadiusFilterButton();
		pressBack();
		assertHotelDistances(HotelFilter.SearchRadius.LARGE.getRadius(Distance.DistanceUnit.MILES));

		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickMediumRadiusFilterButton();
		pressBack();
		assertHotelDistances(HotelFilter.SearchRadius.MEDIUM.getRadius(Distance.DistanceUnit.MILES));

		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickSmallRadiusFilterButton();
		pressBack();
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
		int currentHotelCount = EspressoUtils.getListCount(Results.hotelList()) - 1;
		ScreenActions.enterLog(TAG, "Hotel count after adding filter text :" + currentHotelCount);
		if (currentHotelCount != 0) {
			for (int j = 1; j < currentHotelCount - 1; j++) {
				DataInteraction searchResultRow = Results.hotelAtIndex(j);
				String hotelName = EspressoUtils.getListItemValues(searchResultRow, R.id.name_text_view).toLowerCase(mRes.getConfiguration().locale);
				ScreenActions.enterLog(TAG, "Hotel name in text view:" + hotelName);

				// If hotel name contains "..." don't test because it could produce false negative
				if (!hotelName.contains("...")) {
					if (!hotelName.contains(filterText.toLowerCase(mRes.getConfiguration().locale))) {
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
		Results.hotelList().perform(slowSwipeUp());

		for (int i = 0; i < FILTER_STRINGS.length; i++) {
			String filterString = FILTER_STRINGS[i];
			SortFilter.clickHotelSortFilterButton();
			SortFilter.filterEditText().perform(clearText());
			SortFilter.enterFilterText(filterString);
			Common.closeSoftKeyboard(SortFilter.filterEditText());
			pressBack();
			assertHotelTitlesContains(filterString);
		}
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		// These tests are only applicable to phones
		if (ExpediaBookingApp.useTabletInterface(getInstrumentation().getTargetContext())) {
			Common.pressBackOutOfApp();
		}
	}
}

