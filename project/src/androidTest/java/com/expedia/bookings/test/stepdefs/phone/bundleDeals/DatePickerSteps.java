package com.expedia.bookings.test.stepdefs.phone.bundleDeals;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.pagemodels.common.SearchScreen;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class DatePickerSteps {
	@Then("^Text \"(.*?)\"  should be displayed below calender title$")
	public void validateCalenderSubtitle(String text) throws Throwable {
		SearchScreen.calendarSubtitle().check(matches(allOf(withText(text), isDisplayed())));
	}

	@And("^I choose date from calendar widget$")
	public void validateDateForPackages(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		Format dateFormatter = new SimpleDateFormat("MMM d", Locale.US);
		String stDateStr = dateFormatter.format(stDate.toDate()).toString();
		LocalDate incrdate = stDate.plusDays(1);
		String incrdateStr = dateFormatter.format(incrdate.toDate()).toString();
		String finalstr = stDateStr + " - " + incrdateStr;
		if (parameters.get("end_date") != null) {
			LocalDate endDate = LocalDate.now()
				.plusDays(Integer.parseInt(parameters.get("end_date")));
			String endDateStr = dateFormatter.format(endDate.toDate()).toString();
			//choose departure and run date
			SearchScreen.chooseDates(stDate, endDate);
			//validate calender tooltip and subtitle
			SearchScreen.validateDatesToolTip(stDateStr + " - " + endDateStr, "Drag to modify");
			validateCalenderSubtitle(
				stDateStr + " - " + endDateStr + " " + parameters.get("number_of_nights"));
		}
		else {
			//choose departure date
			SearchScreen.chooseDates(stDate, null);
			//validate calender tooltip and subtilte
			SearchScreen.validateDatesToolTip(stDateStr, "Next: Select return date");
			validateCalenderSubtitle(stDateStr + " – Select return date");
		}
	}
}
