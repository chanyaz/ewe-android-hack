package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/7/14.
 */
public class CommonPaymentMethodScreen extends ScreenActions {
	private static final int sAddNewCardTextViewID = R.id.new_payment_new_card;

	//Object access
	public static ViewInteraction addNewCardTextView() {
		return onView(withId(sAddNewCardTextViewID));
	}

	//Object interaction
	public static void clickOnAddNewCardTextView() {
		addNewCardTextView().perform(scrollTo(), click());
	}
}
