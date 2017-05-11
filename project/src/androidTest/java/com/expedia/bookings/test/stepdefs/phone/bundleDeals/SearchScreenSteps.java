package com.expedia.bookings.test.stepdefs.phone.bundleDeals;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import cucumber.api.java.en.When;
import cucumber.api.java.en.And;

import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;


public class SearchScreenSteps {
	@When("^I enter source and destination for packages$")
	public void enterSourceAndDestinationForPackages(Map<String, String> parameters) throws Throwable {
		SearchScreen.searchEditText()
			.perform(waitForViewToDisplay(), typeText(parameters.get("source")));
		SearchScreen.selectLocation(parameters.get("source_suggest"));
		SearchScreen.searchEditText()
			.perform(waitForViewToDisplay(), typeText(parameters.get("destination")));
		SearchScreen.selectLocation(parameters.get("destination_suggest"));
	}

	@And("^Validate the date selected on calender button$")
	public void validateSelectedDateOnCalenderButtonForPackage(Map<String, String> parameters) throws Throwable {
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
}
