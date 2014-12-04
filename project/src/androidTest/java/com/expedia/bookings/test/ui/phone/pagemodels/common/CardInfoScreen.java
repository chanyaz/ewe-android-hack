package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.closeSoftKeyboard;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/7/14.
 */
public class CardInfoScreen extends ScreenActions {
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
	private static final int YEAR_DOWN_BUTTON_ID = R.id.year_down;

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

	public static ViewInteraction saveButtonString() {
		return onView(withText(R.string.save));
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

	public static ViewInteraction yearDownButton() {
		return onView(withId(YEAR_DOWN_BUTTON_ID));
	}

	public static ViewInteraction setButton() {
		return positiveButton();
	}

	public static ViewInteraction cancelButton() {
		return negativeButton();
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

	public static void clickYearDownButton() {
		yearDownButton().perform(click());
	}

	public static void clickSetButton() {
		setButton().perform(click());
	}

	public static void clickCancelButton() {
		cancelButton().perform(click());
	}

	public static void clickNoThanksButton() {
		noThanksButtonString().perform(click());
	}

	public static void clickSaveButton() {
		saveButtonString().perform(click());
	}

	public static void clickNextButton() {
		nextButton().perform(click());
	}
}
