package com.expedia.bookings.test.tests.tablet.Hotels.ui.regression;

import java.util.concurrent.atomic.AtomicReference;

import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.tests.pageModels.tablet.Settings;
import com.expedia.bookings.test.tests.pageModels.tablet.SortFilter;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;

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

	@Override
	public void runTest() throws Throwable {
		// These tests are only applicable to tablets
		if (ExpediaBookingApp.useTabletInterface(getInstrumentation().getTargetContext())) {

			Settings.clearPrivateData(getInstrumentation());
			// Point to the mock server
			Settings.setCustomServer(getInstrumentation(), "mocke3.mobiata.com");

			// Espresso will not launch our activity for us, we must launch it via getActivity().
			getActivity();

			super.runTest();
		}
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
		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickToSortHotelByPrice();
		int totalHotels = EspressoUtils.getListCount(Results.hotelList());
		if (totalHotels > 1) {
			DataInteraction prevResultRow = Results.hotelAtIndex(1);
			String previousRowPriceString = EspressoUtils.getListItemValues(prevResultRow, R.id.price_text_view);
			float previousRowPrice = getCleanFloatFromString(previousRowPriceString);
			for (int j = 1; j < totalHotels - 1; j++) {
				DataInteraction curentResultRow = Results.hotelAtIndex(j);
				String currentRowPriceString = EspressoUtils.getListItemValues(curentResultRow, R.id.price_text_view);
				float currentRowPrice = getCleanFloatFromString(currentRowPriceString);
				Common.enterLog(TAG, "PRICE " + currentRowPrice + " >= " + previousRowPrice);
				assertTrue(currentRowPrice >= previousRowPrice);
				previousRowPrice = currentRowPrice;
			}
		}
	}

	public void testSortByRating() throws Exception {
		final AtomicReference<Float> rating = new AtomicReference<Float>();
		initiateSearchHelper();
		SortFilter.clickHotelSortFilterButton();
		SortFilter.clickToSortHotelByRating();
		int totalHotels = EspressoUtils.getListCount(Results.hotelList());
		if (totalHotels > 1) {
			DataInteraction prevResultRow = Results.hotelAtIndex(1);
			prevResultRow.onChildView(withId(R.id.user_rating_bar)).perform(getRating(rating));
			float previousRowRating = rating.get();
			for (int i = 1; i < totalHotels - 1; i++) {
				DataInteraction currentHotelRowView = Results.hotelAtIndex(i);
				currentHotelRowView.onChildView(withId(R.id.user_rating_bar)).perform(getRating(rating));
				float currentRowRating = rating.get();
				Common.enterLog(TAG, "RATING " + previousRowRating + " >= " + currentRowRating);
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
