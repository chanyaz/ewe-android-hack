package com.expedia.bookings.test.tests.pageModelsEspresso.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/7/14.
 */
public class FindItineraryScreen extends ScreenActions {
	private static final int sHeaderTextViewID = R.id.itin_heading_textview;
	private static final int sFindItineraryButtonID = R.id.find_itinerary_button;
	private static final int sEmailAddressEditTextID = R.id.email_edit_text;
	private static final int sItinNumberEditTextID = R.id.itin_number_edit_text;

	public static ViewInteraction findItineraryHeaderText() {
		return onView(withText(sHeaderTextViewID));
	}

	public static ViewInteraction findItineraryButton() {
		return onView(withId(sFindItineraryButtonID));
	}

	public static ViewInteraction emailAddressEditText() {
		return onView(withId(sEmailAddressEditTextID));
	}

	public static ViewInteraction itinNumberEditText() {
		return onView(withId(sItinNumberEditTextID));
	}

	public static void clickFindItineraryButton() {
		findItineraryButton().perform(click());
	}
}
