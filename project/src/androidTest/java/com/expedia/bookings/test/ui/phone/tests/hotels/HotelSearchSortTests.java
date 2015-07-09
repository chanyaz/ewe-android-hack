package com.expedia.bookings.test.ui.phone.tests.hotels;

import java.util.concurrent.atomic.AtomicReference;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.Espresso;

import static com.expedia.bookings.test.espresso.ViewActions.getRating;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 5/20/14.
 */
public class HotelSearchSortTests extends PhoneTestCase {

	private static final String TAG = HotelSearchSortTests.class.getName();

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
		HotelsSearchScreen.clickSortByPrice();
		int totalHotels = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView());
		if (totalHotels > 1) {
			DataInteraction prevResultRow = HotelsSearchScreen.hotelListItem().atPosition(1);
			String previousRowPriceString = EspressoUtils.getListItemValues(prevResultRow, R.id.price_text_view);
			float previousRowPrice = getCleanFloatFromString(previousRowPriceString);
			for (int j = 1; j < totalHotels - 1; j++) {
				DataInteraction curentResultRow = HotelsSearchScreen.hotelListItem().atPosition(j);
				String currentRowPriceString = EspressoUtils.getListItemValues(curentResultRow, R.id.price_text_view);
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
		HotelsSearchScreen.clickSortByDistance();
		int totalHotels = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView());
		if (totalHotels > 1) {
			DataInteraction prevResultRow = HotelsSearchScreen.hotelListItem().atPosition(1);
			String previousRowDistanceString = EspressoUtils.getListItemValues(prevResultRow, R.id.proximity_text_view);
			float previousRowDistance = getCleanFloatFromString(previousRowDistanceString);
			for (int j = 1; j < totalHotels - 1; j++) {
				DataInteraction currentHotelRowView = HotelsSearchScreen.hotelListItem().atPosition(j);
				String currentRowDistanceString = EspressoUtils.getListItemValues(currentHotelRowView, R.id.proximity_text_view);
				float currentRowDistance = getCleanFloatFromString(currentRowDistanceString);
				ScreenActions.enterLog(TAG, "DISTANCE " + currentRowDistance + " >= " + previousRowDistance);
				assertTrue(currentRowDistance >= previousRowDistance);
				previousRowDistance = currentRowDistance;
			}
		}
		Espresso.pressBack();
	}

	public void testSortByRating() throws Exception {
		final AtomicReference<Float> rating = new AtomicReference<Float>();
		initiateSearchHelper("Boston, MA");
		HotelsSearchScreen.clickOnSortButton();
		HotelsSearchScreen.clickSortByUserRating();
		int totalHotels = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView());
		if (totalHotels > 1) {
			DataInteraction prevResultRow = HotelsSearchScreen.hotelListItem().atPosition(1);
			prevResultRow.onChildView(withId(R.id.user_rating_bar)).perform(getRating(rating));
			float previousRowRating = rating.get();
			for (int i = 1; i < totalHotels - 1; i++) {
				DataInteraction currentHotelRowView = HotelsSearchScreen.hotelListItem().atPosition(i);
				currentHotelRowView.onChildView(withId(R.id.user_rating_bar)).perform(getRating(rating));
				float currentRowRating = rating.get();
				ScreenActions.enterLog(TAG, "RATING " + previousRowRating + " >= " + currentRowRating);
				assertTrue(previousRowRating >= currentRowRating);
				previousRowRating = currentRowRating;
			}
		}
		Espresso.pressBack();
	}
}
