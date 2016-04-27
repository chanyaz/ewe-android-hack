package com.expedia.bookings.test.phone.pagemodels.common;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CommonCheckoutScreen {
	private static final int CHECKOUT_BUTTON_ID = R.id.menu_checkout;
	private static final int LOG_IN_BUTTON_ID = R.id.login_text_view;
	private static final int LOG_OUT_BUTTON_ID = R.id.account_logout_logout_button;
	private static final int SELECT_PAYMENT_BUTTON_ID = R.id.payment_info_btn;

	// Object access

	public static ViewInteraction checkoutButton() {
		return onView(withId(CHECKOUT_BUTTON_ID));
	}

	public static ViewInteraction logInButton() {
		return onView(withId(LOG_IN_BUTTON_ID));
	}

	public static ViewInteraction logOutButton() {
		return onView(withId(LOG_OUT_BUTTON_ID));
	}

	public static ViewInteraction selectPaymentButton() {
		return onView(withId(SELECT_PAYMENT_BUTTON_ID));
	}

	public static ViewInteraction slideToPurchaseStartView() {
		return onView(withId(R.id.slide_to_purchase_widget));
	}

	public static ViewInteraction flightsLegalTextView() {
		return onView(withId(R.id.legal_blurb));
	}

	// Object interaction

	public static void clickCheckoutButton() {
		Common.delay(1);
		checkoutButton().perform(ViewActions.waitForViewToDisplay(), click());
	}

	public static void clickLogOutButton() {
		logOutButton().perform(click());

		/* When the user logs out, there's a create trip call
		* Handle the price change popup
		*/
		try {
			onView(withText(R.string.ok)).perform(click());
		}
		catch (Exception e) {
			//
		}
	}

	public static void clickSelectPaymentButton() {
		selectPaymentButton().perform(scrollTo(), click());
	}

	public static void slideToCheckout() {
		slideToPurchaseStartView().perform(swipeRight());
	}

	public static void clickNewPaymentCard() {
		onView(withId(R.id.new_payment_new_card)).perform(click());
	}

	public static void clickIAcceptButton() {
		onView(withId(R.id.layout_i_accept)).perform(click());
	}

	public static void clickTravelerDetails() {
		onView(withId(R.id.traveler_empty_text_view)).perform(scrollTo());
		onView(withId(R.id.traveler_empty_text_view)).perform(click());
	}
}
