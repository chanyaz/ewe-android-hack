package com.expedia.bookings.test.phone.rail;

import org.hamcrest.Matcher;
import org.junit.Test;

import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.RailTestCase;
import com.expedia.bookings.test.pagemodels.rail.RailScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class RailAmenitiesTest extends RailTestCase {

	@Test
	public void testRailAmenities() throws Throwable {
		navigateFromSearchToAmenities();
		assertTabVisible();
		assertAmenities();
	}

	private void navigateFromSearchToAmenities() throws Throwable {
		RailScreen.navigateToDetails();
		RailScreen.scrollToOutboundFareOptions();
		onView(withText("Any off-peak train")).check(matches(isDisplayed()));
		RailScreen.clickAmenitiesLink("Any off-peak train");
	}

	private void assertTabVisible() {
		onView(allOf(isDescendantOfA(withId(R.id.amenities_rules_tabs)),
			withText(R.string.amenities))).check(matches(isDisplayed()));
		onView(allOf(isDescendantOfA(withId(R.id.amenities_rules_tabs)),
			withText(R.string.fare_rules))).check(matches(isDisplayed()));
	}

	private void assertAmenities() {
		onView(withText("Manchester Piccadilly to London Euston")).check(matches(isDisplayed()));
		assertAmenityIsDisplayed("• Buffet Service\n• WiFi Access (Additional Cost)\n",
			"Manchester Piccadilly to London Euston");
		assertAmenityIsDisplayed("• Snack Trolley Service\n", "London Paddington to Reading");
	}

	private void assertAmenityIsDisplayed(String amenityString, String segmentName) {
		Matcher<View> matcher = allOf(
			withText(amenityString),
			withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
			hasSibling(withText(segmentName)));
		onView(matcher).check(matches(isDisplayed()));
	}
}
