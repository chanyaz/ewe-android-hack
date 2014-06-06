package com.expedia.bookings.test.tests.tablet.Hotels.ui.regression;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.mobiata.android.util.SettingUtils;

import static com.expedia.bookings.test.utils.EspressoUtils.slowSwipeUp;
import static com.expedia.bookings.test.utilsEspresso.ViewActions.getRating;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 6/5/14.
 */
public class HotelSearchSortTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	public HotelSearchSortTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = HotelSearchSortTests.class.getSimpleName();
	Context mContext;
	SharedPreferences mPrefs;
	Resources mRes;
	HotelsUserData mUser;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mRes = mContext.getResources();
		mUser = new HotelsUserData(getInstrumentation());
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		getActivity();
	}

	private float getCleanFloatFromString(String str) {
		str = str.replaceAll("[^\\d.]", "");
		return Float.parseFloat(str);
	}

	private void initiateSearchHelper() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Results.hotelList().perform(slowSwipeUp());
	}

	public void testSortByPrice() throws Exception {
		initiateSearchHelper();
		Results.clickHotelSortFilterButton();
		Results.clickToSortHotelByPrice();
		EspressoUtils.getListCount(Results.hotelList(), "totalHotels", 1);
		int totalHotels = mPrefs.getInt("totalHotels", 0);
		if (totalHotels > 1) {
			DataInteraction prevResultRow = Results.hotelAtIndex(1);
			EspressoUtils.getListItemValues(prevResultRow, R.id.price_text_view, "previousRowPrice");
			String previousRowPriceString = mPrefs.getString("previousRowPrice", "");
			float previousRowPrice = getCleanFloatFromString(previousRowPriceString);
			for (int j = 1; j < totalHotels - 1; j++) {
				DataInteraction curentResultRow = Results.hotelAtIndex(j);
				EspressoUtils.getListItemValues(curentResultRow, R.id.price_text_view, "currentRowPrice");
				String currentRowPriceString = mPrefs.getString("currentRowPrice", "");
				float currentRowPrice = getCleanFloatFromString(currentRowPriceString);
				ScreenActions.enterLog(TAG, "PRICE " + currentRowPrice + " >= " + previousRowPrice);
				assertTrue(currentRowPrice >= previousRowPrice);
				previousRowPrice = currentRowPrice;
			}
		}
	}

	public void testSortByRating() throws Exception {
		initiateSearchHelper();
		Results.clickHotelSortFilterButton();
		Results.clickToSortHotelByRating();
		EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView(), "totalHotels", 1);
		int totalHotels = mPrefs.getInt("totalHotels", 0);
		if (totalHotels > 1) {
			DataInteraction prevResultRow = Results.hotelAtIndex(1);
			prevResultRow.onChildView(withId(R.id.user_rating_bar)).perform(getRating("previousRowRating"));
			float previousRowRating = mPrefs.getFloat("previousRowRating", 0);
			for (int i = 1; i < totalHotels - 1; i++) {
				DataInteraction currentHotelRowView = Results.hotelAtIndex(i);
				currentHotelRowView.onChildView(withId(R.id.user_rating_bar)).perform(getRating("currentRowRating"));
				float currentRowRating = mPrefs.getFloat("currentRowRating", 0);
				ScreenActions.enterLog(TAG, "RATING " + previousRowRating + " >= " + currentRowRating);
				assertTrue(previousRowRating >= currentRowRating);
				previousRowRating = currentRowRating;
			}
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
