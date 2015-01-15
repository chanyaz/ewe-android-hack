package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.DataInteraction;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

/**
 * Created by dmadan on 4/10/14.
 */
public class HotelsRoomsRatesScreen extends ScreenActions {
	private static final int HOTEL_NAME_TEXT_VIEW_ID = R.id.name_text_view;
	private static final int HOTEL_RATING_BAR_ID = R.id.hotel_rating_bar_stars;
	private static final int LIST_VIEW_ID = android.R.id.list;
	private static final int BACK_BUTTON_ID = android.R.id.home;
	private static final int RENOVATION_INFO_IMAGE_ID = R.id.construction_chevron;
	private static final int ADDITIONAL_FEES_INFO_IMAGE_ID = R.id.resort_fees_chevron;
	private static final int TOTAL_PRICE_TEXT_VIEW_ID = R.id.total_price_text_view;

	private static final int NUM_HEADERS_IN_LIST_VIEW = 1;

	// Object access

	public static ViewInteraction hotelNameTextView() {
		return onView(withId(HOTEL_NAME_TEXT_VIEW_ID));
	}

	public static ViewInteraction hotelRatingBar() {
		return onView(withId(HOTEL_RATING_BAR_ID));
	}

	public static ViewInteraction roomList() {
		return onView(withId(LIST_VIEW_ID));
	}

	public static ViewInteraction backButton() {
		return onView(withId(BACK_BUTTON_ID));
	}

	public static ViewInteraction renovationInfoButton() {
		return onView(withId(RENOVATION_INFO_IMAGE_ID));
	}

	public static ViewInteraction additionalFeesInfoButton() {
		return onView(withId(ADDITIONAL_FEES_INFO_IMAGE_ID));
	}

	public static ViewInteraction totalPriceTextView() {
		return onView(withId(TOTAL_PRICE_TEXT_VIEW_ID));
	}

	public static DataInteraction listItem() {
		return onData(anything()).inAdapterView(withId(android.R.id.list));
	}

	// Object interaction

	public static void clickBackButton() {
		(backButton()).perform(click());
	}

	public static void clickRenovationInfoButton() {
		(renovationInfoButton()).perform(click());
	}

	public static void clickAdditionalFeesInfoButton() {
		(additionalFeesInfoButton()).perform(click());
	}

	public static void selectRoomItem(int index) {
		onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(NUM_HEADERS_IN_LIST_VIEW + index).perform(click());

		//handle price change popup after selecting a room
		try {
			onView(withText(R.string.ok)).perform(click());
		}
		catch (Exception e) {
			//
		}
	}
}
