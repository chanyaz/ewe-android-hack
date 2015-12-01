package com.expedia.bookings.test.phone.pagemodels.common;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class InfoScreen {
	public static void clickBookingSupport() {
		onView(withText(R.string.booking_support)).perform(click());
	}

	public static void clickContactPhone() {
		onView(withText(R.string.contact_expedia_phone)).perform(click());
	}
}
