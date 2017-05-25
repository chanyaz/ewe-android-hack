package com.expedia.bookings.test.stepdefs.phone.bundleDeals;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import org.joda.time.LocalDate;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.not;


public class SearchScreenSteps {
	@When("^I enter source and destination for packages$")

	public void enterSourceAndDestinationForPackages(Map<String, String> parameters)
		throws Throwable {

		SearchScreen.searchEditText()
			.perform(waitForViewToDisplay(), typeText(parameters.get("source")));


		SearchScreen.selectLocation(parameters.get("source_suggest"));
		SearchScreen.searchEditText()
			.perform(waitForViewToDisplay(), typeText(parameters.get("destination")));
		SearchScreen.selectLocation(parameters.get("destination_suggest"));
	}

	@And("^Validate the date selected on calender button$")
	public void validateSelectedDateOnCalenderButtonForPackage(Map<String, String> parameters)
		throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		Format dateFormatter = new SimpleDateFormat("MMM d", Locale.US);
		String stDateStr = dateFormatter.format(stDate.toDate()).toString();

		if (parameters.get("end_date") != null) {
			LocalDate endDate = LocalDate.now()
				.plusDays(Integer.parseInt(parameters.get("end_date")));
			String endDateStr = dateFormatter.format(endDate.toDate()).toString();

			SearchScreen.calendarCard().check(matches(withText(
				stDateStr + " - " + endDateStr + " " + parameters.get("number_of_nights"))));
		}
		else {
			LocalDate incrdate = stDate.plusDays(1);
			String incrdateStr = dateFormatter.format(incrdate.toDate()).toString();
			String finalstr = stDateStr + " - " + incrdateStr;
			SearchScreen.calendarCard().check(matches(withText(finalstr + " (1 night)")));

		}
	}

	@And("^Validate plus icon for Adults is disabled$")
	public void validateAdultsPlusIconDisabled() throws Throwable {
		onView(withId(R.id.adults_plus)).check(matches(not(isEnabled())));
	}

	@And("^Validate plus icon for Children is disabled$")
	public void validateChildrenPlusIconDisabled() throws Throwable {
		onView(withId(R.id.children_plus)).check(matches(not(isEnabled())));
	}

	@Then("^I increase the adult count$")
	public void incrementAdultCount() throws Throwable {
		onView(withId(R.id.adults_plus)).perform(click());
	}
}
