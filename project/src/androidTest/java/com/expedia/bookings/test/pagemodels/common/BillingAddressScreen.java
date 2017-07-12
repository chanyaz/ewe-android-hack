package com.expedia.bookings.test.pagemodels.common;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

public class BillingAddressScreen {
	private static final int sAddressLine1EditTextID = R.id.edit_address_line_one;
	private static final int sAddressLine2EditTextID = R.id.edit_address_line_two;
	private static final int sAddressCityEditTextID = R.id.edit_address_city;
	private static final int sAddressStateEditTextID = R.id.edit_address_state;
	private static final int sAddressPostalCodeEditTextID = R.id.edit_address_postal_code;

	//Object access

	public static ViewInteraction addressLineOneEditText(int parentId) {
		return onView(allOf(withId(sAddressLine1EditTextID), isDescendantOfA(withId(parentId))));
	}

	public static ViewInteraction addressLineTwoEditText(int parentId) {
		return onView(allOf(withId(sAddressLine2EditTextID), isDescendantOfA(withId(parentId))));
	}

	public static ViewInteraction cityEditText(int parentId) {
		return onView(allOf(withId(sAddressCityEditTextID), isDescendantOfA(withId(parentId))));
	}

	public static ViewInteraction stateEditText(int parentId) {
		return onView(allOf(withId(sAddressStateEditTextID), isDescendantOfA(withId(parentId))));
	}

	public static ViewInteraction postalCodeEditText(int parentId) {
		return onView(allOf(withId(sAddressPostalCodeEditTextID), isDescendantOfA(withId(parentId))));
	}

	//Object interaction

	public static void typeTextAddressLineOne(String text, int parentId) {
		addressLineOneEditText(parentId).perform(typeText(text), closeSoftKeyboard());
	}

	public static void typeTextCity(String text, int parentId) {
		cityEditText(parentId).perform(typeText(text), closeSoftKeyboard());
	}

	public static void typeTextState(String text, int parentId) {
		stateEditText(parentId).perform(typeText(text), closeSoftKeyboard());
	}

	public static void typeTextPostalCode(String text, int parentId) {
		postalCodeEditText(parentId).perform(scrollTo(), typeText(text), closeSoftKeyboard());
	}
}
