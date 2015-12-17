package com.expedia.bookings.test.ui.phone.pagemodels.common;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;

public class TripsScreen extends LaunchActionBar {
	private static final int LOG_IN_BUTTON_ID = R.id.login_button;

	public static ViewInteraction logInButton() {
		return onView(withId(LOG_IN_BUTTON_ID));
	}

	public static void clickOnLogInButton() {
		logInButton().perform(click());
	}

	public static DataInteraction tripsListItem() {
		return onData(anything()).inAdapterView(withId(android.R.id.list));
	}

}
