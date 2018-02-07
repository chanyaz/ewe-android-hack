package com.expedia.bookings.test.pagemodels.common;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;

public class LogInScreen {

	// Object access

	public static ViewInteraction emailAddressEditText() {
		return onView(allOf(withId(R.id.input_text), withParent(withId(R.id.email_address_sign_in))));
	}

	public static ViewInteraction passwordEditText() {
		return onView(allOf(withId(R.id.input_text), withParent(withId(R.id.password))));
	}

	public static ViewInteraction logInButton() {
		return onView(withId(R.id.sign_in_button));
	}

	// Object interaction

	public static void clickOnLoginButton() {
		Common.closeSoftKeyboard(LogInScreen.passwordEditText());
		Common.delay(1);
		logInButton().perform(click());
		Common.delay(2);
	}

	public static void typeTextEmailEditText(String text) {
		emailAddressEditText().perform(typeText(text));
	}

	public static void typeTextPasswordEditText(String text) {
		passwordEditText().perform(typeText(text));
	}

	public static void signIn(String username) {
		typeTextEmailEditText(username);
		typeTextPasswordEditText("password");
		clickOnLoginButton();
	}
}
