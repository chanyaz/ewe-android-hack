package com.expedia.bookings.test.tests.pageModelsEspresso.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.closeSoftKeyboard;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/7/14.
 */
public class CommonTravelerInformationScreen extends ScreenActions {

	private static final int FIRST_NAME_EDIT_TEXT_ID = R.id.edit_first_name;
	private static final int MIDDLE_NAME_EDIT_TEXT_ID = R.id.edit_middle_name;
	private static final int LAST_NAME_EDIT_TEXT_ID = R.id.edit_last_name;
	private static final int PHONE_NUMBER_EDIT_TEXT_ID = R.id.edit_phone_number;
	private static final int EMAIL_EDIT_TEXT_ID = R.id.edit_email_address;
	private static final int BIRTHDATE_SPINNER_BUTTON_ID = R.id.edit_birth_date_text_btn;
	private static final int DONE_STRING_ID = R.string.done;
	private static final int NEXT_BUTTON_ID = R.id.menu_next;
	private static final int DONE_BUTTON_ID = R.id.menu_done;
	private static final int ENTER_A_NEW_TRAVELER_STRING_ID = R.string.enter_new_traveler;
	private static final int SET_STRING_ID = R.string.btn_set;

	// Object access

	public static ViewInteraction firstNameEditText() {
		return onView(withId(FIRST_NAME_EDIT_TEXT_ID));
	}

	public static ViewInteraction middleNameEditText() {
		return onView(withId(MIDDLE_NAME_EDIT_TEXT_ID));
	}

	public static ViewInteraction lastNameEditText() {
		return onView(withId(LAST_NAME_EDIT_TEXT_ID));
	}

	public static ViewInteraction phoneNumberEditText() {
		return onView(withId(PHONE_NUMBER_EDIT_TEXT_ID));
	}

	public static ViewInteraction birthDateSpinnerButton() {
		return onView(withId(BIRTHDATE_SPINNER_BUTTON_ID));
	}

	public static ViewInteraction done() {
		return onView(withText(DONE_STRING_ID));
	}

	public static ViewInteraction doneButton() {
		return onView(withId(DONE_BUTTON_ID));
	}

	public static ViewInteraction emailEditText() {
		return onView(withId(EMAIL_EDIT_TEXT_ID));
	}

	public static ViewInteraction nextButton() {
		return onView(withId(NEXT_BUTTON_ID));
	}

	public static ViewInteraction enterANewTraveler() {
		return onView(withText(ENTER_A_NEW_TRAVELER_STRING_ID));
	}

	public static ViewInteraction set() {
		return onView(withText(SET_STRING_ID));
	}
	// Object interaction

	public static void enterFirstName(String firstName) {
		firstNameEditText().perform(typeText(firstName), closeSoftKeyboard());
	}

	public static void enterMiddleName(String middleName) {
		firstNameEditText().perform(typeText(middleName), closeSoftKeyboard());
	}

	public static void enterLastName(String lastName) {
		lastNameEditText().perform(typeText(lastName), closeSoftKeyboard());
	}

	public static void enterPhoneNumber(String phoneNumber) {
		phoneNumberEditText().perform(typeText(phoneNumber), closeSoftKeyboard());
	}

	public static void clickBirthDateButton() {
		birthDateSpinnerButton().perform(click());
	}

	public static void clickDoneString() {
		done().perform(click());
	}

	public static void clickDoneButton() {
		doneButton().perform(click());
	}

	public static void clickEnterANewTraveler() {
		enterANewTraveler().perform(click());
	}

	public static void enterEmailAddress(String emailAddress) {
		emailEditText().perform(typeText(emailAddress), closeSoftKeyboard());
	}

	public static void clickNextButton() {
		nextButton().perform(click());
	}

	public static void clickSetButton() {
		set().perform(click());
	}

}
