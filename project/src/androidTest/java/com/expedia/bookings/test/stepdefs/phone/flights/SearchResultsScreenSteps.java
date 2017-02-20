package com.expedia.bookings.test.stepdefs.phone.flights;

import java.util.concurrent.TimeUnit;
import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.newflights.FlightsScreen;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;

public class SearchResultsScreenSteps {
	@Then("^I select first flight$")
	public void selectFirstFlight() throws Throwable {
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0);
		FlightsScreen.selectOutboundFlight().perform(click());
	}

	@And("^I wait for results to load$")
	public void waitForResultsToLoad() throws Throwable {
		onView(withId(R.id.sort_filter_button)).perform(waitFor(isDisplayed(), 20, TimeUnit.SECONDS));
	}

	@Then("^on FSR the destination is \"(.*?)\"$")
	public void verifyDestination(String destination) throws Throwable {
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Select flight to"))))
			.check(matches(withText(containsString(destination))));
	}

	@And("^Validate that flight search results are displayed$")
	public void validateThatFlightSearchResultsDisplyed() throws Throwable {
		onView(withId(R.id.list_view)).check(matches(isDisplayed()));
	}

	@Then("^I click on search icon to go to search form$")
	public void clickOnSearchIcon() throws Throwable {
		onView(withId(R.id.sort_filter_button)).perform(waitFor(isCompletelyDisplayed(), 20, TimeUnit.SECONDS));
		onView(withId(R.id.menu_search)).perform(click());
	}
}
