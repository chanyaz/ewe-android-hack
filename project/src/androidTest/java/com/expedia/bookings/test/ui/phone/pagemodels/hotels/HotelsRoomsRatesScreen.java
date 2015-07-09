package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.ViewActions;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.DataInteraction;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.core.AllOf.allOf;

public class HotelsRoomsRatesScreen {
	private static final int NUM_HEADERS_IN_LIST_VIEW = 1;

	// Object access

	public static ViewInteraction hotelNameTextView() {
		return onView(withId(R.id.name_text_view));
	}

	public static ViewInteraction hotelRatingBar() {
		return onView(withId(R.id.hotel_rating_bar_stars));
	}

	public static ViewInteraction roomList() {
		return onView(withId(android.R.id.list));
	}

	public static DataInteraction listItem() {
		return onData(anything()).inAdapterView(withId(android.R.id.list));
	}

	// Object interaction

	public static void selectRoomItem(int index) {
		onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(NUM_HEADERS_IN_LIST_VIEW + index)
			.perform(click());

		//handle price change popup after selecting a room
		dismissPriceChange();
	}

	public static void selectRoomItemWithPriceChange(int index) {
		getRoomRow(index).perform(click());
	}

	public static DataInteraction getRoomRow(int index) {
		return onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(NUM_HEADERS_IN_LIST_VIEW + index);
	}

	public static void dismissPriceChange() {
		try {
			onView(withText(R.string.ok)).perform(click());
		}
		catch (Exception e) {
			//
		}
	}

	public static void selectETPRoomItem(int index) {
		onView(allOf(withId(android.R.id.list))).perform(ViewActions.clickETPRoomItem(index));
	}

	public static void clickPayLaterButton() {
		onView(withId(R.id.radius_pay_later)).perform(click());
	}

	public static void clickSelectRoomButton() {
		onView(withId(R.id.select_room_button)).perform(click());
	}

}
