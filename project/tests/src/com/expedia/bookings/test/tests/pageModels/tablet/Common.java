package com.expedia.bookings.test.tests.pageModels.tablet;

import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.android.apps.common.testing.ui.espresso.action.ViewActions;

public class Common {
	public static void closeSoftKeyboard(ViewInteraction v) {
		v.perform(ViewActions.closeSoftKeyboard());
	}
}
