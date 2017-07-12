package com.expedia.bookings.test.stepdefs.phone.flights;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import org.joda.time.LocalDate;
import android.support.test.espresso.matcher.ViewMatchers;
import com.expedia.bookings.R;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;


public class DatePickerSteps {

	@And("^I pick dates for flights$")
	public static void pickDates(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		LocalDate endDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("end_date")));
		SearchScreen.selectDates(stDate, endDate);
	}

	@And("^I pick departure date for flights$")
	public void selectDepartureDate(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		SearchScreen.selectDates(stDate, null);
	}

	@Then("^Validate that Previous month arrow is displayed: (true|false)$")
	public void validatePreviousMonthArrowVisibility(boolean isDisplayed) throws Throwable {
		validateViewVisibility(R.id.previous_month, isDisplayed);
	}

	@Then("^Validate that Next month arrow is displayed: (true|false)$")
	public void validateNextMonthArrowVisibility(boolean isDisplayed) throws Throwable {
		validateViewVisibility(R.id.next_month, isDisplayed);
	}

	@Then("^Validate that Calender widget is displayed: (true|false)")
	public void validateCalenderWidgetVisibility(boolean isDisplayed) throws Throwable {
		validateViewVisibility(R.id.parentPanel, isDisplayed);
	}

	@Then("^Validate that Current Month calender is displayed$")
	public void validateCurrentMonthYearInCalender() throws Throwable {
		Format dateFormatter = new SimpleDateFormat("MMMM yyyy", Locale.US);
		String todayDateStr = dateFormatter.format(new Date());
		validateMonthYearOfCalender(todayDateStr);
	}

	@And("^Validate that next month calender is displayed$")
	public void validateNextMonthCalenderTitle() throws Throwable {
		Format dateFormatter = new SimpleDateFormat("MMMM yyyy", Locale.US);
		String nextMonthYearStr = dateFormatter.format(LocalDate.now().plusMonths(1).toDate()).toString();
		validateMonthYearOfCalender(nextMonthYearStr);
	}

	@Then("^I click on Next month button$")
	public void clickNextMonthButton() throws Throwable {
		SearchScreen.nextMonthButton().perform(click());

	}

	@Then("^I click on Previous month button$")
	public void clickPreviousMonthButton() throws Throwable {
		SearchScreen.previousMonthButton().perform(click());

	}

	@And("^Validate the selected date on calender button$")
	public void validateCalenderButtonText(Map<String, String> parameters) throws Throwable {
		Format dateFormatter = new SimpleDateFormat("MMM d", Locale.US);
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		LocalDate endDate = null;
		boolean isRoundTrip = true;

		String stDateStr = dateFormatter.format(stDate.toDate()).toString().trim();

		//Currently, we are using two different versions of hyphen in our code:
		// When user has selected only departure date, Mar 23 – Select return date
		// When user has selected both departure date and return date, Mar 23 - Apr 8
		String endDateStr = " – Select return date";

		if (parameters.get("end_date") != null) {
			endDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("end_date")));
			endDateStr = " - " + dateFormatter.format(endDate.toDate()).toString().trim();
		}
		if (parameters.get("isRoundTrip") != null) {
			isRoundTrip = new Boolean(parameters.get("isRoundTrip"));
		}

		if (isRoundTrip) {
			SearchScreen.calendarCard().check(matches(withText(stDateStr + endDateStr)));
		}
		else {
			SearchScreen.calendarCard().check(matches(withText(stDateStr + " (One Way)")));
		}
	}

	@Then("^Validate that \"(.*?)\" text below calender title is displayed$")
	public void validateCalenderSubtitle(String text) throws Throwable {
		SearchScreen.calendarSubtitle().check(matches(allOf(withText(text), isDisplayed())));
	}

	@And("^I choose departure date for flights-roundtrip and validate the tool tip")
	public void selectRoundTripDepartureDateAndValidate(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		Format dateFormatter = new SimpleDateFormat("MMM d", Locale.US);
		String stDateStr = dateFormatter.format(stDate.toDate()).toString();

		//choose departure date
		SearchScreen.chooseDates(stDate, null);

		//validate calender tooltip and subtilte
		SearchScreen.validateDatesToolTip(stDateStr, "Next: Select return date");
		validateCalenderSubtitle(stDateStr + " – Select return date");
	}

	@And("^I choose return date for flights-roundtrip and validate the tool tip")
	public void selectRoundTripReturnDateAndValidate(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		LocalDate endDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("end_date")));
		Format dateFormatter = new SimpleDateFormat("MMM d", Locale.US);
		String stDateStr = dateFormatter.format(stDate.toDate()).toString();
		String endDateStr = dateFormatter.format(endDate.toDate()).toString();

		//choose return date
		SearchScreen.chooseDates(stDate, endDate);

		//validate calender tooltip and subtitle
		SearchScreen.validateDatesToolTip(stDateStr + " - " + endDateStr, "Drag to modify");
		validateCalenderSubtitle(stDateStr + " - " + endDateStr);
	}

	@And("^I choose departure date for flights-oneway and validate the tool tip")
	public void selectOneWayDepartureDateAndValidate(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		Format dateFormatter = new SimpleDateFormat("MMM d", Locale.US);
		String stDateStr = dateFormatter.format(stDate.toDate()).toString();

		//choose departure date
		SearchScreen.chooseDates(stDate, null);

		//validate calender tooltip and subtitle
		SearchScreen.validateDatesToolTip(stDateStr, "Drag to modify");
		validateCalenderSubtitle(stDateStr + " (One Way)");

	}


	private void validateMonthYearOfCalender(String monthYearString) throws Throwable {
		onView(allOf(withId(R.id.current_month), withText(monthYearString))).check(matches(isDisplayed()));
	}

	private void validateViewVisibility(int resId, boolean isDisplayed) {
		ViewMatchers.Visibility visibility = ViewMatchers.Visibility.INVISIBLE;
		if (isDisplayed) {
			visibility = ViewMatchers.Visibility.VISIBLE;
		}
		onView(withId(resId)).check(matches(withEffectiveVisibility(visibility)));
	}

}
