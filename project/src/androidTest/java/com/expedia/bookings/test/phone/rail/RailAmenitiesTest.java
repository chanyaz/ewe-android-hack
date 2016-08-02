package com.expedia.bookings.test.phone.rail;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.expedia.bookings.test.espresso.RailTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.packages.RailScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
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
		RailScreen.calendarButton().perform(click());
		LocalDate firstStartDate = LocalDate.now().plusDays(10);
		RailScreen.selectDates(firstStartDate, firstStartDate.plusDays(2));
		RailScreen.dialogDoneButton().perform(click());

		RailScreen.searchButton().perform(click());

		onView(withText("11:55 AM – 3:22 PM")).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed())).perform(click());
		onView(withText("Walk from London Euston to London Paddington")).check(matches(isDisplayed()));

		RailScreen.scrollToFareOptions();
		onView(withText("Any off-peak train")).check(matches(isDisplayed()));
		RailScreen.clickAmenitiesLink("Any off-peak train");
	}

	private void assertAmenities() {
		onView(withText("Manchester Piccadilly to London Euston")).check(matches(isDisplayed()));
		assertAmenityIsDisplayed("• Buffet Service\n• WiFi Access (Additional Cost)\n",
			"Manchester Piccadilly to London Euston");
		assertAmenityIsDisplayed("No amenities", "London Paddington to Reading");
	}

	private void assertAmenityIsDisplayed(String amenityString, String segmentName) {
		Matcher<View> matcher = Matchers.allOf(
			withText(amenityString),
			withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
			hasSibling(withText(segmentName)));
		onView(matcher).check(matches(isDisplayed()));
	}
}
