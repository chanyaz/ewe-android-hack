package com.expedia.bookings.test.tablet.pagemodels;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasSibling;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by dmadan on 8/5/14.
 */
public class HotelDetails {

	public static ViewInteraction hotelName() {
		return onView(withId(R.id.hotel_header_hotel_name));
	}

	public static ViewInteraction hotelPrice() {
		return onView(allOf(withId(R.id.text_price_per_night), isDisplayed()));
	}

	public static ViewInteraction hotelRating() {
		return onView(allOf(withId(R.id.user_rating_bar), hasSibling(withId(R.id.user_rating_text))));
	}

	public static ViewInteraction addHotel() {
		return onView(
			allOf(
				withId(R.id.room_rate_button_add),
				isDescendantOfA(allOf(withId(R.id.room_rate_add_select_container), hasSibling(allOf(withId(R.id.room_rate_detail_container),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))))
			)
		);
	}

	//add hotel button in Hotel reviews
	public static ViewInteraction reviewsAddHotel() {
		return onView(allOf(withId(R.id.room_rate_button_add), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static void clickAddHotel() {
		addHotel().perform(scrollTo(), click());
	}

	public static void clickReviews() {
		onView(withId(R.id.user_rating_text)).perform(click());
	}

	public static void clickCriticalTab() {
		onView(withId(R.id.user_review_button_critical)).perform(click());
	}

	public static void clickFavorableTab() {
		onView(withId(R.id.user_review_button_favorable)).perform(click());
	}

	public static void clickRecentTab() {
		onView(withId(R.id.user_review_button_recent)).perform(click());
	}
}
