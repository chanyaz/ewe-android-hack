package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.expedia.bookings.R;
import android.support.test.espresso.ViewInteraction;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class LogInScreen {
	private static final int EMAIL_ADDRESS_ET_STRING_ID = R.id.username_edit_text;
	private static final int PASSWORD_ET_STRING_ID = R.id.password_edit_text;
	private static final int FACEBOOK_BTN_ID = R.id.log_in_with_facebook_btn;
	private static final int LOGIN_BTN_ID = R.id.log_in_with_expedia_btn;

	// Object access

	public static ViewInteraction emailAddressEditText() {
		return onView(withId(EMAIL_ADDRESS_ET_STRING_ID));
	}

	public static ViewInteraction passwordEditText() {
		return onView(withId(PASSWORD_ET_STRING_ID));
	}

	public static ViewInteraction facebookButton() {
		return onView(withId(FACEBOOK_BTN_ID));
	}

	public static ViewInteraction logInButton() {
		return onView(withId(LOGIN_BTN_ID));
	}

	// Object interaction

	public static void clickOnLoginButton() {
		logInButton().perform(click());
	}

	public static void typeTextEmailEditText(String text) {
		emailAddressEditText().perform(typeText(text));
	}

	public static void typeTextPasswordEditText(String text) {
		passwordEditText().perform(typeText(text));
	}
}
