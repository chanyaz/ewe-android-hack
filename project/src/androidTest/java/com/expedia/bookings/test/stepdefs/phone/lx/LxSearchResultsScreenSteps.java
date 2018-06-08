package com.expedia.bookings.test.stepdefs.phone.lx;

import com.expedia.bookings.R;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;

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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;

public class LxSearchResultsScreenSteps {

	@And("^I wait for activities results to load$")
	public void waitForResultsToLoad() throws Throwable {
		LXScreen.waitForSearchListDisplayed();
	}

	@And("^Validate that lx search results are displayed$")
	public void validateThatFlightSearchResultsDisplayed() throws Throwable {
		LXScreen.waitForSearchListDisplayed();
	}

	@Then("^I click on search icon to go to search form for lx$")
	public void clickOnSearchIcon() throws Throwable {
		LXScreen.searchButtonOnSearchResultsToolbar().perform(click());
	}

	@Then("^on ASR the destination is \"(.*?)\"$")
	public void verifyDestination(String destination) throws Throwable {
		onView(allOf(withParent(withId(R.id.toolbar_two)), withText(containsString(destination))))
			.check(matches(withText(containsString(destination))));
	}

	@Then("^Validate that toolbar detail text is displayed for lx: (true|false)")
	public void validateToolbarDetailTextVisibility(boolean isDisplayed) throws Throwable {
		onView(allOf(withId(R.id.toolbar_detail_text), isDescendantOfA(withId(R.id.toolbar_lx_results))))
			.check(matches(isDisplayed()));
	}

	@Then("^Validate that toolbar subtitle text is displayed for lx: (true|false)")
	public void validateToolbarSubtitleTextVisibility(boolean isDisplayed) throws Throwable {
		onView(allOf(withId(R.id.toolbar_subtitle_text), isDescendantOfA(withId(R.id.toolbar_lx_results))))
			.check(matches(isDisplayed()));
	}

	@Then("^Validate that sort & filter is displayed")
	public void validateSortAndFilterVisibility() throws Throwable {
		onView(allOf(withId(R.id.sort_filter_button), isDescendantOfA(withId(R.id.lx_search_results_widget))))
			.check(matches(isDisplayed()));
	}
}
