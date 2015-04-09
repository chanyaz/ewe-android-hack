package com.expedia.bookings.test.ui.phone.pagemodels.flights;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/7/14.
 */
public class FlightsCheckoutScreen extends CommonCheckoutScreen {
	private static final int LOG_OUT_STRING_ID = R.string.sign_out;

	public static ViewInteraction logOut() {
		return onView(withText(LOG_OUT_STRING_ID));
	}

	public static void clickLogOutString() {
		logOut().perform();
	}
}
