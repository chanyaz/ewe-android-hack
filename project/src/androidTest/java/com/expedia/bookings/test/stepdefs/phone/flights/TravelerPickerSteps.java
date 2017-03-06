package com.expedia.bookings.test.stepdefs.phone.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;


public class TravelerPickerSteps {
	@Then("^I click on guest button$")
	public void clickOnGuestButton() throws Throwable {
		SearchScreen.selectGuestsButton().perform(click());
	}

	@Then("^I increase the adult count to max$")
	public void increaseAdultCount() throws Throwable {
		SearchScreen.incrementAdultsButton();
		onView(withId(R.id.adult)).check(matches(withText(containsString("Adults"))));
		for (int i = 1; i < 5; i++) {
			SearchScreen.incrementAdultsButton();
		}
	}

	@And("^I press done$")
	public void pressDone() throws Throwable {
		onView(withId(android.R.id.button1)).perform(click());
	}

	@And("^reduce the travellers count$")
	public void reduceNumberOfTraveler() throws Throwable {
		SearchScreen.removeAdultsButton().perform(click());
	}

	@Then("^I increase the child count to max$")
	public void increaseChildCount() throws Throwable {
		SearchScreen.incrementChildrenButton();
		onView(withId(R.id.children)).check(matches(withText(containsString("1 Child"))));
		for (int i = 1; i < 4; i++) {
			SearchScreen.incrementChildrenButton();
		}
		onView(withId(R.id.children)).check(matches(withText(containsString("Children"))));
	}

	@And("^equal number of age pickers are shown$")
	public void checkChildAgeRepresenter() throws Throwable {
		onView(withId(R.id.child_spinner_1)).check(matches(isDisplayed()));
		onView(withId(R.id.child_spinner_2)).check(matches(isDisplayed()));
		onView(withId(R.id.child_spinner_3)).check(matches(isDisplayed()));
		onView(withId(R.id.child_spinner_4)).check(matches(isDisplayed()));
	}

	@And("^the default age is 10 years$")
	public void checkDefaultAge() throws Throwable {
		onView(withParent(withId(R.id.child_spinner_1))).check(matches(withText(containsString("10 years old"))));
		onView(withParent(withId(R.id.child_spinner_2))).check(matches(withText(containsString("10 years old"))));
		onView(withParent(withId(R.id.child_spinner_3))).check(matches(withText(containsString("10 years old"))));
		onView(withParent(withId(R.id.child_spinner_4))).check(matches(withText(containsString("10 years old"))));
	}

	@And("^Reduce the child count$")
	public void reduceChildCount() throws Throwable {
		SearchScreen.removeChildButton().perform(click());
	}

	@And("^corresponding age picker is removed$")
	public void checkAgeRepresnterVisibility() throws Throwable {
		onView(withId(R.id.child_spinner_4)).check(matches(not(isDisplayed())));
	}
}
