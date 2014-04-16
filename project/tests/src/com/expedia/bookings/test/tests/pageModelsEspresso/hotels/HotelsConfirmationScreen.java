package com.expedia.bookings.test.tests.pageModelsEspresso.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ConfirmationScreen;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/10/14.
 */
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

}
