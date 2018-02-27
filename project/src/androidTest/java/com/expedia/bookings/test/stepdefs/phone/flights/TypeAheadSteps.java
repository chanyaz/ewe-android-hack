package com.expedia.bookings.test.stepdefs.phone.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

public class TypeAheadSteps {
	@Then("^flights suggest typeAhead is not fired$")
	public void verifySuggestionListEmpty() throws Throwable {
		SearchScreen.suggestionList().check(matches(not(hasDescendant(withId(R.id.suggestion_text_container)))));
	}

	@Then("^flights suggest typeAhead is fired for \"(.*?)\"$")
	public void verifySuggestionsForGivenQuery(String query) throws Throwable {
		SearchScreen.waitForSearchEditText().check(matches(withText(query)));
		if (query.equals("lon")) {
			SearchScreen.suggestionList()
				.check(matches(hasDescendant(withText("London, England, UK (LON - All Airports)"))));
		}
		else if (query.equals("lond")) {
			SearchScreen.suggestionList()
				.check(matches(hasDescendant(withText("San Francisco, CA (SFO-San Francisco Intl.)"))));
		}
	}

	@Then("^flights suggest typeAhead is fired$")
	public void checkTypeAheadFired() throws Throwable {
		SearchScreenActions.waitForSuggestions(hasDescendant(withId(R.id.suggestion_text_container)));
		SearchScreen.suggestionList().check(matches(hasDescendant(withId(R.id.suggestion_text_container))));
	}

	@Then("^\"(.*?)\" is listed at the top of suggestion list as recent search$")
	public void checkRecentSearchesSuggestionResults(String result) throws Throwable {
		SearchScreen.suggestionList()
			.check(matches(hasDescendant(withText(result))));
	}

	@And("^the results are listed in hierarchy$")
	public void verifyHierarchicalSuggestion() throws Throwable {
		SearchScreen.suggestionList().check(matches(hasDescendant(withId(R.id.hierarchy_imageview))));
	}


}
