package com.expedia.bookings.test.pagemodels.common;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

public class TripsScreen {
	private static final int LOG_IN_BUTTON_ID = R.id.account_sign_in_container;
	private static final int LOG_IN_TEXT_VIEW_ID = R.id.login_text_view;

	public static ViewInteraction addGuestItinButton() {
		return onView(withId(R.id.add_guest_itin_text_view));
	}

	public static ViewInteraction logInButton() {
		return onView(withId(LOG_IN_BUTTON_ID));
	}

	public static ViewInteraction refreshTripsButtonText() {
		return onView(allOf(withId(LOG_IN_TEXT_VIEW_ID), withText("Refresh your trips")));
	}

	public static void clickOnLogInButton() {
		logInButton().perform(click());
	}

	public static DataInteraction tripsListItem() {
		return onData(anything()).inAdapterView(withId(android.R.id.list));
	}

	public static ViewInteraction enterItinToolbarText() {
		return onView(allOf(withText("Find Guest Booked Trip"), isDescendantOfA(withId(R.id.toolbar))));
	}
}
