package com.expedia.bookings.test.tests.pageModels.tablet;

import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.android.apps.common.testing.ui.espresso.action.ViewActions;
import com.mobiata.android.Log;

public class Common {
	public static void closeSoftKeyboard(ViewInteraction v) {
		v.perform(ViewActions.closeSoftKeyboard());
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
