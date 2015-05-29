package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import com.expedia.bookings.R;

import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.action.ViewActions.click;

/**
 * Created by dmadan on 4/10/14.
 */
public class HotelsSortMenu extends ScreenActions {
	private static final int SORT_POPULARITY_STRING_ID = R.string.sort_description_popular;
	private static final int SORT_PRICE_STRING_ID = R.string.sort_description_price;
	private static final int SORT_USER_RATING_STRING_ID = R.string.sort_description_rating;
	private static final int SORT_DISTANCE_STRING_ID = R.string.sort_description_distance;

	// Object access

	public static ViewInteraction getSortByPopularityString() {
		return onView(withText(SORT_POPULARITY_STRING_ID));
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

