package com.expedia.bookings.test.stepdefs.phone.bundleDeals;


import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class PackagesSearchSteps {
	//param 'ignore1' - could be either 'I type' or 'I add' and param 'ignore2' - could be either 'source' or 'destination'
	@When("^(I type|I add) \"(.*?)\" in the packages (source|destination) search box$")
	public void typeInSearchBox(String ignore1, String query, String ignore2) throws Throwable {
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(query));
	}
	@Then("^packages suggest typeAhead is not fired$")
	public void verifySuggestionListEmpty() throws Throwable {
		SearchScreen.suggestionList().check(matches(not(hasDescendant(withId(R.id.suggestion_text_container)))));
	}
	@Then("^validate \"(.*?)\" suggestion is fired for typing \"(.*?)\"$")
	public void verifySuggestionsForGivenQuery(String location, String query) throws Throwable {
		SearchScreen.searchEditText().check(matches(withText(query)));
		SearchScreen.suggestionList()
			.check(matches(allOf(hasDescendant(withText(containsString(location))), isDisplayed())));
	}
	@Then("^packages suggest typeAhead is fired$")
	public void checkTypeAheadFired() throws Throwable {
		SearchScreen.waitForSuggestions(hasDescendant(withId(R.id.suggestion_text_container)));
		SearchScreen.suggestionList().check(matches(allOf(hasDescendant(withId(R.id.suggestion_text_container)), isDisplayed())));
	}

}
