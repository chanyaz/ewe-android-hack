package com.expedia.bookings.test.ui.phone.pagemodels.flights;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonTravelerInformationScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/8/14.
 */
public class FlightsTravelerInfoScreen extends CommonTravelerInformationScreen {

	public static ViewInteraction redressEditText() {
		return onView(withId(R.id.edit_redress_number));
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

	public static void clickPopulatedTravelerDetails() {
		onView(withId(R.id.traveler_container)).perform(click());
		onView(withId(R.id.current_traveler_contact)).perform(click());
	}

	public static ViewInteraction nameMustMatchTextView() {
		return onView(withId(R.id.name_match_warning_text_view));
	}
}
