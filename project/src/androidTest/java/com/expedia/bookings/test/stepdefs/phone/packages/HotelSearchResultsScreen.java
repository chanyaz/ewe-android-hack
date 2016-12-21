package com.expedia.bookings.test.stepdefs.phone.packages;

import java.util.concurrent.atomic.AtomicReference;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.newflights.FlightsScreen;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.getListChildCount;
import static com.expedia.bookings.test.espresso.ViewActions.getChildViewTextGeneric;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by pchaudhari on 10/11/16.
 */

public class HotelSearchResultsScreen {

	@Then("I click on guests' button")
	public void selectGuestButton() throws Throwable {
		SearchScreen.selectGuestsButton().perform(click());
	}

	@Then("^I increment child count")
	public void incrementChildCount() throws Throwable {
		SearchScreen.incrementChildrenButton();
	}


	@Then("^I select child age")
	public void selectChildAge() throws Throwable {
		SearchScreen.childAgeDropDown(1).perform(click());
		onData(anything())
			.inRoot(withDecorView(not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))))
			.atPosition(1).perform(click());
		SearchScreen.searchAlertDialogDone().perform(click());
	}

	@Then("^I click on filter button on package hotels SRP")
	public void clickOnFilterButton() throws Throwable {
		HotelScreen.clickTopCornerSortFilter();
	}


	@Then("^I search for the renovation notice")
	public void searchForRenovationNotice() throws Throwable {
		HotelScreen.clickRenoInfo();
		onView(withId(R.id.content_description)).perform(ViewActions.waitForViewToDisplay());
	}

	@Then("^I select hotel with name \"(.*?)\"$")
	public void selectingHotel(String name) throws Throwable {
		HotelScreen.selectHotel(name);
	}

	@Then("^I select room in package hotel details page")
	public void selectRoomOfHotel() throws Throwable {
		PackageScreen.hotelDetailsToolbar().perform(ViewActions.waitForViewToDisplay());
		Common.delay(1);
		PackageScreen.selectRoom();
	}

	@Then("^I click on the top filter button on flights results screen")
	public void clickOnTopFilterButton() throws Throwable {
		FlightsScreen.clickTopCornerFilterButton();
	}

	@Then("^I wait for flight results to appear in packages")
	public void waitForFlightResultsToAppear() throws Throwable {
		PackageScreen.flightList().perform(waitForViewToDisplay());
	}

	@Then("^I check if filter by stops works in packages flight filter")
	public void checkOneStopFlights() throws Throwable {
		int number = getListChildCount(PackageScreen.stopsContainer());
		for (int i = 0; i < number; i++) {
			final AtomicReference<String> value = new AtomicReference<String>();
			onView(withId(R.id.stops_container)).perform(getChildViewTextGeneric(i, value, R.id.label));
			String numberOfStops = value.get();

			PackageScreen.tickCheckboxWithText(numberOfStops);
			onView(allOf(withId(R.id.dynamic_feedback_counter))).check(matches(withText(containsString("Result"))));
			PackageScreen.tickCheckboxWithText(numberOfStops);
		}
	}

	@Then("^I decrease the flight duration to (\\d+) hr in packages flight filter")
	public void checkSliders(final int time) throws Throwable {
		onView(withId(R.id.duration_seek_bar)).perform(ViewActions.setCustomSeekBarTo(time));
		onView(allOf(withId(R.id.duration), withText(time + "hr"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.dynamic_feedback_counter))).check(matches(withText(containsString("Result"))));
	}

	@Then("^I set departure time from (\\d+) to (\\d+) in packages flight filter")
	public void changeDepartureTimes(int from, int to) throws Throwable {
		onView(withId(R.id.departure_range_bar)).perform(ViewActions.setCustomRangeSeekBarTo(from, to));
		String arrivaltime = "am";
		if (from > 12) {
			from = from - 12;
			arrivaltime = "pm";
		}
		onView(allOf(withId(R.id.departure_range_min_text), withText(from + ":00 " + arrivaltime)))
			.check(matches(isDisplayed()));
		String departureTime = "am";
		if (to > 12) {
			to = to - 12;
			departureTime = "pm";
		}
		onView(allOf(withId(R.id.departure_range_max_text), withText(to + ":00 " + departureTime)))
			.check(matches(isDisplayed()));
		onView(allOf(withId(R.id.dynamic_feedback_counter))).check(matches(withText(containsString("Result"))));

	}

	@Then("^I set arrival time from (\\d+) to (\\d+) in packages flight filter")
	public void changeArrivalTimes(int from, int to) throws Throwable {
		onView(withId(R.id.arrival_range_bar)).perform(scrollTo(), ViewActions.setCustomRangeSeekBarTo(from, to));
		String arrivaltime = "am";
		if (from > 12) {
			from = from - 12;
			arrivaltime = "pm";
		}
		onView(allOf(withId(R.id.arrival_range_min_text), withText(from + ":00 " + arrivaltime)))
			.check(matches(isDisplayed()));
		String departureTime = "am";
		if (to > 12) {
			to = to - 12;
			departureTime = "pm";
		}
		onView(allOf(withId(R.id.arrival_range_max_text), withText(to + ":00 " + departureTime)))
			.check(matches(isDisplayed()));

		onView(allOf(withId(R.id.dynamic_feedback_counter))).check(matches(withText(containsString("Result"))));

	}

	@Then("^I click sort to get results in packages flight filter")
	public void applyFilters() throws Throwable {
		onView(withId(R.id.search_btn)).perform(click());
	}


	@Then("^I select the (\\d+) flight from package flights to go to details page")
	public void selectFirstFlightToGoToDetailsPage(final int number) throws Throwable {
		PackageScreen.selectFlight(number);
		onView(allOf(withId(R.id.select_flight_button), withText("Select this Flight")))
			.perform(waitForViewToDisplay());
	}

	@Then("^I check if the price here is given per person")
	public void checkPriceIsPerPerson() throws Throwable {
		onView(allOf(withId(R.id.bundle_price))).check(matches(withText(containsString("/person"))));
	}

	@Then("^I select this flight and wait for inbound flights results to appear")
	public void waitForOutboundFlights() throws Throwable {
		PackageScreen.selectThisFlight().perform(click());
		PackageScreen.flightList().perform(waitForViewToDisplay());
	}

	@Then("^I click on the baggage fee info link")
	public void clickOnBaggageFee() throws Throwable {
		PackageScreen.baggageFeeInfo().perform(scrollTo(), click());
	}

	@Then("^I wait for the web info page to load and take screenshot")
	public void waitForWebPageToLoad() throws Throwable {
		//TODO : we'll add the functionality when the framework is ready
	}

	@Then("^I close the baggage fee info page to come to the details page")
	public void closeBaggageFeeWebView() throws Throwable {
		Common.pressBack();
	}


	@Then("^I select the flight and wait for package deal to load")
	public void waitForPackageDealToLoad() throws Throwable {
		PackageScreen.selectThisFlight().perform(click());
		PackageScreen.hotelBundleWidget().perform(waitForViewToDisplay());
	}

	@Then("^I click on change flights to edit flights")
	public void changeFlights() throws Throwable {
		onView(withContentDescription("More options")).perform(click());
		onView(allOf(withText("Change flights"))).perform(click());
	}
}
