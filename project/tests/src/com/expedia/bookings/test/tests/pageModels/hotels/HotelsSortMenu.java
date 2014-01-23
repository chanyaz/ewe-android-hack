package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

public class HotelsSortMenu extends ScreenActions {

	private static final int SORT_POPULARITY_STRING_ID = R.string.sort_description_popular;
	private static final int SORT_DEALS_STRING_ID = R.string.sort_description_deals;
	private static final int SORT_PRICE_STRING_ID = R.string.sort_description_price;
	private static final int SORT_USER_RATING_STRING_ID = R.string.sort_description_rating;
	private static final int SORT_DISTANCE_STRING_ID = R.string.sort_description_distance;

	public static final int SORT_POPULARITY_MENU_ID = R.id.menu_select_sort_popularity;
	public static final int SORT_DEALS_MENU_ID = R.id.menu_select_sort_deals;
	public static final int SORT_PRICE_MENU_ID = R.id.menu_select_sort_price;
	public static final int SORT_USER_RATING_MENU_ID = R.id.menu_select_sort_user_rating;
	public static final int SORT_DISTANCE_MENU_ID = R.id.menu_select_sort_distance;

	public HotelsSortMenu(Instrumentation instrumentation, Activity activity, Resources res, TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public String getSortByPopularityString() {
		return getString(SORT_POPULARITY_STRING_ID);
	}

	public String getSortByDealsString() {
		return getString(SORT_DEALS_STRING_ID);
	}

	public String getSortByPriceString() {
		return getString(SORT_PRICE_STRING_ID);
	}

	public String getSortByUserRatingString() {
		return getString(SORT_USER_RATING_STRING_ID);
	}

	public String getSortByDistanceString() {
		return getString(SORT_DISTANCE_STRING_ID);
	}

	// Object interaction

	public void clickSortByPopularityString() {
		clickOnText(getSortByPopularityString());
	}

	public void clickSortByDealsString() {
		clickOnText(getSortByDealsString());
	}

	public void clickSortByPriceString() {
		clickOnText(getSortByPriceString());
	}

	public void clickSortByUserRatingString() {
		clickOnText(getSortByUserRatingString());
	}

	public void clickSortByDistanceString() {
		clickOnText(getSortByDistanceString());
	}

}
