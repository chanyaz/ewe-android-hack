package com.expedia.bookings.test.ui.tablet.tests.hotels;

import java.util.concurrent.atomic.AtomicReference;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.SortFilter;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

import android.support.test.espresso.DataInteraction;

import static com.expedia.bookings.test.espresso.ViewActions.getRating;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 6/5/14.
 */
public class HotelSearchSortTests extends TabletTestCase {

	private static final String TAG = HotelSearchSortTests.class.getSimpleName();

	private float getCleanFloatFromString(String str) {
		str = str.replaceAll("[^\\d.]", "");
		return Float.parseFloat(str);
	}

	private void initiateSearchHelper() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Results.swipeUpHotelList();
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
}
