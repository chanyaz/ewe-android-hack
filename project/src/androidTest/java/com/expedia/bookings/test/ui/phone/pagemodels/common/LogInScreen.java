package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.expedia.bookings.R;
import android.support.test.espresso.ViewInteraction;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class LogInScreen {

	// Object access

	public static ViewInteraction emailAddressEditText() {
		return onView(withId(R.id.username_edit_text));
	}

	public static ViewInteraction passwordEditText() {
		return onView(withId(R.id.password_edit_text));
	}

	public static ViewInteraction facebookButton() {
		return onView(withId(R.id.log_in_with_facebook_btn));
	}

	public static ViewInteraction logInButton() {
		return onView(withId(R.id.log_in_btn));
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
