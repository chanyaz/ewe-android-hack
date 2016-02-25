package com.expedia.bookings.test.phone.pagemodels.hotels;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
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

	public static ViewInteraction paymentOptionsButton() {
		return onView(withId(R.id.card_info_container));
	}

	public static ViewInteraction paymentInfoIcon() {
		return onView(withId(R.id.card_info_icon));
	}

	public static ViewInteraction paymentInfoText() {
		return onView(withId(R.id.card_info_name));
	}

	public static ViewInteraction paymentInfoStatusIcon() {
		return onView(withId(R.id.card_info_status_icon));
	}

	public static ViewInteraction purchaseTotalText() {
		return onView(withId(R.id.purchase_total_text_view));
	}

	public static ViewInteraction rewardsToEarnText() {
		return onView(withId(R.id.account_rewards_textview));
	}

	public static void openPaymentOptions() {
		paymentOptionsButton().perform(click());
	}


}
