package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.expedia.bookings.R;
import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/7/14.
 */
public class TripsScreen extends LaunchActionBar {
	private static final int LOG_IN_BUTTON_ID = R.id.login_button;

	public static ViewInteraction logInButton() {
		return onView(withId(LOG_IN_BUTTON_ID));
	}

	public static void clickOnLogInButton() {
		logInButton().perform(click());
	}

}
