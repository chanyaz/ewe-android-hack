package com.expedia.bookings.test.phone.pagemodels.common;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

public class TripsScreen {
	private static final int LOG_IN_BUTTON_ID = R.id.status_refresh_button;

	public static ViewInteraction addGuestItinButton() {
		return onView(withId(R.id.add_guest_itin_text_view));
	}

	public static ViewInteraction logInButton() {
		return onView(allOf(withId(LOG_IN_BUTTON_ID), withText(R.string.sign_in_for_your_trips)));
	}

	public static ViewInteraction refreshTripsButton() {
		return onView(allOf(withId(LOG_IN_BUTTON_ID), withText("Refresh Trips")));
	}

	public static void clickOnLogInButton() {
		logInButton().perform(click());
	}

	public static DataInteraction tripsListItem() {
		return onData(anything()).inAdapterView(withId(android.R.id.list));
	}

	public static ViewInteraction enterItinDetailsView() {
		return onView(withId(R.id.itin_heading_textview));
	}
}
