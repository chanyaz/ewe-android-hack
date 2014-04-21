package com.expedia.bookings.test.tests.pageModelsEspresso.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonTravelerInformationScreen;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

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

	public static ViewInteraction passportString() {
		return onView(withText(R.string.passport));
	}

	public static void  clickSetButton(){
		onView(withText("Set")).perform(click());

	}
}
