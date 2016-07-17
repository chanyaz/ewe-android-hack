package com.expedia.bookings.test.phone.accessibility.packages;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.packages.PackageScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.CustomMatchers.withInfoText;

public class PackagesCheckoutTest extends PackageTestCase {

	public void testTravelerWidget() throws Throwable {
		PackageScreen.doPackageSearch();
		PackageScreen.checkout().perform(click());

		PackageScreen.travelerInfo().perform(scrollTo(), click());
		Espresso.closeSoftKeyboard();
		onView(withId(R.id.first_name_input)).check(matches(withInfoText(" First Name")));
		onView(withId(R.id.last_name_input)).check(matches(withInfoText(" Last Name")));
		onView(withId(R.id.edit_phone_number)).check(matches(withInfoText(" Phone Number")));
		onView(withId(R.id.edit_birth_date_text_btn)).check(matches(withInfoText(" Date of Birth")));
		PackageScreen.clickTravelerAdvanced();
		onView(withId(R.id.redress_number)).check(matches(withInfoText(" Redress # (if applicable)")));
		PackageScreen.clickTravelerAdvanced();
		Common.pressBack();

		PackageScreen.enterTravelerInfo();
		onView(withId(R.id.first_name_input)).check(matches(withInfoText(" First Name, FiveStar")));
		onView(withId(R.id.last_name_input)).check(matches(withInfoText(" Last Name, Bear")));
		onView(withId(R.id.edit_phone_number)).check(matches(withInfoText(" Phone Number, 7732025862")));
		onView(withId(R.id.edit_birth_date_text_btn)).check(matches(withInfoText(" Date of Birth, Jan 1, 1900")));
		onView(withId(R.id.redress_number)).check(matches(withInfoText(" Redress # (if applicable), 1234567")));
		Common.pressBack();
	}
}
