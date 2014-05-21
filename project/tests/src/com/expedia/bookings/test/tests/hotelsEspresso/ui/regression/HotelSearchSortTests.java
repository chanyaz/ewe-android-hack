package com.expedia.bookings.test.tests.hotelsEspresso.ui.regression;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.mobiata.android.util.SettingUtils;

import static com.expedia.bookings.test.utilsEspresso.ViewActions.getRating;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 5/20/14.
 */
public class HotelSearchSortTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	public HotelSearchSortTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = HotelSearchSortTests.class.getName();

	Context mContext;
	SharedPreferences mPrefs;
	Resources mRes;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mRes = mContext.getResources();
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		getActivity();
	}

	private float getCleanFloatFromString(String str) {
		str = str.replaceAll("[^\\d.]", "");
		return Float.parseFloat(str);
	}

	private void initiateSearchHelper(String searchDestination) throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText(searchDestination);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
	}

	public void testSortByPrice() throws Exception {
		initiateSearchHelper("Boston, MA");
		HotelsSearchScreen.clickOnSortButton();
		HotelsSearchScreen.sortMenu().clickSortByPriceString();
		EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView(), "totalFlights", 1);
		int totalHotels = mPrefs.getInt("totalFlights", 0);
		if (totalHotels > 1) {
			DataInteraction prevResultRow = HotelsSearchScreen.hotelListItem().atPosition(1);
			EspressoUtils.getListItemValues(prevResultRow, R.id.price_text_view, "previousRowPrice");
			String previousRowPriceString = mPrefs.getString("previousRowPrice", "");
			float previousRowPrice = getCleanFloatFromString(previousRowPriceString);
			for (int j = 1; j < totalHotels - 1; j++) {
				DataInteraction curentResultRow = HotelsSearchScreen.hotelListItem().atPosition(j);
				EspressoUtils.getListItemValues(curentResultRow, R.id.price_text_view, "currentRowPrice");
				String currentRowPriceString = mPrefs.getString("currentRowPrice", "");
				float currentRowPrice = getCleanFloatFromString(currentRowPriceString);
				ScreenActions.enterLog(TAG, "PRICE " + currentRowPrice + " >= " + previousRowPrice);
				assertTrue(currentRowPrice >= previousRowPrice);
				previousRowPrice = currentRowPrice;
			}
		}
		Espresso.pressBack();
	}

	public void testSortByDistance() throws Exception {
		String address = "114 Sansome St. San Francisco, CA 94104";
		ScreenActions.enterLog(TAG, "Test Sort By Distance - address: " + address);
		initiateSearchHelper(address);
		HotelsSearchScreen.clickOnSortButton();
		HotelsSearchScreen.sortMenu().clickSortByDistanceString();
		EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView(), "totalFlights", 1);
		int totalHotels = mPrefs.getInt("totalFlights", 0);
		if (totalHotels > 1) {
			DataInteraction prevResultRow = HotelsSearchScreen.hotelListItem().atPosition(1);
			EspressoUtils.getListItemValues(prevResultRow, R.id.proximity_text_view, "previousRowDistance");
			String previousRowDistanceString = mPrefs.getString("previousRowDistance", "");
			float previousRowDistance = getCleanFloatFromString(previousRowDistanceString);
			for (int j = 1; j < totalHotels - 1; j++) {
				DataInteraction currentHotelRowView = HotelsSearchScreen.hotelListItem().atPosition(j);
				EspressoUtils.getListItemValues(currentHotelRowView, R.id.proximity_text_view, "previousRowDistance");
				String currentRowDistanceString = mPrefs.getString("previousRowDistance", "");
				float currentRowDistance = getCleanFloatFromString(currentRowDistanceString);
				ScreenActions.enterLog(TAG, "DISTANCE " + currentRowDistance + " >= " + previousRowDistance);
				assertTrue(currentRowDistance >= previousRowDistance);
				previousRowDistance = currentRowDistance;
			}
		}
		Espresso.pressBack();
	}

	public void testSortByRating() throws Exception {
		initiateSearchHelper("Boston, MA");
		HotelsSearchScreen.clickOnSortButton();
		HotelsSearchScreen.sortMenu().clickSortByUserRatingString();
		EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView(), "totalFlights", 1);
		int totalHotels = mPrefs.getInt("totalFlights", 0);
		if (totalHotels > 1) {
			DataInteraction prevResultRow = HotelsSearchScreen.hotelListItem().atPosition(1);
			prevResultRow.onChildView(withId(R.id.user_rating_bar)).perform(getRating("previousRowRating"));
			float previousRowRating = mPrefs.getFloat("previousRowRating", 0);
			for (int i = 1; i < totalHotels - 1; i++) {
				DataInteraction currentHotelRowView = HotelsSearchScreen.hotelListItem().atPosition(i);
				currentHotelRowView.onChildView(withId(R.id.user_rating_bar)).perform(getRating("currentRowRating"));
				float currentRowRating = mPrefs.getFloat("currentRowRating", 0);
				ScreenActions.enterLog(TAG, "RATING " + previousRowRating + " >= " + currentRowRating);
				assertTrue(previousRowRating >= currentRowRating);
				previousRowRating = currentRowRating;
			}
		}
		Espresso.pressBack();
	}
}
