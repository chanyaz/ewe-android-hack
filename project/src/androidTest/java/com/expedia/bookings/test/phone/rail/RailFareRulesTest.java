package com.expedia.bookings.test.phone.rail;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.RailTestCase;
import com.expedia.bookings.test.pagemodels.rail.RailScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

public class RailFareRulesTest extends RailTestCase {

	@Test
	public void testFareRules() throws Throwable {
		navigateFromSearchToFareRules();
		assertFareRules();
	}

	private void navigateFromSearchToFareRules() throws Throwable {
		RailScreen.navigateToDetails();
		RailScreen.scrollToOutboundFareOptions();
		onView(withText("Any off-peak train")).check(matches(isDisplayed()));
		RailScreen.clickFareRules("First Anytime Single (1st Class)", "Travel anytime of day");
	}

	private void assertFareRules() {
		assertFareTitleIsVisible();
		assertFareRuleIsDisplayed("Additional information about this fare can be found");
		assertFareRuleIsDisplayed("Valid only for travel via (changing trains or passing through) London.");
		assertFareRuleIsDisplayed("Your ticket is refundable before 9 Dec, 2016 12:55");
		assertFareRuleIsDisplayed("If you cancel before your ticket is printed, an admin fee of 40.00 GBP will be deducted from your refund.");
		assertFareRuleIsDisplayed("If you cancel after your ticket is printed, an admin fee of up to 10.00 GBP per ticket per passenger will be deducted from your refund. If the ticket is less than 10.00 GBP");
	}

	private void assertFareRuleIsDisplayed(String fareRule) {
		onView(withText(containsString(fareRule))).check(matches(isDisplayed()));
	}

	private void assertFareTitleIsVisible() {
		Matcher<View> matcher = Matchers.allOf(
			withText("First Anytime Single (1st Class)"),
			withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
			withParent(withId(R.id.fare_rules_widget)));
		onView(matcher).check(matches(isDisplayed()));
	}

}
