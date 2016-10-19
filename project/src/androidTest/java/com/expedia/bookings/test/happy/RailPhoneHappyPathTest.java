package com.expedia.bookings.test.happy;

import org.hamcrest.CoreMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.RailTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.rail.RailScreen;

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

	public void testRailPhoneHappyPath() throws Throwable {
		RailScreen.navigateToTripOverview();
		assertLegInfo();
		assertDetailsCollapsed();
		assertDetailsExpanded();

		//assert info container
		RailScreen.fareDesciptionInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Travel anytime of day")))));

		RailScreen.checkout().perform(click());

		RailScreen.clickTravelerCard();
		RailScreen.fillInTraveler();
		assertCheckoutDisplayed();

		CheckoutViewModel.waitForPaymentInfoDisplayed();
		CheckoutViewModel.paymentInfo().perform(click());
		RailScreen.enterPaymentDetails();

		RailScreen.performSlideToPurchase();
		assertConfirmationScreen();
	}

	private void assertCheckoutDisplayed() {
		onView(withId(R.id.rail_traveler_card_view)).check(matches(isDisplayed()));
	}

	private void assertLegInfo() {
		RailScreen.legInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("12:55 PM – 4:16 PM")))));
		RailScreen.legInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("3h 21m, 2 Changes")))));
		RailScreen.legInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Multiple train operators")))));
	}

	private void assertDetailsExpanded() {
		RailScreen.detailsIcon().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.rail_leg_details);
		RailScreen.detailsIcon().perform(click());
	}

	private void assertDetailsCollapsed() {
		EspressoUtils.assertViewIsNotDisplayed(R.id.rail_leg_details);
	}

	private void assertConfirmationScreen() {
		onView(allOf(withId(R.id.destination), withText("Reading"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.first_row), isDescendantOfA(withId(R.id.outbound_leg_card)), withText("Manchester Piccadilly to Reading"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.second_row), isDescendantOfA(withId(R.id.outbound_leg_card)), withText("12:55 pm, 4 Travelers"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.itin_number), withText("#7938604594 sent to noah@mobiata.com"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.view_itin_button), withText("View Itinerary"))).check(matches(isDisplayed()));
	}
}
