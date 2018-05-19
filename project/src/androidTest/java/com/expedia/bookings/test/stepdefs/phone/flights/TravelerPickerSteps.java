package com.expedia.bookings.test.stepdefs.phone.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

public class TravelerPickerSteps {
	@Then("^I click on guest button$")
	public void clickOnGuestButton() throws Throwable {
		SearchScreen.selectGuestsButton().perform(click());
	}

	@Then("^I increase the adult count to max$")
	public void increaseAdultCount() throws Throwable {
		SearchScreenActions.clickIncrementAdultTravelerButton();
		onView(allOf(isDescendantOfA(withId(R.id.adult_count_selector)), withId(R.id.traveler_type))).check(matches(withText(containsString("Adults"))));
		for (int i = 1; i < 5; i++) {
			SearchScreenActions.clickIncrementAdultTravelerButton();
		}
	}

	@Then("^I increase the adult count by: (\\d+)$")
	public void increaseAdultTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreenActions.clickIncrementAdultTravelerButton();
		}
	}

	@Then("^I increase the youth count by: (\\d+)$")
	public void increaseYouthTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreenActions.clickIncrementYouthTravelerButton();
		}
	}

	@Then("^I increase the child count by: (\\d+)$")
	public void increaseChildTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreenActions.clickIncrementChildTravelerButton();
		}
	}

	@Then("^I increase the infant count by: (\\d+)$")
	public void increaseInfantTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreenActions.clickIncrementInfantTravelerButton();
		}
	}

	@Then("^I decrease the adult count by: (\\d+)$")
	public void decreaseAdultTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreenActions.clickDecrementAdultTravelerButton();
		}
	}

	@Then("^I decrease the youth count by: (\\d+)$")
	public void decreaseYouthTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreenActions.clickDecrementYouthTravelerButton();
		}
	}

	@Then("^I decrease the child count by: (\\d+)$")
	public void decreaseChildTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreenActions.clickDecrementChildTravelerButton();
		}
	}

	@Then("^I decrease the infant count by: (\\d+)$")
	public void decreaseInfantTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreenActions.clickDecrementInfantTravelerButton();
		}
	}

	@And("^I press done$")
	public void pressDone() throws Throwable {
		onView(withId(android.R.id.button1)).perform(click());
	}

	@And("^reduce the travellers count$")
	public void reduceNumberOfTraveler() throws Throwable {
		SearchScreenActions.clickDecrementAdultTravelerButton();
	}

	@Then("^I increase the child count to max$")
	public void increaseChildCount() throws Throwable {
		SearchScreenActions.clickIncrementChildTravelerButton();
		onView(allOf(isDescendantOfA(withId(R.id.child_count_selector)), withId(R.id.traveler_type))).check(matches(withText(containsString("1 Child"))));
		for (int i = 1; i < 4; i++) {
			SearchScreenActions.clickIncrementChildTravelerButton();
		}
		onView(allOf(isDescendantOfA(withId(R.id.child_count_selector)), withId(R.id.traveler_type))).check(matches(withText(containsString("Children"))));
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
		SearchScreenActions.clickDecrementChildTravelerButton();
	}
}
