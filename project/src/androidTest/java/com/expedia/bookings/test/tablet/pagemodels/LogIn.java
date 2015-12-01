package com.expedia.bookings.test.tablet.pagemodels;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by dmadan on 5/30/14.
 */
public class LogIn {

	public static ViewInteraction loginExpediaButton() {
		return onView(withId(R.id.sign_in_button));
	}

	public static ViewInteraction loginFacebookButton() {
		return onView(withId(R.id.sign_in_with_facebook_button));
	}

	public static void clickLoginExpediaButton() {
		Common.closeSoftKeyboard(onView(allOf(withId(R.id.password_edit_text), withParent(withId(R.id.password)))));
		Common.delay(1);
		loginExpediaButton().perform(click());
	}

	public static void enterUserName(String text) {
		onView(allOf(withId(R.id.input_text), withParent(withId(R.id.email_address_sign_in)))).check(
			matches(isDisplayed()));
		onView(allOf(withId(R.id.input_text), withParent(withId(R.id.email_address_sign_in)))).perform(click(),
			typeText(text));
	}

	public static void enterPassword(String text) {
		onView(allOf(withId(R.id.password_edit_text), withParent(withId(R.id.password)))).perform(typeText(text));
	}
}
