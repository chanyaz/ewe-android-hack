package com.expedia.bookings.test.phone.pagemodels.hotels;


import static android.support.test.espresso.action.ViewActions.click;

import com.expedia.bookings.R;

import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class HotelReceiptModel {

	public static ViewInteraction nightsTextView() {
		return onView(withId(R.id.nights_text));
	}

	public static ViewInteraction grandTotalTextView() {
		return onView(withId(R.id.grand_total_text));
	}

	public static void clickGrandTotalTextView() {
		grandTotalTextView().perform(click());
	}
}
