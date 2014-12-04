package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.closeSoftKeyboard;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;

/**
 * Created by dmadan on 4/10/14.
 */
public class HotelsFilterMenu extends ScreenActions {
	private static final int FILTER_EDIT_TEXT_ID = R.id.filter_hotel_name_edit_text;

	private static final int SMALL_RADIUS_BUTTON_ID = R.id.radius_small_button;
	private static final int MEDIUM_RADIUS_BUTTON_ID = R.id.radius_medium_button;
	private static final int LARGE_RADIUS_BUTTON_ID = R.id.radius_large_button;
	private static final int ALL_RADIUS_BUTTON_ID = R.id.radius_all_button;

	private static final int LOW_RATING_BUTTON_ID = R.id.rating_low_button;
	private static final int MEDIUM_RATING_BUTTON_ID = R.id.rating_medium_button;
	private static final int HIGH_RATING_BUTTON_ID = R.id.rating_high_button;
	private static final int ALL_RATING_BUTTON_ID = R.id.rating_all_button;

	private static final int LOW_PRICE_BUTTON_ID = R.id.price_cheap_button;
	private static final int MODERATE_PRICE_BUTTON_ID = R.id.price_moderate_button;
	private static final int EXPENSIVE_PRICE_BUTTON_ID = R.id.price_expensive_button;
	private static final int ALL_PRICE_BUTTON_ID = R.id.price_all_button;

	private static final int VIP_ACCESS_BUTTON_ID = R.id.filter_vip_access;

	// Object access

	public static ViewInteraction filterEditText() {
		return onView(withId(FILTER_EDIT_TEXT_ID));
	}

	public static ViewInteraction smallRadiusFilterButton() {
		return onView(withId(SMALL_RADIUS_BUTTON_ID));
	}

	public static ViewInteraction mediumRadiusFilterButton() {
		return onView(withId(MEDIUM_RADIUS_BUTTON_ID));
	}

	public static ViewInteraction largeRadiusFilterButton() {
		return onView(withId(LARGE_RADIUS_BUTTON_ID));
	}

	public static ViewInteraction lowRatingFilterButton() {
		return onView(withId(LOW_RATING_BUTTON_ID));
	}

	public static ViewInteraction mediumRatingFilterButton() {
		return onView(withId(MEDIUM_RATING_BUTTON_ID));
	}

	public static ViewInteraction highRatingFilterButton() {
		return onView(withId(HIGH_RATING_BUTTON_ID));
	}

	public static ViewInteraction allRatingFilterButton() {
		return onView(withId(ALL_RATING_BUTTON_ID));
	}

	public static ViewInteraction lowPriceFilterButton() {
		return onView(withId(LOW_PRICE_BUTTON_ID));
	}

	public static ViewInteraction moderatePriceFilterButton() {
		return onView(withId(MODERATE_PRICE_BUTTON_ID));
	}

	public static ViewInteraction expensivePriceFilterButton() {
		return onView(withId(EXPENSIVE_PRICE_BUTTON_ID));
	}

	public static ViewInteraction allPriceFilterButton() {
		return onView(withId(ALL_PRICE_BUTTON_ID));
	}

	public static ViewInteraction filterVIPAccessButton() {
		return onView(withId(VIP_ACCESS_BUTTON_ID));
	}

	// Object interactions

	public static void enterFilterText(String text) {
		filterEditText().perform(typeText(text), closeSoftKeyboard());
	}

	public static void clickSmallRadiusFilterButton() {
		(smallRadiusFilterButton()).perform(click());
	}

	public static void clickMediumRadiusFilterButton() {
		(mediumRadiusFilterButton()).perform(click());
	}

	public static void clickLargeRadiusFilterButton() {
		(largeRadiusFilterButton()).perform(click());
	}

	public static void clickLowRatingFilterButton() {
		(lowRatingFilterButton()).perform(click());
	}

	public static void clickMediumRatingFilterButton() {
		(mediumRatingFilterButton()).perform(click());
	}

	public static void clickHighRatingFilterButton() {
		(highRatingFilterButton()).perform(click());
	}

	public static void clickAllRatingFilterButton() {
		(allRatingFilterButton()).perform(click());
	}

	public static void clickVIPAccessFilterButton() {
		(filterVIPAccessButton()).perform(click());
	}
}
