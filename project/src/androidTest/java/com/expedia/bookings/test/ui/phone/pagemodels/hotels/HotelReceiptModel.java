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
public class HotelReceiptModel extends ScreenActions {

	private static final int NIGHTS_TEXT_VIEW_ID = R.id.nights_text;
	private static final int GRAND_TOTAL_TEXT_VIEW_ID = R.id.grand_total_text;
	private static final int COST_SUMMARY_STRING_ID = R.string.cost_summary;


	public static ViewInteraction nightsTextView() {
		return onView(withId(NIGHTS_TEXT_VIEW_ID));
	}

	public static ViewInteraction grandTotalTextView() {
		return onView(withId(GRAND_TOTAL_TEXT_VIEW_ID));
	}

	public static ViewInteraction costSummaryString() {
		return onView(withText(COST_SUMMARY_STRING_ID));
	}

	// Object interaction

	public static void clickGrandTotalTextView() {
		grandTotalTextView().perform(click());
	}
}
