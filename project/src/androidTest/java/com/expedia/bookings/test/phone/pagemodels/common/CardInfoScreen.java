package com.expedia.bookings.test.phone.pagemodels.common;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CardInfoScreen {
	private static final int CREDIT_CARD_NUMBER_EDIT_TEXT_ID = R.id.edit_creditcard_number;
	private static final int EXPIRATION_DATE_BUTTON_ID = R.id.edit_creditcard_exp_text_btn;
	private static final int NAME_ON_CARD_EDIT_TEXT_ID = R.id.edit_name_on_card;
	private static final int EMAIL_ADDRESS_EDIT_TEXT_ID = R.id.edit_email_address;
	private static final int POSTAL_CODE_EDIT_TEXT_ID = R.id.edit_address_postal_code;
	private static final int DONE_BUTTON_ID = R.id.menu_done;
	private static final int NEXT_BUTTON_ID = R.id.menu_next;
	private static final int MONTH_UP_BUTTON_ID = R.id.month_up;
	private static final int MONTH_DOWN_BUTTON_ID = R.id.month_down;
	private static final int YEAR_UP_BUTTON_ID = R.id.year_up;

	// Object access

	public static ViewInteraction creditCardNumberEditText() {
		return onView(withId(CREDIT_CARD_NUMBER_EDIT_TEXT_ID));
	}

	public static ViewInteraction expirationDateButton() {
		return onView(withId(EXPIRATION_DATE_BUTTON_ID));
	}

	public static ViewInteraction nameOnCardEditText() {
		return onView(withId(NAME_ON_CARD_EDIT_TEXT_ID));
	}

	public static ViewInteraction doneButton() {
		return onView(withId(DONE_BUTTON_ID));
	}

	public static ViewInteraction noThanksButtonString() {
		return onView(withText(R.string.no_thanks));
	}

	public static ViewInteraction emailEditText() {
		return onView(withId(EMAIL_ADDRESS_EDIT_TEXT_ID));
	}

	public static ViewInteraction postalCodeEditText() {
		return onView(withId(POSTAL_CODE_EDIT_TEXT_ID));
	}

	// Object access expiration date dialog
	public static ViewInteraction monthUpButton() {
		return onView(withId(MONTH_UP_BUTTON_ID));
	}

	public static ViewInteraction monthDownButton() {
		return onView(withId(MONTH_DOWN_BUTTON_ID));
	}

	public static ViewInteraction yearUpButton() {
		return onView(withId(YEAR_UP_BUTTON_ID));
	}

	public static ViewInteraction setButton() {
		return onView(withId(R.id.positive_button));
	}

	public static ViewInteraction nextButton() {
		return onView(withId(NEXT_BUTTON_ID));
	}

	// Object interaction

	public static void typeTextCreditCardEditText(String text) {
		creditCardNumberEditText().perform(typeText(text), closeSoftKeyboard());
	}

	public static void clickOnExpirationDateButton() {
		expirationDateButton().perform(click());
	}

	public static void typeTextNameOnCardEditText(String text) {
		nameOnCardEditText().perform(typeText(text), closeSoftKeyboard());
	}

	public static void typeTextEmailEditText(String emailAddress) {
		emailEditText().perform(typeText(emailAddress), closeSoftKeyboard());
	}

	public static void typeTextPostalCode(String postalCode) {
		postalCodeEditText().perform(typeText(postalCode), closeSoftKeyboard());

	}

	public static void clickOnDoneButton() {
		doneButton().perform(click());

		// Once returning to the checkout screen we want to let google wallet settle
		Common.delay(5);
	}

	// Object interaction expiration date dialog
	public static void clickMonthUpButton() {
		monthUpButton().perform(click());
	}

	public static void clickMonthDownButton() {
		monthDownButton().perform(click());
	}

	public static void clickYearUpButton() {
		yearUpButton().perform(click());
	}

	public static void clickSetButton() {
		setButton().perform(click());
	}

	public static void clickNoThanksButton() {
		noThanksButtonString().perform(click());
	}

	public static void clickNextButton() {
		Common.delay(2);
		nextButton().perform(click());
		Common.delay(3);
	}
}
