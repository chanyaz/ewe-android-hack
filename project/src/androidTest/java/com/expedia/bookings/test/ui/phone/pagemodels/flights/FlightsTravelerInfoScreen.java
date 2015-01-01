package com.expedia.bookings.test.ui.phone.pagemodels.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonTravelerInformationScreen;
import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/8/14.
 */
public class FlightsTravelerInfoScreen extends CommonTravelerInformationScreen {
	private static final int REDRESS_EDITTEXT_ID = R.id.edit_redress_number;

	public static ViewInteraction redressEditText() {
		return onView(withId(REDRESS_EDITTEXT_ID));
	}

	public static void typeRedressText(String redressText) {
		redressEditText().perform(typeText(redressText));
	}

	public static void clickSetButton() {
		onView(withText("Set")).perform(click());
	}

	public static void clickTravelerDetails() {
		onView(withId(R.id.traveler_empty_text_view)).perform(click());
	}
}
