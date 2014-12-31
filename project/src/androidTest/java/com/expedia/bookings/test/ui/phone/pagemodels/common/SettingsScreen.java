package com.expedia.bookings.test.ui.phone.pagemodels.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;


/**
 * Created by dmadan on 4/7/14.
 */
public class SettingsScreen extends ScreenActions {
	private static final int CLEAR_PRIVATE_DATE_STRING_ID = R.string.clear_private_data;
	private static final int OK_STRING_ID = R.string.ok;
	private static final int ACCEPT_STRING_ID = R.string.accept;
	private static final int CANCEL_STRING_ID = R.string.cancel;
	private static final int COUNTRY_STRING_ID = R.string.preference_point_of_sale_title;


	// Object access

	public static ViewInteraction okString() {
		return onView(withText(OK_STRING_ID));
	}

	public static ViewInteraction cancelString() {
		return onView(withText(CANCEL_STRING_ID));
	}

	public static ViewInteraction acceptString() {
		return onView(withText(ACCEPT_STRING_ID));
	}

	public static ViewInteraction country() {
		return onView(withText(COUNTRY_STRING_ID));
	}

	// Object interaction

	public static void clickCountryString() {
		country().perform(click());
	}

	public static void clickOkString() {
		okString().perform(click());
	}

	public static void clickCancelString() {
		cancelString().perform(click());
	}

	public static void clickacceptString() {
		acceptString().perform(click());
	}
}
