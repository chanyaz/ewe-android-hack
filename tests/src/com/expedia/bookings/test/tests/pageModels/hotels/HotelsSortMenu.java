package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

public class HotelsSortMenu extends ScreenActions {

	private static int sSortPopularityStringID = R.string.sort_description_popular;
	private static int sSortDealsStringID = R.string.sort_description_deals;
	private static int sSortPriceStringID = R.string.sort_description_price;
	private static int sSortUserRatingStringID = R.string.sort_description_rating;
	private static int sSortDistanceStringID = R.string.sort_description_distance;

	public HotelsSortMenu(Instrumentation instrumentation, Activity activity, Resources res, TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public String getSortByPopularityButton() {
		return getString(sSortPopularityStringID);
	}

	public String getSortByDealsButton() {
		return getString(sSortDealsStringID);
	}

	public String getSortByPriceButton() {
		return getString(sSortPriceStringID);
	}

	public String getSortByUserRatingButton() {
		return getString(sSortUserRatingStringID);
	}

	public String getSortByDistanceButton() {
		return getString(sSortDistanceStringID);
	}

	// Object interaction

	public void clickSortByPopularityButton() {
		clickOnText(getSortByPopularityButton());
	}

	public void clickSortByDealsButton() {
		clickOnText(getSortByDealsButton());
	}

	public void clickSortByPriceButton() {
		clickOnText(getSortByPriceButton());
	}

	public void clickSortByUserRatingButton() {
		clickOnText(getSortByUserRatingButton());
	}

	public void clickSortByDistanceButton() {
		clickOnText(getSortByDistanceButton());
	}

}
