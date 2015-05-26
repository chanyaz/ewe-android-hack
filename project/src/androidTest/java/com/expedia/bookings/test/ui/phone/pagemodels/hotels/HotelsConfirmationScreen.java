package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ConfirmationScreen;
import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class HotelsConfirmationScreen extends ConfirmationScreen {
	private static final int SUMMARY_TEXT_VIEW_ID = R.id.stay_summary_text_view;
	private static final int HOTEL_NAME_TEXT_VIEW_ID = R.id.hotel_name_text_view;

	// Object access

	public static ViewInteraction summaryTextView() {
		return onView(withId(SUMMARY_TEXT_VIEW_ID));
	}

	public static ViewInteraction hotelNameTextView() {
		return onView(withId(HOTEL_NAME_TEXT_VIEW_ID));
	}

	public static ViewInteraction itineraryTextView() {
		return onView(withId(R.id.itinerary_text_view));
	}

	public static ViewInteraction emailTextView() {
		return onView(withId(R.id.email_text_view));
	}

	public static ViewInteraction doneButton() {
		return onView(withId(R.id.menu_done));
	}
}
