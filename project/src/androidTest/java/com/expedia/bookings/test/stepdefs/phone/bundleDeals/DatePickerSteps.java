package com.expedia.bookings.test.stepdefs.phone.bundleDeals;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.Months;

import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.stepdefs.phone.TestUtil.getFormattedDate;
import static org.hamcrest.Matchers.allOf;

public class DatePickerSteps {
	Format dateFormatterEEEMMMd = new SimpleDateFormat("EEE, MMM d", Locale.US);
	Format dateFormatMMMd = new SimpleDateFormat("MMM d", Locale.US);

	@Then("^Text \"(.*?)\"  should be displayed below calender title$")
	public void validateCalenderSubtitle(String text) throws Throwable {
		SearchScreen.calendarSubtitle().check(matches(allOf(withText(text), isDisplayed())));
	}

	@And("^I choose date from calendar widget$")
	public void validateDateForPackages(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));

		if (parameters.get("end_date") != null) {
			LocalDate endDate = LocalDate.now()
				.plusDays(Integer.parseInt(parameters.get("end_date")));
			//choose departure and return date
			SearchScreenActions.chooseDates(stDate, endDate);
		}
		else {
			//choose departure date
			SearchScreenActions.chooseDates(stDate, null);
		}
	}

	@And("^I open calendar widget$")
	public void openCalendarWidget() throws Throwable {
		SearchScreen.selectDateButton().perform(click());
	}

	// Validate calender tooltip and subtitle
	@Then("^Validate dates selected are correct$")
	public void validateDateSelected(Map<String, String> parameters) throws Throwable {
		String startDateParam = parameters.get("start_date");
		String endDateParam = parameters.get("end_date");

		LocalDate startDate = LocalDate.now().plusDays(Integer.parseInt(startDateParam));
		String startDateMMMd = getFormattedDate(startDate, dateFormatMMMd);
		String startDateEEEMMMd = getFormattedDate(startDate, dateFormatterEEEMMMd);
		if (endDateParam != null) {
			LocalDate endDate = LocalDate.now().plusDays(Integer.parseInt(endDateParam));
			String endDateMMMd = getFormattedDate(endDate, dateFormatMMMd);
			String endDateEEEMMMd = getFormattedDate(endDate, dateFormatterEEEMMMd);
			SearchScreenActions.validateDatesToolTip(startDateMMMd + " - " + endDateMMMd, "Drag to modify");
			validateCalenderSubtitle(
				startDateEEEMMMd + "  -  " + endDateEEEMMMd + " " + parameters.get("number_of_nights"));
		}
		else {
			SearchScreenActions.validateDatesToolTip(startDateMMMd, "Next: Select return date");
			validateCalenderSubtitle(startDateEEEMMMd + " – Select return date");
		}
	}

	@Then("^Validate that max end date selectable is (\\d+) days from now$")
	public void validateMaxDateSelectable(int max) throws Throwable {
		LocalDate now = LocalDate.now();
		LocalDate maxSelectableDate = LocalDate.now().plusDays(max);
		int monthCount = Months.monthsBetween(now, maxSelectableDate).getMonths();
		for (int i = 0; i < monthCount; i++) {
			SearchScreen.nextMonthButton().perform(click());
		}

		// We are not able to get individual days view from the calendar popup.
		// Forcefully setting invalid date in test would result in selection of max selectable date as start and end.
		SearchScreenActions.chooseDates(maxSelectableDate.plusDays(1), null);
		SearchScreen.searchAlertDialogDone().perform(click());
		String startDate = getFormattedDate(maxSelectableDate, dateFormatterEEEMMMd);
		String endDate = getFormattedDate(maxSelectableDate.plusDays(1), dateFormatterEEEMMMd);
		SearchScreen.selectDateButton().check(matches(withText(startDate + "  -  " + endDate + " (1 night)")));
	}

}
