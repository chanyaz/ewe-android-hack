package com.expedia.bookings.test.stepdefs.phone.bundleDeals;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.stepdefs.phone.TestUtil;
import com.expedia.bookings.test.stepdefs.phone.model.ApiRequestData;
import com.expedia.bookings.test.stepdefs.phone.utils.StepDefUtils;

import junit.framework.Assert;

import cucumber.api.java.en.When;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;

import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;


import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

import static android.support.test.espresso.matcher.ViewMatchers.isFocusable;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;

import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static com.expedia.bookings.test.espresso.ViewActions.getString;


import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

import static com.expedia.bookings.test.stepdefs.phone.TestUtil.getDateInMMMdd;
import static com.expedia.bookings.test.stepdefs.phone.TestUtil.validateRequestParams;
import static org.hamcrest.CoreMatchers.not;

import static com.expedia.bookings.test.stepdefs.phone.flights.DatePickerSteps.pickDates;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;


public class SearchScreenSteps {

	private ApiRequestData apiRequestData;

	@And("^I want to intercept these calls for packages$")
	public void interceptApiCalls1(List<String> apiCallsAliases) throws Throwable {
		StepDefUtils.interceptApiCalls(apiCallsAliases, new Function1<ApiRequestData, Unit>() {
			@Override
			public Unit invoke(ApiRequestData apiRequestData) {
				SearchScreenSteps.this.apiRequestData = apiRequestData;
				return null;
			}
		}, null);
	}

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
	@And("^I make a packages search with following parameters$")
	public void packagesSearchCall(Map<String, String> parameters) throws Throwable {
		TestUtil.dataSet = parameters;
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(parameters.get("source")));
		SearchScreen.selectLocation(parameters.get("source_suggest"));
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(parameters.get("destination")));
		SearchScreen.selectLocation(parameters.get("destination_suggest"));
		pickDates(parameters);
		selectTravelers(parameters);
		SearchScreen.searchButton().perform(click());
	}
	@Then("^validate hotels loading on package overview screen$")
	public void validateHotelsLoading() throws Throwable {
		onView(allOf(isDescendantOfA(withId(R.id.package_bundle_hotel_widget)), withId(R.id.hotels_card_view_text)))
			.check(matches(withText(containsString("Select hotel in"))));
	}
	@Then("^validate outbound flights loading on package overview screen$")
	public void validateOutboundFlightsLoading() throws Throwable {
		onView(allOf(isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)),
			withId(R.id.flight_card_view_text)))
			.check(matches(allOf(withText(containsString("Select flight to")),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
	}
	@Then("^validate inbound flights loading on package overview screen$")
	public void validateInboundFlightsLoading() throws Throwable {
		onView(allOf(isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)),
			withId(R.id.flight_card_view_text)))
			.check(matches(allOf(withText(containsString("Select flight to")),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
	}
	@Then("^validate HSR screen is displayed with following travel dates and travelers$")
	public void validateTravelDetailsHsr(Map<String, String> parameters) throws Throwable {
		validateHotelTravelDatesHSR(getDate(parameters.get("start_date")), getDate(parameters.get("end_date")));
		validateHotelTravelersHSR(parameters.get("Total_Travelers"));
	}
	@Then("^validate (outbound|inbound) FSR screen is displayed with following travel date and travelers$")
	public void validateTravelDetailsFsr(String ignore, Map<String, String> parameters) throws Throwable {
		validateTravelDatesFSR(getDateOnFSR(parameters.get("travel_date")));
		validateTravelersCountFSR(parameters.get("Total_Travelers"));
	}
	@Then("^I select hotel at position (\\d+) on HSR screen$")
	public void selectHotel(int position) throws Throwable {
		HotelScreen.hotelResultsList().perform(waitForViewToDisplay(), RecyclerViewActions.actionOnItemAtPosition(position + 1, click()));
	}
	@Then("^I store the hotel name in \"(.*?)\"$")
	public void saveHotel(String key) throws Throwable {
		onView(withId(R.id.hotel_details_toolbar)).perform(waitForViewToDisplay());
		TestUtil.storeDataAtRuntime.put(key, getHotelName());
	}
	@Then("^I select first room$")
	public void selectRoom() throws Throwable {
		PackageScreen.selectFirstRoom();
	}
	@Then("^I select (outbound?|inbound) flight to (destination|source) at position (\\d+)$")
	public void clickFlights(String ignore1, String ignore2, int pos) throws Throwable {
		PackageScreen.flightList().perform(RecyclerViewActions.actionOnItemAtPosition(pos, click()));
		PackageScreen.selectThisFlight().perform(waitForViewToDisplay());
		PackageScreen.selectThisFlight().perform(click());
	}
	@Then("^validate \"(.*?)\" is same as user selected on package overview screen$")
	public void matchHotelName(String key) throws Throwable {
		validateHotelName(TestUtil.storeDataAtRuntime.get(key));
	}

	@Then("^validate hotel widget of overview screen with following details$")
	public void validateHotelWidget(Map<String, String> parameters) throws Throwable {
		validateTravelersCountHotelWidget(parameters.get("Total_Travelers"));
		validateTravelDatesHotelWidget(getDate(parameters.get("start_date")), getDate(parameters.get("end_date")));
	}
	@Then("^validate flight outbound widget of overview screen with following details$")
	public void validateOutboundFlightWidget(Map<String, String> parameters) throws Throwable {
		validateOutboundFlightLocation(parameters.get("destination"));
		validateTravelDatesOutboundFlightWidget(getDate(parameters.get("travel_date")));
		validateTravelersCountFlightOutboundWidget(parameters.get("Total_Travelers"));
	}
	@Then("^validate flight inbound widget of overview screen with following details$")
	public void validateInboundFlightWidget(Map<String, String> parameters) throws Throwable {
		validateInboundFlightLocation(parameters.get("source"));
		validateTravelDatesInboundFlightWidget(getDate(parameters.get("travel_date")));
		validateTravelersCountFlightInboundWidget(parameters.get("Total_Travelers"));
	}

	@Then("^I validate that checkout screen is displayed$")
	public void validateCheckoutScreenVisible() throws Throwable {
		onView(withId(R.id.login_widget)).perform(waitForViewToDisplay()).check(matches(isDisplayed()));
	}

	@Then("^I type \"(.*?)\" and select the location \"(.*?)\"$")
	public void validateTypeAheadCallTrigerred(String query, String location) throws Throwable {
		SearchScreen.searchEditText().perform(waitForViewToDisplay(), typeText(query));
		SearchScreen.selectLocation(location);
		//Assert.assertNotNull(apiRequestData);
	}

	@Then("^Validate that no typeahead call is trigerred for packages$")
	public void validateNoTypeAheadCallTrigerred() throws Throwable {
		Common.delay(5);
		Assert.assertEquals(null, apiRequestData);
	}

	@Then("^Validate the \"(.*?)\" API request query params for following parameters for packages")
	public void validateRequestQueryData(String type, Map<String, String> expParameters) throws Throwable {
		validateRequestParams(expParameters, apiRequestData);
	}

	@Then("^Validate the getPackages API request query data for following parameters for packages")
	public void validateGetPackageRequest(Map<String, String> expParameters) throws Throwable {
		validateRequestParams(expParameters, apiRequestData);
	}

	private void selectTravelers(Map<String, String> parameters) {
		int adult = Integer.parseInt(parameters.get("adults"));
		int child = Integer.parseInt(parameters.get("child"));
		SearchScreen.selectGuestsButton().perform(click());
		for (int i = 1; i < adult; i++) {
			SearchScreen.incrementAdultsButton();
		}
		for (int i = 0; i < child; i++) {
			SearchScreen.incrementChildrenButton();
		}
		SearchScreen.searchAlertDialogDone().perform(click());
	}
	private String getDate(String travelDate) {
		LocalDate startDate = LocalDate.now().plusDays(Integer.parseInt(travelDate));
		String date = String.valueOf(startDate.getDayOfMonth());
		String month = com.expedia.bookings.test.stepdefs.phone.flights.SearchScreenSteps.getMonth(startDate.getMonthOfYear());
		String monthDate = month + " " + date;
		return monthDate;
	}
	private String getDateOnFSR(String travelDate) {
		LocalDate startDate = LocalDate.now().plusDays(Integer.parseInt(travelDate));
		Format dateFormatter = new SimpleDateFormat("MMM dd", Locale.US);
		String monthDate = dateFormatter.format(startDate.toDate()).toString();
		return monthDate;
	}
	private void validateHotelTravelDatesHSR(String startDate, String endDate) {
		onView(Matchers.allOf(isDescendantOfA(withId(R.id.hotel_results_toolbar)), withText(Matchers.containsString("Guest"))))
			.check(matches(withText(Matchers.containsString(startDate + " - " + endDate))));
	}
	private void validateTravelDatesHotelWidget(String startDate, String endDate) {
		onView(Matchers.allOf(isDescendantOfA(withId(R.id.package_bundle_hotel_widget)), withText(Matchers.containsString("Guest"))))
			.check(matches(withText(Matchers.containsString(startDate + " - " + endDate))));
	}
	private void validateHotelTravelersHSR(String travelers) {
		onView(Matchers.allOf(isDescendantOfA(withId(R.id.hotel_results_toolbar)), withText(Matchers.containsString("Guest"))))
			.check(matches(withText(Matchers.containsString(travelers + " Guest"))));
	}
	private void validateTravelersCountHotelWidget(String travelers) {
		onView(Matchers.allOf(isDescendantOfA(withId(R.id.package_bundle_hotel_widget)), withText(Matchers.containsString("Guest"))))
			.check(matches(withText(Matchers.containsString(travelers + " Guest"))));
	}
	private void validateTravelDatesFSR(String travelDate) {
		onView(Matchers.allOf(isDescendantOfA(withId(R.id.flights_toolbar)), withText(Matchers.containsString("Traveler"))))
			.check(matches(withText(Matchers.containsString(travelDate))));
	}
	private void validateTravelDatesOutboundFlightWidget(String travelDate) {
		onView(Matchers.allOf(isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)), withText(Matchers.containsString("Traveler"))))
			.check(matches(withText(Matchers.containsString(travelDate))));
	}
	private void validateTravelDatesInboundFlightWidget(String travelDate) {
		onView(Matchers.allOf(isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)), withText(Matchers.containsString("Traveler"))))
			.check(matches(withText(Matchers.containsString(travelDate))));
	}
	private void validateTravelersCountFSR(String travelers) {
		onView(Matchers.allOf(isDescendantOfA(withId(R.id.flights_toolbar)), withText(Matchers.containsString("Travelers"))))
			.check(matches(withText(Matchers.containsString(travelers + " Travelers"))));
	}
	private void validateTravelersCountFlightOutboundWidget(String travelers) {
		onView(Matchers.allOf(isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)), withText(Matchers.containsString("Travelers"))))
			.check(matches(withText(Matchers.containsString(travelers + " Travelers"))));
	}
	private void validateTravelersCountFlightInboundWidget(String travelers) {
		onView(Matchers.allOf(isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)), withText(Matchers.containsString("Travelers"))))
			.check(matches(withText(Matchers.containsString(travelers + " Travelers"))));
	}
	private void validateOutboundFlightLocation(String location) {
		onView(Matchers.allOf(isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)),
			withId(R.id.flight_card_view_text)))
			.check(matches(allOf(withText(containsString("Flight to " + "(" + location + ")")),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
	}
	private void validateInboundFlightLocation(String location) {
		onView(Matchers.allOf(isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)),
			withId(R.id.flight_card_view_text)))
			.check(matches(allOf(withText(containsString("Flight to " + "(" + location + ")")),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
	}
	private String getHotelName() {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(allOf(isDescendantOfA(withId(R.id.hotel_details_toolbar)), withId(R.id.hotel_name_text))).perform(getString(value));
		String hotel = value.get();
		return hotel;
	}
	private void validateHotelName(String name) {
		onView(allOf(withParent(withParent(withParent(withParent(withId(R.id.package_bundle_hotel_widget))))),
			withId(R.id.hotels_card_view_text)))
			.check(matches(allOf(withText(containsString(name)))));
	}

	@Then("^Validate the getPackages API request form data for following parameters")
	public void validateGetPackagesRequestFormData(Map<String, String> expParameters) throws Throwable {

		HashMap<String, String> modifiableExpParameters = new HashMap<>();
		modifiableExpParameters.putAll(expParameters);
		Format dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		if (modifiableExpParameters.get("fromDate") != null ) {
			LocalDate stDate = LocalDate.now().plusDays(Integer.parseInt(expParameters.get("fromDate")));
			modifiableExpParameters.put("fromDate", dateFormatter.format(stDate.toDate()).toString());
		}
		if (modifiableExpParameters.get("toDate") != null ) {
			LocalDate returnDate = LocalDate.now().plusDays(Integer.parseInt(expParameters.get("toDate")));
			modifiableExpParameters.put("toDate", dateFormatter.format(returnDate.toDate()).toString());
		}
		for (Map.Entry<String, String> entry : modifiableExpParameters.entrySet()) {
			Assert.assertEquals(entry.getValue() , apiRequestData.getFormData().get(entry.getKey()));
		}
	}

	@Then("^Validate that Package Overview screen is displayed")
	public void isDisplayedPackageOverviewScreen() throws Throwable {
		onView(allOf(withId(R.id.step_one_text), isDescendantOfA(withId(R.id.bundle_widget)))).perform(waitForViewToDisplay()).check(matches(withText("Step 1: Select Hotel")));
	}

	@Then("^Validate search form retains details of search for packages")
	public void validateSearchFormDetails(Map<String, String> expParameters) throws Throwable {

		String startDate = getDateInMMMdd(expParameters.get("start_date"));
		String endDate = getDateInMMMdd(expParameters.get("end_date"));
		String expectedCalendarDate = startDate + " - " + endDate + " " + expParameters.get("numberOfNights");

		SearchScreen.origin().check(matches(withText(expParameters.get("source"))));
		SearchScreen.destination().check(matches(withText(expParameters.get("destination"))));
		SearchScreen.calendarCard().check(matches(withText(expectedCalendarDate)));
		SearchScreen.selectTravelerText().check(matches(withText(expParameters.get("totalTravelers"))));
	}

	@Then("^Validate search form default state for packages")
	public void validateSearchFormDefaultState(Map<String, String> expParameters) throws Throwable {
		SearchScreen.origin().check(matches(withText(expParameters.get("source"))));
		SearchScreen.destination().check(matches(withText(expParameters.get("destination"))));
		SearchScreen.calendarCard().check(matches(withText(expParameters.get("calendar"))));
		SearchScreen.selectTravelerText().check(matches(withText(expParameters.get("totalTravelers"))));
	}

	@Then("^Validate Search button is disabled")
	public void searchButtonDisabled() throws Throwable {
		SearchScreen.searchButton().check(matches(not(isFocusable())));
	}

	@Then("^Validate toolbar title is \"(.*?)\" for packages")
	public void validate(String title) throws Throwable {
		onView(withId(R.id.title)).check(matches(withText(title)));
	}

}
