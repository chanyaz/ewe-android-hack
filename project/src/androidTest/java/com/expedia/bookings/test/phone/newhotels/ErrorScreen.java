package com.expedia.bookings.test.phone.newhotels;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class ErrorScreen {

	public static void clickOnEditPayment() {
		onView(withText(R.string.edit_payment)).perform(click());
	}

	public static void clickOnEditTravellerInfo() {
		onView(withText(R.string.edit_current_traveler)).perform(click());
	}

	public static void clickOnRetry() {
		onView(withText(R.string.retry)).perform(click());
	}

	public static void clickOnPickANewHotel() {
		onView(withText(R.string.pick_new_hotel)).perform(click());
	}
}
