package com.expedia.bookings.test.happy;

import org.hamcrest.Matcher;
import org.junit.Test;

import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.RailTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.rail.RailScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class RailPhoneHappyPathTest extends RailTestCase {

	@Test
	public void testRailPhoneHappyPath() throws Throwable {
		RailScreen.navigateToTripOverview();
		assertLegInfo();
		assertOutboundDetailsCollapsed();
		assertOutboundDetailsExpanded();

		//assert info container
		RailScreen.ouboundFareDescriptionInfo().check(matches(
			allOf(isDisplayed(), withText("Travel anytime of day"))));

		RailScreen.checkoutAndPurchase();

		assertConfirmationScreen();
	}

	@Test
	public void testRoundTripSearch() throws Throwable {
		RailScreen.performRoundTripSearch();
		onView(withText(R.string.select_outbound)).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed()));

		RailScreen.selectRoundTripOutbound();
		EspressoUtils.assertViewIsDisplayed(R.id.outbound_header_view);

		RailScreen.selectRoundTripInbound();
		RailScreen.checkoutAndPurchase();

		onView(allOf(withId(R.id.view_itin_button), withText("View Itinerary")))
			.perform(ViewActions.waitForViewToDisplay())
			.check(matches(isDisplayed()));
	}

	private void assertLegInfo() {
		RailScreen.outboundLegInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("12:55 PM – 4:16 PM")))));
		RailScreen.outboundLegInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("3h 21m, 2 Changes")))));
		RailScreen.outboundLegInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Multiple train operators")))));
	}

	private void assertOutboundDetailsExpanded() {
		RailScreen.outboundDetailsIcon().perform(click());
		Matcher<View> matcher = allOf(
			isDescendantOfA(withId(R.id.rail_outbound_leg_widget)),
			withId(R.id.rail_leg_details));

		EspressoUtils.assertViewIsDisplayed(matcher);
		RailScreen.outboundDetailsIcon().perform(click());
	}

	private void assertOutboundDetailsCollapsed() {
		Matcher<View> matcher = allOf(
			isDescendantOfA(withId(R.id.rail_outbound_leg_widget)),
			withId(R.id.rail_leg_details));

		EspressoUtils.assertViewIsNotDisplayed(matcher);
	}

	private void assertConfirmationScreen() {
		onView(allOf(withId(R.id.destination), withText("Glasgow (All Stations)"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.confirmation_title), isDescendantOfA(withId(R.id.outbound_leg_card)), withText("London (All Stations) to Glasgow (All Stations)"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.confirmation_subtitle), isDescendantOfA(withId(R.id.outbound_leg_card)), withText("7:43 am, 4 Travelers"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.itin_number), withText("#7938604594 sent to noah@mobiata.com"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.view_itin_button), withText("View Itinerary"))).check(matches(isDisplayed()));
	}
}
