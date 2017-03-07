package com.expedia.bookings.test.stepdefs.phone.flights;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.joda.time.LocalDate;
import android.support.test.espresso.matcher.RootMatchers;
import com.expedia.bookings.R;
import com.expedia.bookings.test.BuildConfig;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.phone.newflights.FlightsScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.airportDropDownEntryWithAirportCode;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static com.expedia.bookings.test.stepdefs.phone.flights.DatePickerSteps.pickDates;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;


public class SearchScreenSteps {

	int totalTravelers;

	@When("^I enter source and destination for flights$")
	public void enterSourceAndDestination(Map<String, String> parameters) throws Throwable {
		if (BuildConfig.FLAVOR.equalsIgnoreCase("airasiago")) {
			selectSourceFromDropDown("DMK");
			selectDestinationFromDropdown("HKG");

		}
		else {
			SearchScreen.origin().perform(click());
			SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(parameters.get("source")));
			SearchScreen.selectLocation(parameters.get("source_suggest"));
			SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(parameters.get("destination")));
			SearchScreen.selectLocation(parameters.get("destination_suggest"));
		}
	}

	@When("^I type \"(.*?)\" in the flights search box$")
	public void typeInOriginSearchBox(String query) throws Throwable {
		SearchScreen.origin().perform(click());
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(query));
	}

	@When("^I type \"(.*?)\" in the flights destination search box$")
	public void typeInDestinationSearchBox(String query) throws Throwable {
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(query));
	}

	@When("^I select source location from the dropdown as \"(.*?)\"$")
	public void selectSourceFromDropDown(String location) throws Throwable {
		SearchScreen.origin().perform(click());
		onData(airportDropDownEntryWithAirportCode(location))
			.inRoot(RootMatchers.isPlatformPopup()).perform(click());
	}

	@And("^I select destination from the dropdown as \"(.*?)\"$")
	public void selectDestinationFromDropdown(String location) throws Throwable {
		Common.delay(1);
		SearchScreen.destination().perform(click());
		onData(airportDropDownEntryWithAirportCode(location))
			.inRoot(RootMatchers.isPlatformPopup()).perform(click());
	}

	@When("^I add \"(.*?)\" to the query in flights search box$")
	public void addLettersToQuery(String q) throws Throwable {
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(q));
	}

	@When("^I select \"(.*?)\" from suggestions$")
	public void selectSuggestion(String suggestion) throws Throwable {
		SearchScreen.selectLocation(suggestion);
	}


	@And("^I change travellers count and press done$")
	public void changeTravellersCount() throws Throwable {
		SearchScreen.selectGuestsButton().perform(click());
		SearchScreen.searchAlertDialogDone().perform(waitForViewToDisplay());
		SearchScreen.incrementAdultsButton();
		SearchScreen.incrementAdultsButton();
		SearchScreen.incrementChildrenButton();
		SearchScreen.searchAlertDialogDone().perform(click());
	}

	@And("^I select one way trip$")
	public void selectOneWayTrip() throws Throwable {
		FlightsScreen.selectOneWay();
	}

	@Then("^I can trigger flights search$")
	public void searchClick() throws Throwable {
		SearchScreen.searchButton().perform(click());
	}

	@Then("^departure field exists for flights search form$")
	public void checkDepartureField() throws Throwable {
		SearchScreen.origin().check(matches(isDisplayed()));
		SearchScreen.origin().check(matches(withText(containsString("Flying from"))));
	}

	@And("arrival field exists for flights search form$")
	public void checkArrivalField() throws Throwable {
		SearchScreen.destination().check(matches(isDisplayed()));
		SearchScreen.destination().check(matches(withText(containsString("Flying to"))));
	}

	@Then("^calendar field exists for flights search form$")
	public void checkCalendarField() throws Throwable {
		onView(withId(R.id.calendar_card)).check(matches(isDisplayed()));
		onView(withId(R.id.calendar_card)).check(matches(withText(containsString("Select Dates"))));
	}


	@Then("^calendar field exists for one way flights search form$")
	public void checkCalendarFieldOneWay() throws Throwable {
		onView(withId(R.id.calendar_card)).check(matches(isDisplayed()));
		onView(withId(R.id.calendar_card)).check(matches(withText(containsString("Select departure date"))));
	}

	@And("^I make a flight search with following parameters$")
	public void flightSearchCall(Map<String, String> parameters) throws Throwable {
		DataUtil.dataSet = parameters;
		enterSourceAndDestination(parameters);
		pickDates(parameters);
		selectTravelers(parameters);
		SearchScreen.searchButton().perform(click());
	}

	@And("^search criteria is retained on the search form$")
	public void checkFormDetails() throws Throwable {
		onView(withId(R.id.origin_card)).check(matches(withText(containsString(DataUtil.dataSet.get("source")))));
		onView(withId(R.id.destination_card)).check(matches(withText(containsString(DataUtil.dataSet.get("destination")))));
		int totalNumberOfTravelers = Integer.parseInt(DataUtil.dataSet.get("adults")) + Integer.parseInt(DataUtil.dataSet.get("child"));
		onView(withId(R.id.traveler_card)).check(matches(withText(containsString(String.valueOf(totalNumberOfTravelers) + " Traveler"))));
	}

	@And("^I trigger flight search again with following parameters$")
	public void remakeFlightSearch(Map<String, String> parameters) throws Throwable {
		DataUtil.dataSet = parameters;
		SearchScreen.origin().perform(click());
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(parameters.get("source")));
		SearchScreen.selectLocation(parameters.get("source_suggest"));
		SearchScreen.destination().perform(click());
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(parameters.get("destination")));
		SearchScreen.selectLocation(parameters.get("destination_suggest"));
		SearchScreen.calendarCard().perform(click());
		pickDates(parameters);
		SearchScreen.selectGuestsButton().perform(click());
		int previousNumberOfAdults = Integer.parseInt(FlightsSearchScreen.getAdultTravelerNumberText().split(" ")[0]);
		int previousNumberOfChildren = Integer.parseInt(FlightsSearchScreen.getChildTravelerNumberText().split(" ")[0]);
		changeNumberOfAdults(previousNumberOfAdults);
		changeNumberOfChildren(previousNumberOfChildren);
		SearchScreen.searchAlertDialogDone().perform(click());
		SearchScreen.searchButton().perform(click());
		onView(withId(R.id.sort_filter_button)).perform(waitFor(isDisplayed(), 20, TimeUnit.SECONDS));
	}

	private void selectTravelers(Map<String, String> parameters) {
		int adult = Integer.parseInt(parameters.get("adults"));
		int child = Integer.parseInt(parameters.get("child"));
		this.totalTravelers = adult + child;
		SearchScreen.selectGuestsButton().perform(click());

		for (int i = 1; i < adult; i++) {
			SearchScreen.incrementAdultsButton();
		}
		for (int i = 0; i < child; i++) {
			SearchScreen.incrementChildrenButton();
		}
		SearchScreen.searchAlertDialogDone().perform(click());
	}

	private void changeNumberOfAdults(int previousNumberOfAdults) {
		while (previousNumberOfAdults < Integer.parseInt(DataUtil.dataSet.get("adults"))) {
			SearchScreen.incrementAdultsButton();
			previousNumberOfAdults++;
		}
		while (previousNumberOfAdults > Integer.parseInt(DataUtil.dataSet.get("adults"))) {
			SearchScreen.removeAdultsButton().perform(click());
			previousNumberOfAdults--;
		}
	}


	private void changeNumberOfChildren(int previousNumberOfChildren) {
		int adult = Integer.parseInt(DataUtil.dataSet.get("adults"));
		int child = Integer.parseInt(DataUtil.dataSet.get("child"));
		this.totalTravelers = adult + child;
		while (previousNumberOfChildren < Integer.parseInt(DataUtil.dataSet.get("child"))) {
			SearchScreen.incrementChildrenButton();
			previousNumberOfChildren++;
		}
		while (previousNumberOfChildren > Integer.parseInt(DataUtil.dataSet.get("child"))) {
			SearchScreen.removeChildButton().perform(click());
			previousNumberOfChildren--;
		}
	}

	@And("^on FSR the date is as user selected$")
	public void verifyDate() throws Throwable {
		LocalDate startDate = LocalDate.now().plusDays(Integer.parseInt(DataUtil.dataSet.get("start_date")));
		String date = String.valueOf(startDate.getDayOfMonth());
		String year = String.valueOf(startDate.getYear());
		String month = getMonth(startDate.getMonthOfYear());
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
			.check(matches(withText(containsString(date))));
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
			.check(matches(withText(containsString(month))));
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
			.check(matches(withText(containsString(year))));
	}

	public static String getMonth(int month) {
		switch (month) {
		case 1:
			return "Jan";
		case 2:
			return "Feb";
		case 3:
			return "Mar";
		case 4:
			return "Apr";
		case 5:
			return "May";
		case 6:
			return "Jun";
		case 7:
			return "Jul";
		case 8:
			return "Aug";
		case 9:
			return "Sep";
		case 10:
			return "Oct";
		case 11:
			return "Nov";
		default:
			return "Dec";
		}
	}

	@And("^on outbound FSR the number of traveller are as user selected$")
	public void verifyTravelersForInbound() throws Throwable {
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
			.check(matches(withText(containsString(totalTravelers + " Traveler"))));
	}

	@Then("^I verify date is as user selected for inbound flight$")
	public void verifyDateForInboundFlight() throws Throwable {
		LocalDate startDate = LocalDate.now().plusDays(Integer.parseInt(DataUtil.dataSet.get("end_date")));
		String date = String.valueOf(startDate.getDayOfMonth());
		String year = String.valueOf(startDate.getYear());
		String month = getMonth(startDate.getMonthOfYear());
		onView(allOf(withParent(withId(R.id.flights_toolbar)), hasSibling(withText("Select return flight")),
			withText(containsString("Traveler")))).check(matches(withText(containsString(date))));
		onView(allOf(withParent(withId(R.id.flights_toolbar)), hasSibling(withText("Select return flight")),
			withText(containsString("Traveler")))).check(matches(withText(containsString(month))));
		onView(allOf(withParent(withId(R.id.flights_toolbar)), hasSibling(withText("Select return flight")),
			withText(containsString("Traveler")))).check(matches(withText(containsString(year))));
	}

	@And("^on inbound FSR the number of traveller are as user selected$")
	public void verifyTravelersForOutbound() throws Throwable {
		onView(allOf(withParent(withId(R.id.flights_toolbar)), hasSibling(withText("Select return flight")),
			withText(containsString("Traveler"))))
			.check(matches(withText(containsString(totalTravelers + " Traveler"))));
	}

	@Then("^(\\d+) traveler count is as selected by user$")
	public void checkNumberOfTravellers(int number) throws Throwable {
		onView(withId(R.id.traveler_card)).check(matches(withText(containsString(number + " Traveler"))));
	}

	@Then("^Validate that Done button is disabled$")
	public void validateDoneButtonDisabled() throws Throwable {
		onView(withId(R.id.parentPanel)).check(matches(hasDescendant(allOf(withId(android.R.id.button1),
			withText("DONE"), not(isEnabled())))));
	}

	@Then("^Validate that Done button is enabled")
	public void validateDoneButtonEnabled() throws Throwable {
		onView(withId(R.id.parentPanel)).check(matches(hasDescendant(allOf(withId(android.R.id.button1),
			withText("DONE"), isEnabled()))));
	}

	@Given("^I Click on Select Dates button for flights$")
	public void clickSelectDatedButton() throws Throwable {
		SearchScreen.selectDateButton().perform(click());
	}

	@And("^I Click on Done button$")
	public void clickDoneButton() throws Throwable {
		SearchScreen.searchAlertDialogDone().perform(click());
	}
}
