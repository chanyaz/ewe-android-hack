package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.swipeRight;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/7/14.
 */
public class TripsScreen extends LaunchActionBar {
	private static final int ENTER_ITINERARY_NUMBER_ID = R.id.or_enter_itin_number_tv;
	private static final int LOG_IN_BUTTON_ID = R.id.login_button;
	private static final int FETCHING_YOUR_ITINERARIES_STRING_ID = R.string.fetching_your_itinerary;
	private static final int ITIN_LIST_VIEW_ID = android.R.id.list;

	public static ViewInteraction itineraryListView() {
		return onView(withId(ITIN_LIST_VIEW_ID));
	}

	public static ViewInteraction fetchingYourItineraries() {
		return onView(withText(FETCHING_YOUR_ITINERARIES_STRING_ID));
	}

	public static ViewInteraction enterItinNumberView() {
		return onView(withId(ENTER_ITINERARY_NUMBER_ID));
	}

	public static ViewInteraction logInButton() {
		return onView(withId(LOG_IN_BUTTON_ID));
	}

	public static void clickEnterItinNumber() {
		enterItinNumberView().perform(click());
	}

	public static void clickOnLogInButton() {
		logInButton().perform(click());
	}

	public static void swipeToLaunchScreen() {
		swipeRight();
	}

}
