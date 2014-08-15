package com.expedia.bookings.test.phone.pagemodels.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.ScreenActions;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

/**
 * Created by dmadan on 4/10/14.
 */
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

	// Object access

	public static ViewInteraction getSortByPopularityString() {
		return onView(withText(SORT_POPULARITY_STRING_ID));
	}

	public static ViewInteraction getSortByDealsString() {
		return onView(withText(SORT_DEALS_STRING_ID));
	}

	public static ViewInteraction getSortByPriceString() {
		return onView(withText(SORT_PRICE_STRING_ID));
	}

	public static ViewInteraction getSortByUserRatingString() {
		return onView(withText(SORT_USER_RATING_STRING_ID));
	}

	public static ViewInteraction getSortByDistanceString() {
		return onView(withText(SORT_DISTANCE_STRING_ID));
	}

	// Object interaction

	public void clickSortByPopularityString() {
		getSortByPopularityString().perform(click());
	}

	public static void clickSortByDealsString() {
		getSortByDealsString().perform(click());
	}

	public static void clickSortByPriceString() {
		getSortByPriceString().perform(click());
	}

	public static void clickSortByUserRatingString() {
		getSortByUserRatingString().perform(click());
	}

	public static void clickSortByDistanceString() {
		getSortByDistanceString().perform(click());
	}
}

