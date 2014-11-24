package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/10/14.
 */
public class HotelsReviewsScreen extends ScreenActions {
	private static final int sSelectButtonID = R.id.menu_select_hotel;
	private static final int sLoadingReviewsStringID = R.string.user_review_loading_text;
	private static final int sTitleViewID = R.id.title;
	private static final int sRatingViewID = R.id.rating;
	private static final int sFavorableStringID = R.string.user_review_sort_button_favorable;
	private static final int sRecentStringID = R.string.user_review_sort_button_recent;
	private static final int sCriticalStringID = R.string.user_review_sort_button_critical;
	private static final int sBackButtonID = android.R.id.home;

	// Object access

	public static ViewInteraction titleView() {
		return onView(withId(sTitleViewID));
	}

	public static ViewInteraction ratingBar() {
		return onView(withId(sRatingViewID));
	}

	public static ViewInteraction selectButton() {
		return onView(withId(sSelectButtonID));
	}

	public static ViewInteraction favorableString() {
		return onView(withText(sFavorableStringID));
	}

	public static ViewInteraction recentString() {
		return onView(withText(sRecentStringID));
	}

	public static ViewInteraction criticalString() {
		return onView(withText(sCriticalStringID));
	}

	public static ViewInteraction backButton() {
		return onView(withId(sBackButtonID));
	}

	public static ViewInteraction loadingUserReviews() {
		return onView(withText(sLoadingReviewsStringID));
	}

	// Object interaction

	public static void clickSelectButton() {
		(selectButton()).perform(click());
	}

	public static void clickFavorableTab() {
		(favorableString()).perform(click());
	}

	public static void clickRecentTab() {
		(recentString()).perform(click());
	}

	public static void clickCriticalTab() {
		(criticalString()).perform(click());
	}

	public static void clickBackButton() {
		(backButton()).perform(click());
	}

}
