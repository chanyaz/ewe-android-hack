package com.expedia.bookings.test.ui.phone.pagemodels.common;

import android.util.Log;

import com.expedia.bookings.R;
import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/7/14.
 */
public class ScreenActions {

	public static void enterLog(String tag, String logText) {
		Log.v(tag, logText);
	}

	public static void delay(int seconds) {
		seconds = seconds * 1000;
		try {
			Thread.sleep(seconds);
		}
		catch (InterruptedException e) {
			//ignore
		}
	}

	protected static ViewInteraction positiveButton() {
		return onView(withId(R.id.positive_button));
	}

	protected static ViewInteraction negativeButton() {
		return onView(withId(R.id.negative_button));
	}

}
