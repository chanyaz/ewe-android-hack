package com.expedia.bookings.test.tests.hotelsEspresso.ui.regression;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.mobiata.android.util.SettingUtils;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;

/**
 * Created by dmadan on 5/15/14.
 */
public class HotelSearchFilterTests extends ActivityInstrumentationTestCase2<PhoneSearchActivity> {
	public HotelSearchFilterTests() {
		super(PhoneSearchActivity.class);
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
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Boston, MA");
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		LocalDate mStartDate = new LocalDate(year, month, 5);
		LocalDate mEndDate = new LocalDate(year, month, 10);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);
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
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Boston, MA");
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		LocalDate mStartDate = new LocalDate(year, month, 5);
		LocalDate mEndDate = new LocalDate(year, month, 10);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);
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
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("114 Sansome St., San Francisco, CA 94104");
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		LocalDate mStartDate = new LocalDate(year, month, 5);
		LocalDate mEndDate = new LocalDate(year, month, 10);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);

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
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		LocalDate mStartDate = new LocalDate(year, month, 5);
		LocalDate mEndDate = new LocalDate(year, month, 10);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);
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
