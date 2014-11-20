package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/7/14.
 */
public class LogInScreen extends ScreenActions {
	private static final int EMAIL_ADDRESS_ET_STRING_ID = R.id.username_edit_text;
	private static final int PASSWORD_ET_STRING_ID = R.id.password_edit_text;
	private static final int FACEBOOK_BTN_ID = R.id.log_in_with_facebook_btn;
	private static final int LOGIN_BTN_ID = R.id.log_in_with_expedia_btn;
	private static final int FORGOT_PASSWORD_LINK_ID = R.id.forgot_your_password_link;
	private static final int LOGGING_IN_STRING_ID = R.string.logging_in;
	private static final int FETCHING_ITINERARIES_STRING_ID = R.string.fetching_your_itinerary;
	private static final int ERROR_FETCHING_ITINERARIES_STRING_ID = R.string.itinerary_fetch_error;
	private static final int NO_TRIPS_STRING = R.string.no_upcoming_trips;

	// Object access

	public static ViewInteraction emailAddressEditText() {
		return onView(withId(EMAIL_ADDRESS_ET_STRING_ID));
	}

	public static ViewInteraction passwordEditText() {
		return onView(withId(PASSWORD_ET_STRING_ID));
	}

	public static ViewInteraction facebookButton() {
		return onView(withId(FACEBOOK_BTN_ID));
	}

	public static ViewInteraction logInButton() {
		return onView(withId(LOGIN_BTN_ID));
	}

	public static ViewInteraction forgotPasswordLink() {
		return onView(withId(FORGOT_PASSWORD_LINK_ID));
	}

	public static ViewInteraction loggingInDialogString() {
		return onView(withText(LOGGING_IN_STRING_ID));
	}

	public static ViewInteraction fetchingYourItineraries() {
		return onView(withText(FETCHING_ITINERARIES_STRING_ID));
	}

	public static ViewInteraction errorFetchingYourItineraries() {
		return onView(withText(ERROR_FETCHING_ITINERARIES_STRING_ID));
	}

	public static ViewInteraction noUpcomingTrips() {
		return onView(withText(NO_TRIPS_STRING));
	}

	// Object interaction

	public static void clickOnFacebookButton() {
		facebookButton().perform(click());
	}

	public static void clickOnLoginButton() {
		logInButton().perform(click());
	}

	public static void clickOnForgotPasswordLink() {
		forgotPasswordLink().perform(click());
	}

	public static void typeTextEmailEditText(String text) {
		emailAddressEditText().perform(typeText(text));
	}

	public static void typeTextPasswordEditText(String text) {
		passwordEditText().perform(typeText(text));
	}
}
