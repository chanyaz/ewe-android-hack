package com.expedia.bookings.test.ui.tablet.pagemodels;

import android.app.Instrumentation;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.mobiata.android.Log;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
import static org.hamcrest.CoreMatchers.not;

public class Common {
	public static void closeSoftKeyboard(ViewInteraction v) {
		v.perform(ViewActions.closeSoftKeyboard());
		ScreenActions.delay(1);
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

	public static void enterLog(String tag, String logText) {
		Log.v(tag, logText);
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

	public static void pressBackOutOfApp(Instrumentation inst) {
		try {
			if (!SpoonScreenshotUtils.hasActiveActivity()) {
				Log.v("No activity to back out of");
				return;
			}

			for (int i = 0; i < 30; i++) {
				Espresso.pressBack();
			}

			throw new RuntimeException("Backed out 30 times but app didn't close!");
		}
		catch (Throwable e) {
			Log.v("Pressed back a bunch of times: ", e);
		}
	}
}
