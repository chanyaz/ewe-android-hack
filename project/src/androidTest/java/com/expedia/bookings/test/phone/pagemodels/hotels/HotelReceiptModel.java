package com.expedia.bookings.test.phone.pagemodels.hotels;


import static android.support.test.espresso.action.ViewActions.click;

import com.expedia.bookings.R;

import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class HotelReceiptModel {

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
