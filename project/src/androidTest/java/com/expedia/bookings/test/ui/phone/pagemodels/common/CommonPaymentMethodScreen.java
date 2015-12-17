package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.expedia.bookings.R;
import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.action.ViewActions.click;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class CommonPaymentMethodScreen {
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
