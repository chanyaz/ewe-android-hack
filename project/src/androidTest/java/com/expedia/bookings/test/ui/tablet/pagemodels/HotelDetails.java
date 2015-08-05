package com.expedia.bookings.test.ui.tablet.pagemodels;

import com.expedia.bookings.R;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;

import static com.expedia.bookings.test.espresso.ViewActions.swipeUp;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
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

	public static void clickSelectHotelWithRoomDescription(String roomDescription) {
		onView(withId(R.id.scrolling_content)).perform(swipeUp());
		onView(withText(roomDescription)).perform(click());
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
