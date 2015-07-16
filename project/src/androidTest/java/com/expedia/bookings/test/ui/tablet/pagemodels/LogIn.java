package com.expedia.bookings.test.ui.tablet.pagemodels;

import com.expedia.bookings.R;
import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 5/30/14.
 */
public class LogIn {

	public static ViewInteraction loginExpediaButton() {
		return onView(withId(R.id.log_in_btn));
	}

	public static ViewInteraction loginFacebookButton() {
		return onView(withId(R.id.log_in_with_facebook_btn));
	}

	public static void clickLoginExpediaButton() {
		loginExpediaButton().perform(click());
	}

	public static void enterUserName(String text) {
		onView(withId(R.id.username_edit_text)).check(matches(isDisplayed()));
		onView(withId(R.id.username_edit_text)).perform(click(), typeText(text));
	}

	public static void enterPassword(String text) {
		onView(withId(R.id.password_edit_text)).perform(typeText(text));
	}
}
