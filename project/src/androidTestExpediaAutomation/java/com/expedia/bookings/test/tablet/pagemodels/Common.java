package com.expedia.bookings.test.tablet.pagemodels;

import android.app.Instrumentation;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.android.apps.common.testing.ui.espresso.action.ViewActions;
import com.mobiata.android.Log;

import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.CoreMatchers.not;

public class Common {
	public static void closeSoftKeyboard(ViewInteraction v) {
		v.perform(ViewActions.closeSoftKeyboard());
	}

	public static void checkDisplayed(ViewInteraction v) {
		v.check(matches(isDisplayed()));
	}

	public static void checkNotDisplayed(ViewInteraction v) {
		v.check(matches(not(isDisplayed())));
	}

	public static void checkErrorIconDisplayed(ViewInteraction v) {
		v.check(matches(withCompoundDrawable(R.drawable.ic_error_blue)));
	}

	public static void checkErrorIconNotDisplayed(ViewInteraction v) {
		v.check(matches(not(withCompoundDrawable(R.drawable.ic_error_blue))));
	}

	public static void enterLog(String TAG, String logText) {
		android.util.Log.v(TAG, logText);
	}

	public static boolean isTablet(Instrumentation inst) {
		return ExpediaBookingApp.useTabletInterface(inst.getTargetContext());
	}

	public static boolean isPhone(Instrumentation inst) {
		return !isTablet(inst);
	}

	public static void pressBack() {
		try {
			Espresso.pressBack();
		}
		catch (Exception e) {
			Log.v("Pressed back and got an exception: ", e);
		}
	}

	public static void pressBackOutOfApp() {
		try {
			while (true) {
				Espresso.pressBack();
			}
		}
		catch (Exception e) {
			Log.v("Pressed back a bunch of times: ", e);
		}
	}
}
