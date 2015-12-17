package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import static android.support.test.espresso.action.ViewActions.click;

import com.expedia.bookings.R;

import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class HotelsReviewsScreen {
	public static ViewInteraction favorableString() {
		return onView(withText(R.string.user_review_sort_button_favorable));
	}

	public static ViewInteraction recentString() {
		return onView(withText(R.string.user_review_sort_button_recent));
	}

	public static ViewInteraction criticalString() {
		return onView(withText(R.string.user_review_sort_button_critical));
	}

	public static void clickFavorableTab() {
		favorableString().perform(click());
	}

	public static void clickRecentTab() {
		recentString().perform(click());
	}

	public static void clickCriticalTab() {
		criticalString().perform(click());
	}
}
