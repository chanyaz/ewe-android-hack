package com.expedia.bookings.test.tests.pageModelsEspresso.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.closeSoftKeyboard;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;


/**
 * Created by dmadan on 4/7/14.
 */
public class BillingAddressScreen {
	private static final int sAddressLine1EditTextID = R.id.edit_address_line_one;
	private static final int sAddressLine2EditTextID = R.id.edit_address_line_two;
	private static final int sAddressCityEditTextID = R.id.edit_address_city;
	private static final int sAddressStateEditTextID = R.id.edit_address_state;
	private static final int sAddressPostalCodeEditTextID = R.id.edit_address_postal_code;
	private static final int sAddressCountrySpinnerID = R.id.edit_country_spinner;
	private static final int sNextButtonID = R.id.menu_next;

	//Object access

	public static ViewInteraction addressLineOneEditText() {
		return onView(withId(sAddressLine1EditTextID));
	}

	public static ViewInteraction addressLineTwoEditText() {
		return onView(withId(sAddressLine2EditTextID));
	}

	public static ViewInteraction cityEditText() {
		return onView(withId(sAddressCityEditTextID));
	}

	public static ViewInteraction stateEditText() {
		return onView(withId(sAddressStateEditTextID));
	}

	public static ViewInteraction postalCodeEditText() {
		return onView(withId(sAddressPostalCodeEditTextID));
	}

	public static ViewInteraction countrySpinner() {
		return onView(withId(sAddressCountrySpinnerID));
	}

	public static ViewInteraction nextButton() {
		return onView(withId(sNextButtonID));
	}

	//Object interaction

	public static void typeTextAddressLineOne(String text) {
		addressLineOneEditText().perform(typeText(text), closeSoftKeyboard());
	}

	public static void typeTextAddressLineTwo(String text) {
		addressLineTwoEditText().perform(typeText(text), closeSoftKeyboard());
	}

	public static void typeTextCity(String text) {
		cityEditText().perform(typeText(text), closeSoftKeyboard());
	}

	public static void typeTextState(String text) {
		stateEditText().perform(typeText(text), closeSoftKeyboard());
	}

	public static void typeTextPostalCode(String text) {
		postalCodeEditText().perform(typeText(text), closeSoftKeyboard());
	}

	public static void clickCountrySpinner() {
		countrySpinner().perform(click());
	}

	public static void clickNextButton() {
		nextButton().perform(click());
	}
}
