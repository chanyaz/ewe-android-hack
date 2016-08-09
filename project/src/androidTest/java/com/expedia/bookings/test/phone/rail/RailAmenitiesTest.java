package com.expedia.bookings.test.phone.rail;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.expedia.bookings.test.espresso.RailTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class RailAmenitiesTest extends RailTestCase {

	public void testRailAmenities() throws Throwable {
		navigateFromLaunchToAmenities();
		assertAmenities();
	}

	private void navigateFromLaunchToAmenities() {
		RailScreen.navigateToDetails();
		RailScreen.scrollToFareOptions();
		onView(withText("Any off-peak train")).check(matches(isDisplayed()));
		RailScreen.clickAmenitiesLink("Any off-peak train");
	}

	private void assertAmenities() {
		onView(withText("Manchester Piccadilly to London Euston")).check(matches(isDisplayed()));
		assertAmenityIsDisplayed("• Buffet Service\n• WiFi Access (Additional Cost)\n",
			"Manchester Piccadilly to London Euston");
		assertAmenityIsDisplayed("• Snack Trolley Service\n", "London Paddington to Reading");
	}

	private void assertAmenityIsDisplayed(String amenityString, String segmentName) {
		Matcher<View> matcher = Matchers.allOf(
			withText(amenityString),
			withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
			hasSibling(withText(segmentName)));
		onView(matcher).check(matches(isDisplayed()));
	}
}
