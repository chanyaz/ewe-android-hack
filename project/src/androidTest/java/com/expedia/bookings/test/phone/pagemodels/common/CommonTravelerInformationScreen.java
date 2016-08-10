package com.expedia.bookings.test.phone.pagemodels.common;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.Is.is;

public class CommonTravelerInformationScreen {

	private static final int FIRST_NAME_EDIT_TEXT_ID = R.id.edit_first_name;
	private static final int MIDDLE_NAME_EDIT_TEXT_ID = R.id.edit_middle_name;
	private static final int LAST_NAME_EDIT_TEXT_ID = R.id.edit_last_name;
	private static final int PHONE_NUMBER_EDIT_TEXT_ID = R.id.edit_phone_number;
	private static final int EMAIL_EDIT_TEXT_ID = R.id.edit_email_address;
	private static final int BIRTHDATE_SPINNER_BUTTON_ID = R.id.edit_birth_date_text_btn;
	private static final int GENDER_SPINNER_BUTTON_ID = R.id.edit_gender_spinner;
	private static final int DONE_STRING_ID = R.string.done;
	private static final int NEXT_BUTTON_ID = R.id.menu_next;
	private static final int DONE_BUTTON_ID = R.id.menu_done;

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

	// Object interaction

	public static void enterFirstName(String firstName) {
		firstNameEditText().perform(ViewActions.waitForViewToDisplay(), typeText(firstName));
	}

	public static void enterLastName(String lastName) {
		lastNameEditText().perform(typeText(lastName));
	}

	public static void enterPhoneNumber(String phoneNumber) {
		phoneNumberEditText().perform(typeText(phoneNumber));
	}

	public static void clickBirthDateButton() {
		birthDateSpinnerButton().perform(click());
	}

	public static void selectGender(String genderType) {
		onView(withId(R.id.edit_gender_spinner)).perform(click());
		onData(allOf(is(instanceOf(String.class)),is(genderType))).perform(click());
	}

	public static void clickDoneString() {
		done().perform(click());
	}

	public static void clickDoneButton() {
		doneButton().perform(click());
	}

	public static void enterEmailAddress(String emailAddress) {
		emailEditText().perform(typeText(emailAddress));
	}

	public static void clickNextButton() {
		nextButton().perform(click());
	}

	public static ViewInteraction phoneCountryCodeText() {
		return onView(withParent(withId(R.id.edit_phone_number_country_code_spinner)));
	}
}
