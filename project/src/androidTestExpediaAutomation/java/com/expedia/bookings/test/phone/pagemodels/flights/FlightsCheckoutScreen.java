package com.expedia.bookings.test.phone.pagemodels.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/7/14.
 */
public class FlightsCheckoutScreen extends CommonCheckoutScreen {
	private static final int LOG_OUT_STRING_ID = R.string.log_out;

	public static ViewInteraction logOut() {
		return onView(withText(LOG_OUT_STRING_ID));
	}

	public static void clickLogOutString() {
		logOut().perform();
	}
}
