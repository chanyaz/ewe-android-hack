package com.expedia.bookings.test.stepdefs.phone.lx;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.hamcrest.core.AllOf;

import com.expedia.bookings.R;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;
import com.expedia.bookings.test.stepdefs.phone.TestUtil;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withIndex;
import static com.expedia.bookings.test.espresso.ViewActions.getString;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static com.expedia.bookings.test.stepdefs.phone.lx.LxDatePickerSteps.pickDates;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class LxSearchScreenSteps {

	@When("^I enter destination for lx$")
	public void enterDestination(Map<String, String> parameters) throws Throwable {
		SearchScreenActions.typeAndSelectLocation(parameters.get("destination"), parameters.get("destination_suggest"));
	}

	@Then("^I can trigger lx search$")
	public void searchClick() throws Throwable {
		LXScreen.searchButton().perform(click());
	}

	@Then("^Enter Destination field exists for lx search form$")
	public void checkEnterDestinationField() throws Throwable {
		SearchScreen.waitForSearchEditText();
	}

	@Then("^Validate that Done button is disabled for lx$")
	public void validateDoneButtonDisabled() throws Throwable {
		onView(withId(R.id.parentPanel)).check(matches(hasDescendant(allOf(withId(android.R.id.button1),
			withText("DONE"), not(isEnabled())))));
	}

	@Given("^I Click on Select Dates button for lx")
	public void clickSelectDatedButton() throws Throwable {
		LXScreen.selectDateButton().perform(click());
	}

	@And("^I make a lx search with following parameters$")
	public void lxSearchCall(Map<String, String> parameters) throws Throwable {
		TestUtil.dataSet = parameters;
		enterDestination(parameters);
		pickDates(parameters);
		searchClick();
	}

	@Then("^I store the activity name in \"(.*?)\"$")
	public void saveActivity(String key) throws Throwable {
		TestUtil.storeDataAtRuntime.put(key, getActivityName(1));
	}

	@Then("^I store the activity date range in \"(.*?)\"$")
	public void saveActivityDate(String key) throws Throwable {
		onView(AllOf.allOf(withId(R.id.toolbar_subtitle_text), isDescendantOfA(withId(R.id.toolbar_lx_results))))
			.perform(waitForViewToDisplay());
		TestUtil.storeDataAtRuntime.put(key, getActivityDate());
	}

	private String getActivityName(int position) {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withIndex(withId(R.id.activity_title), position)).perform(waitForViewToDisplay())
			.perform(getString(value));
		String activity = value.get();
		return activity;
	}

	private String getActivityDate() {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(AllOf.allOf(withId(R.id.toolbar_subtitle_text), isDescendantOfA(withId(R.id.toolbar_lx_results))))
			.perform(getString(value));
		String activity = value.get();
		return activity;
	}
}
