package com.expedia.bookings.test.stepdefs.phone.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.phone.newflights.FlightsScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import org.joda.time.LocalDate;

import java.util.Map;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;


public class SearchScreenSteps {

	Map<String, String> parameters;
	int totalTravelers;

	@When("^I enter source and destination for flights$")
	public void enterSourceAndDestination(Map<String, String> parameters) throws Throwable {
		SearchScreen.origin().perform(click());
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(parameters.get("source")));
		SearchScreen.selectLocation(parameters.get("source_suggest"));
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(parameters.get("destination")));
		SearchScreen.selectLocation(parameters.get("destination_suggest"));
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

	@When("^I add \"(.*?)\" to the query in flights search box$")
	public void addLettersToQuery(String q) throws Throwable {
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(q));
	}

	@When("^I select \"(.*?)\" from suggestions$")
	public void selectSuggestion(String suggestion) throws Throwable {
		SearchScreen.selectLocation(suggestion);
	}

	@And("^I pick dates for flights$")
	public void pickDates(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		LocalDate endDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("end_date")));
		SearchScreen.selectDates(stDate, endDate);
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

	@And("^I pick departure date for flights$")
	public void selectDepartureDate(Map<String, String> parameters) throws Throwable {
		LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
		SearchScreen.selectDates(stDate, null);
	}

	@Then("^I can trigger flights search$")
	public void searchClick() throws Throwable {
		SearchScreen.searchButton().perform(click());
	}

	@Then("^flights suggest typeAhead is not fired$")
	public void verifySuggestionListEmpty() throws Throwable {
		SearchScreen.suggestionList().check(matches(not(hasDescendant(withId(R.id.suggestion_text_container)))));
	}

	@Then("^flights suggest typeAhead is fired for \"(.*?)\"$")
	public void verifySuggestionsForGivenQuery(String query) throws Throwable {
		SearchScreen.searchEditText().check(matches(withText(query)));
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
		SearchScreen.waitForSuggestions(hasDescendant(withId(R.id.suggestion_text_container)));
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
		this.parameters = parameters;
		enterSourceAndDestination(parameters);
		pickDates(parameters);
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
		SearchScreen.searchButton().perform(click());
	}

	@Then("^on FSR the destination is \"(.*?)\"$")
	public void verifyDestination(String destination) throws Throwable {
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Select flight to"))))
			.check(matches(withText(containsString(destination))));
	}

	@And("^on FSR the date is as user selected$")
	public void verifyDate() throws Throwable {
		LocalDate startDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("start_date")));
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

	@And("^on inbound FSR the number of traveller are as user selected$")
	public void verifyTravelersForInbound() throws Throwable {
		onView(allOf(withParent(withId(R.id.flights_toolbar)), withText(containsString("Traveler"))))
			.check(matches(withText(containsString(totalTravelers + " Traveler"))));
	}

	@Then("^I verify date is as user selected for inbound flight$")
	public void verifyDateForInboundFlight() throws Throwable {
		LocalDate startDate = LocalDate.now().plusDays(Integer.parseInt(parameters.get("end_date")));
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

	@Then("^I select first flight$")
	public void selectFirstFlight() throws Throwable {
		FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0);
		FlightsScreen.selectOutboundFlight().perform(click());
	}

	@And("^on outbound FSR the number of traveller are as user selected$")
	public void verifyTravelersForOutbound() throws Throwable {
		onView(allOf(withParent(withId(R.id.flights_toolbar)), hasSibling(withText("Select return flight")),
			withText(containsString("Traveler"))))
			.check(matches(withText(containsString(totalTravelers + " Traveler"))));
	}

	@Then("^I click on guest button")
	public void clickOnGuestButton() throws Throwable {
		SearchScreen.selectGuestsButton().perform(click());
	}

	@Then("^I increase the adult count to max")
	public void increaseAdultCount() throws Throwable {
		SearchScreen.incrementAdultsButton();
		onView(withId(R.id.adult)).check(matches(withText(containsString("Adults"))));
		for (int i = 1; i < 5; i++) {
			SearchScreen.incrementAdultsButton();
		}
	}

	@And("^I press done")
	public void pressDone() throws Throwable {
		onView(withId(android.R.id.button1)).perform(click());
	}

	@Then("^(\\d+) traveler count is as selected by user")
	public void checkNumberOfTravellers(int number) throws Throwable {
		onView(withId(R.id.traveler_card)).check(matches(withText(containsString(number + " Traveler"))));
	}

	@And("^reduce the travellers count")
	public void reduceNumberOfTraveler() throws Throwable {
		SearchScreen.removeAdultsButton().perform(click());
	}

	@Then("^I increase the child count to max")
	public void increaseChildCount() throws Throwable {
		SearchScreen.incrementChildrenButton();
		onView(withId(R.id.children)).check(matches(withText(containsString("1 Child"))));
		for (int i = 1; i < 4; i++) {
			SearchScreen.incrementChildrenButton();
		}
		onView(withId(R.id.children)).check(matches(withText(containsString("Children"))));
	}

	@And("^equal number of age pickers are shown")
	public void checkChildAgeRepresenter() throws Throwable {
		onView(withId(R.id.child_spinner_1)).check(matches(isDisplayed()));
		onView(withId(R.id.child_spinner_2)).check(matches(isDisplayed()));
		onView(withId(R.id.child_spinner_3)).check(matches(isDisplayed()));
		onView(withId(R.id.child_spinner_4)).check(matches(isDisplayed()));
	}

	@And("^the default age is 10 years")
	public void checkDefaultAge() throws Throwable {
		onView(withParent(withId(R.id.child_spinner_1))).check(matches(withText(containsString("10 years old"))));
		onView(withParent(withId(R.id.child_spinner_2))).check(matches(withText(containsString("10 years old"))));
		onView(withParent(withId(R.id.child_spinner_3))).check(matches(withText(containsString("10 years old"))));
		onView(withParent(withId(R.id.child_spinner_4))).check(matches(withText(containsString("10 years old"))));
	}

	@And("^Reduce the child count")
	public void reduceChildCount() throws Throwable {
		SearchScreen.removeChildButton().perform(click());
	}

	@And("^corresponding age picker is removed")
	public void checkAgeRepresnterVisibility() throws Throwable {
		onView(withId(R.id.child_spinner_4)).check(matches(not(isDisplayed())));
	}

	@And("^Validate that flight time field is displayed: (true|false)$")
	public void checkVisibilityFlightDuration(boolean isDisplayed) throws Throwable {
		Common.delay(5);
		validateFlightSRPListViewCellItemVisibility(R.id.flight_time_detail_text_view, isDisplayed);
	}

	@And("^Validate that price field is displayed: (true|false)$")
	public void checkVisibilityOfPrice(boolean isDisplayed) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(R.id.price_text_view, isDisplayed);
	}

	@And("^Validate that airline name field is displayed: (true|false)$")
	public void checkVisibilityOfAirlineName(boolean isDisplayed) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(R.id.airline_text_view, isDisplayed);
	}

	@And("^Validate that flight duration field is displayed: (true|false)$")
	public void checkVisibilityOfFlightDuration(boolean isDisplayed) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(R.id.flight_duration_text_view, isDisplayed);
	}

	@And("^Validate that round trip header is displayed: (true|false)$")
	public void checkVisibilityOfRoundTripHeader(boolean isDisplayed) throws Throwable {
		validateFlightSRPListViewCellItemVisibility(R.id.trip_type_text_view, isDisplayed);
	}

	private void validateFlightSRPListViewCellItemVisibility(int resId, boolean isDisplayed) {
		onView(withId(R.id.list_view)).check(RecyclerViewAssertions.assertionOnItemAtPosition(1, hasDescendant(
			allOf(withId(resId), (isDisplayed ? isDisplayed() : not(isDisplayed()))))));
	}

	private void checkString(int resID, String text) {
		onView(withId(R.id.list_view)).check(RecyclerViewAssertions.assertionOnItemAtPosition(2, hasDescendant(
			allOf(withId(resID), withText(containsString(text))))));
	}

	@And("^Name of airline is \"(.*?)\"")
	public void checkAirlineName(String airline) throws Throwable {
		checkString(R.id.airline_text_view, airline);
	}

	@And("^Price of the flight is (\\d+)$")
	public void checkPriceOfFlight(int price) throws Throwable {
		checkString(R.id.price_text_view, String.valueOf(price));
	}

	@And("^Duration of the flight is \"(.*?)\"")
	public void checkDuraionOfFlight(String duration) throws Throwable {
		checkString(R.id.flight_duration_text_view, duration);
	}

	@And("^Timing of the flight is \"(.*?)\"")
	public void checkTimingOfTheFlight(String timing) throws Throwable {
		checkString(R.id.flight_time_detail_text_view, timing);
	}

	@And("^Number of stops are (\\d+)")
	public void numberOfStops(int stops) throws Throwable {
		if (stops > 0) {
			checkString(R.id.flight_duration_text_view, (String.valueOf(stops) + " Stop"));
		}
		else {
			checkString(R.id.flight_duration_text_view, "Nonstop");
		}
	}
}
