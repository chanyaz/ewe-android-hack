package com.expedia.bookings.test.tablet.pagemodels;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 1/8/15.
 */
public class Itin {

	public static void clickTripsMenuButton() {
		onView(withId(R.id.menu_your_trips)).perform(click());
	}
}
