package com.expedia.bookings.test.phone.rail;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.RailTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

public class RailFareRulesTest extends RailTestCase {

	public void testRailAmenities() throws Throwable {
		navigateFromLaunchToFareRules();
		assertFareRules();
	}

	private void navigateFromLaunchToFareRules() {
		RailScreen.calendarButton().perform(click());
		LocalDate firstStartDate = LocalDate.now().plusDays(10);
		RailScreen.selectDates(firstStartDate, firstStartDate.plusDays(2));
		RailScreen.dialogDoneButton().perform(click());

		SearchScreen.searchButton().perform(click());

		onView(withText("11:55 AM – 3:22 PM")).perform(ViewActions.waitForViewToDisplay()).check(matches(isDisplayed())).perform(click());
		onView(withText("Walk from London Euston to London Paddington")).check(matches(isDisplayed()));

		RailScreen.scrollToFareOptions();
		onView(withText("Any off-peak train")).check(matches(isDisplayed()));
		RailScreen.clickFareRules("First");
	}

	private void assertFareRules() {
		assertFareTitleIsVisible();
		assertFareRuleIsDisplayed("Additional information about this fare can be found");
		assertFareRuleIsDisplayed("Valid only for travel via (changing trains or passing through) London.");
		assertFareRuleIsDisplayed("Your ticket is refundable before 5 Aug, 2016 01:30 Coordinated Universal Time");
		assertFareRuleIsDisplayed("If you cancel before your ticket is printed, a penalty of 10.00 GBP will be deducted from your refund");
		assertFareRuleIsDisplayed("If you cancel after your ticket is printed, a penalty of up to 10 GBP per printed ticket per passenger will be deducted from your refund.");
	}

	private void assertFareRuleIsDisplayed(String fareRule) {
		onView(withText(containsString(fareRule))).check(matches(isDisplayed()));
	}

	private void assertFareTitleIsVisible() {
		Matcher<View> matcher = Matchers.allOf(
			withText("First anytime single"),
			withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
			withParent(withId(R.id.fare_rules_widget)));
		onView(matcher).check(matches(isDisplayed()));
	}

}
