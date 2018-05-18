package com.expedia.bookings.test.stepdefs.phone.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;

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
		SearchScreen.incrementAdultTravelerButton();
		onView(allOf(isDescendantOfA(withId(R.id.adult_count_selector)), withId(R.id.traveler_type))).check(matches(withText(containsString("Adults"))));
		for (int i = 1; i < 5; i++) {
			SearchScreen.incrementAdultTravelerButton();
		}
	}

	@Then("^I increase the adult count by: (\\d+)$")
	public void increaseAdultTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreen.incrementAdultTravelerButton();
		}
	}

	@Then("^I increase the youth count by: (\\d+)$")
	public void increaseYouthTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreen.incrementYouthTravelerButton();
		}
	}

	@Then("^I increase the child count by: (\\d+)$")
	public void increaseChildTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreen.incrementChildTravelerButton();
		}
	}

	@Then("^I increase the infant count by: (\\d+)$")
	public void increaseInfantTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreen.incrementInfantTravelerButton();
		}
	}

	@Then("^I decrease the adult count by: (\\d+)$")
	public void decreaseAdultTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreen.decrementAdultTravelerButton();
		}
	}

	@Then("^I decrease the youth count by: (\\d+)$")
	public void decreaseYouthTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreen.decrementYouthTravelerButton();
		}
	}

	@Then("^I decrease the child count by: (\\d+)$")
	public void decreaseChildTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreen.decrementChildTravelerButton();
		}
	}

	@Then("^I decrease the infant count by: (\\d+)$")
	public void decreaseInfantTravelerCount(int number) throws Throwable {
		for (int i = 0; i < number; i++) {
			SearchScreen.decrementInfantTravelerButton();
		}
	}

	@And("^I press done$")
	public void pressDone() throws Throwable {
		onView(withId(android.R.id.button1)).perform(click());
	}

	@And("^reduce the travellers count$")
	public void reduceNumberOfTraveler() throws Throwable {
		SearchScreen.decrementAdultTravelerButton();
	}

	@Then("^I increase the child count to max$")
	public void increaseChildCount() throws Throwable {
		SearchScreen.incrementChildTravelerButton();
		onView(allOf(isDescendantOfA(withId(R.id.child_count_selector)), withId(R.id.traveler_type))).check(matches(withText(containsString("1 Child"))));
		for (int i = 1; i < 4; i++) {
			SearchScreen.incrementChildTravelerButton();
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
		SearchScreen.decrementChildTravelerButton();
	}
}
