package com.expedia.bookings.test.stepdefs.phone.lx;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.Common.validateViewVisibility;
import static org.hamcrest.Matchers.allOf;


public class LxDatePickerSteps {

	@And("^I pick dates for lx$")
	public static void pickDates(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		LXScreen.selectDates(stDate, null);
	}

	@And("^I pick dates range for lx$")
	public static void pickDatesRange(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		LocalDate endDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("end_date")));
		LXScreen.selectDates(stDate, endDate);
	}

	@Then("^Validate that Calender widget is displayed for lx: (true|false)")
	public void validateCalenderWidgetVisibility(boolean isDisplayed) throws Throwable {
		validateViewVisibility(R.id.calendar, isDisplayed);
	}

	@Then("^Validate that Current Month calender is displayed for lx$")
	public void validateCurrentMonthYearInCalender() throws Throwable {
		Format dateFormatter = new SimpleDateFormat("MMMM yyyy", Locale.US);
		String todayDateStr = dateFormatter.format(new Date());
		validateMonthYearOfCalender(todayDateStr);
	}

	@Then("^Validate that Previous month arrow is displayed for lx: (true|false)$")
	public void validatePreviousMonthArrowVisibility(boolean isDisplayed) throws Throwable {
		validateViewVisibility(R.id.previous_month, isDisplayed);
	}

	@Then("^Validate that Next month arrow is displayed for lx: (true|false)$")
	public void validateNextMonthArrowVisibility(boolean isDisplayed) throws Throwable {
		validateViewVisibility(R.id.next_month, isDisplayed);
	}

	@Then("^I click on Next month button for lx$")
	public void clickNextMonthButton() throws Throwable {
		LXScreen.nextMonthButton().perform(click());
	}

	@And("^Validate that next month calender is displayed for lx$")
	public void validateNextMonthCalenderTitle() throws Throwable {
		Format dateFormatter = new SimpleDateFormat("MMMM yyyy", Locale.US);
		String nextMonthYearStr = dateFormatter.format(LocalDate.now().plusMonths(1).toDate()).toString();
		validateMonthYearOfCalender(nextMonthYearStr);
	}

	@Then("^I click on Previous month button for lx$")
	public void clickPreviousMonthButton() throws Throwable {
		LXScreen.previousMonthButton().perform(click());
	}

	private void validateMonthYearOfCalender(String monthYearString) throws Throwable {
		onView(allOf(withId(R.id.current_month), withText(monthYearString))).check(matches(isDisplayed()));
	}
}
