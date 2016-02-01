package com.expedia.bookings.test.happy;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.RailTestCase;
import com.expedia.bookings.test.phone.packages.RailScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class RailPhoneHappyPathTest extends RailTestCase {

	public void testPackagePhoneHappyPath() throws Throwable {
		RailScreen.searchButton().perform(click());
		Common.delay(1);

		onView(withText("7:47 AM – 10:59 AM")).check(matches(isDisplayed()));
	}
}
