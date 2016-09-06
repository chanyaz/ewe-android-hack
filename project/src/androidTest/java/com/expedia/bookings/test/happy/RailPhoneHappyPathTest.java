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
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

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

		onView(withId(R.id.rail_slide_to_purchase_widget)).check(matches(isDisplayed()));
	}

	private void assertCheckoutDisplayed() {
		onView(withId(R.id.rail_traveler_card_view)).check(matches(isDisplayed()));
	}

	private void assertLegInfo() {
		RailScreen.legInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("3:55 PM – 7:22 PM")))));
		RailScreen.legInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("3h 27m, 2 Changes")))));
		RailScreen.legInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Virgin Trains, London Underground, First Great Western")))));
	}

	private void assertDetailsExpanded() {
		RailScreen.detailsIcon().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.rail_leg_details);
		RailScreen.detailsIcon().perform(click());
	}

	private void assertDetailsCollapsed() {
		EspressoUtils.assertViewIsNotDisplayed(R.id.rail_leg_details);
	}
}
