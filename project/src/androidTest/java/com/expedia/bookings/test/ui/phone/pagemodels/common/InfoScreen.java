package com.expedia.bookings.test.ui.phone.pagemodels.common;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class InfoScreen extends ScreenActions {
	private static final int OK_STRING_ID = R.string.ok;
	private static final int CANCEL_STRING_ID = R.string.cancel;
	private static final int BOOKING_SUPPORT_STRING_ID = R.string.booking_support;
	private static final int CONTACT_PHONE_STRING_ID = R.string.contact_expedia_phone;


	// Object access
	public static ViewInteraction okString() {
		return onView(withText(OK_STRING_ID));
	}

	public static ViewInteraction cancelString() {
		return onView(withText(CANCEL_STRING_ID));
	}

	public static ViewInteraction contactPhoneString() {
		return onView(withText(CONTACT_PHONE_STRING_ID));
	}

	public static ViewInteraction bookingSupport() {
		return onView(withText(BOOKING_SUPPORT_STRING_ID));
	}

	// Object interaction

	public static void clickBookingSupport() {
		bookingSupport().perform(click());
	}

	public static void clickOkString() {
		okString().perform(click());
	}

	public static void clickCancelString() {
		cancelString().perform(click());
	}

	public static void clickContactPhone() {
		contactPhoneString().perform(click());
	}
}
