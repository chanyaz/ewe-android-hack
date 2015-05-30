package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/10/14.
 */
public class HotelsCheckoutScreen extends CommonCheckoutScreen {
	public static ViewInteraction guestCountView() {
		return onView(withId(R.id.guests_text));
	}

	public static ViewInteraction couponButton() {
		return onView(withId(R.id.coupon_button));
	}
}
