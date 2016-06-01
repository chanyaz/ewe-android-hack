package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import android.support.annotation.IdRes;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUser;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class PackageFlightFilterTest extends PackageTestCase {

	public void testPackageFlightsFiltersAccessibility() throws Throwable {
		SearchScreen.selectOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
		SearchScreen.searchButton().perform(click());

		HotelScreen.selectHotel("Package Happy Path");
		HotelScreen.selectRoomButton().perform(click());
		HotelScreen.clickRoom("Packages Flights Show More Airlines");
		PackageScreen.selectRoom();

		PackageScreen.flightList().perform(waitForViewToDisplay());
		openFlightFilter();

		EspressoUser.scrollToView(R.id.collapsed_container);
		PackageScreen.showMoreButton().check(matches(isDisplayed()));
		PackageScreen.showMoreButton().check(matches(withContentDescription("Click here to show more airlines")));
		PackageScreen.showMoreButton().perform(click());
		EspressoUser.scrollToView(R.id.collapsed_container);
		PackageScreen.showMoreButton().check(matches(withContentDescription("Click here to show fewer airlines")));
		PackageScreen.showMoreButton().perform(click());
	}

	public void testPackageFlightsFilters() throws Throwable {
		navigateFromLaunchToFlightFilter();

		assertFlightFilterOverview();
		//apply filters
		filterByStops();
		filterByAirlines();
		filterByDuration();
		filterByDepartureArrival();

		// test zero results
		filterAllResults();

		// test flight results filtered
		assertFlightResultsFiltered();

		// test best flight filter
		checkBestFlightNotDisplayed();
		checkBestFlightDisplayed();
	}

	private void assertFlightFilterOverview() {
		//Check default sort is price
		onView(withId(R.id.sort_by_selection_spinner)).check(matches(withSpinnerText(containsString("Price"))));

		EspressoUser.scrollToView(R.id.duration);
		visibleWithText(R.id.duration, "17hr");
		EspressoUser.scrollToView(R.id.departure_range_min_text);
		visibleWithText(R.id.departure_range_min_text, "12:00 am");
		visibleWithText(R.id.departure_range_max_text, "11:59 pm");
		EspressoUser.scrollToView(R.id.arrival_range_min_text);
		visibleWithText(R.id.arrival_range_min_text, "12:00 am");
		visibleWithText(R.id.arrival_range_max_text, "11:59 pm");

		// Fix these counts for best flight #6747
		checkResultsCountForCheckbox("Nonstop", "3");
		checkResultsCountForCheckbox("2+ Stops", "1");
		checkResultsCountForCheckbox("Hawaiian Airlines", "1");
		checkResultsCountForCheckbox("United", "2");
		checkResultsCountForCheckbox("Virgin America", "1");

		// No show more displayed since only 3 airlines
		EspressoUtils.assertViewIsNotDisplayed(R.id.show_more_less_text);
	}

	private void filterByStops() {
		tickCheckboxWithText("Nonstop");
		checkFilteredFlights("2 Results");
		resetFilters();
		tickCheckboxWithText("2+ Stops");
		checkFilteredFlights("1 Result");
		resetFilters();
		tickCheckboxWithText("Nonstop");
		tickCheckboxWithText("2+ Stops");
		checkFilteredFlights("3 Results");
		resetFilters();
	}

	private void filterByAirlines() {
		tickCheckboxWithText("Hawaiian Airlines");
		checkFilteredFlights("1 Result");
		resetFilters();
		tickCheckboxWithText("United");
		checkFilteredFlights("1 Result");
		resetFilters();
		tickCheckboxWithText("Virgin America");
		checkFilteredFlights("1 Result");
		resetFilters();
		tickCheckboxWithText("United");
		tickCheckboxWithText("Hawaiian Airlines");
		tickCheckboxWithText("Virgin America");
		checkFilteredFlights("3 Results");
	}

	private void filterByDuration() {
		//filter by duration
		resetFilters();
		EspressoUser.scrollToView(R.id.duration);
		durationSeekBar().perform(ViewActions.setCustomSeekBarTo(9));
		visibleWithText(R.id.duration, "9hr");
		checkFilteredFlights("2 Results");
	}

	private void filterByDepartureArrival() {
		//filter by departure range
		EspressoUser.scrollToView(R.id.departure_range_min_text);
		departureRangeSeekBar().perform(ViewActions.setCustomRangeSeekBarTo(8, 9));
		visibleWithText(R.id.departure_range_min_text, "8:00 am");
		visibleWithText(R.id.departure_range_max_text, "9:00 am");
		checkFilteredFlights("1 Result");

		//filter by arrival range
		EspressoUser.scrollToView(R.id.arrival_range_min_text);
		arrivalRangeSeekBar().perform(ViewActions.setCustomRangeSeekBarTo(12, 16));
		visibleWithText(R.id.arrival_range_min_text, "12:00 pm");
		visibleWithText(R.id.arrival_range_max_text, "4:00 pm");
		checkFilteredFlights("0 Results");

		resetFilters();
	}

	private void filterAllResults() {
		EspressoUser.scrollToView(R.id.duration);
		durationSeekBar().perform(ViewActions.setCustomSeekBarTo(1));
		visibleWithText(R.id.duration, "1hr");
		checkFilteredFlights("0 Results");

		// verify cannot click done since there are zero results
		clickDone();
		checkFilteredFlights("0 Results");

		// verify cannot click back since there are zero results
		Common.pressBack();
		checkFilteredFlights("0 Results");

		resetFilters();
		clickDone();
		assertBestFlightOnTop();
	}

	private void assertFlightResultsFiltered() {
		openFlightFilter();
		selectSorting("Duration");
		tickCheckboxWithText("Virgin America");
		tickCheckboxWithText("Hawaiian Airlines");
		checkFilteredFlights("2 Results");
		clickDone();
		PackageScreen.flightList().perform(waitForViewToDisplay());

		EspressoUtils
			.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), 1, R.id.flight_time_detail_text_view,
				"8:20 am - 11:00 am");
		EspressoUtils
			.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), 2, R.id.flight_time_detail_text_view,
				"9:50 am - 11:40 pm");
	}

	private void checkBestFlightNotDisplayed() {
		openFlightFilter();
		tickCheckboxWithText("Hawaiian Airlines");
		clickDone();
		assertBestFlightNotDisplayed();

		openFlightFilter();
		resetFilters();
		// best flight banner hidden if only flight in filtered list
		tickCheckboxWithText("United");
		clickDone();
		assertBestFlightNotDisplayed();
	}

	private void checkBestFlightDisplayed() {
		openFlightFilter();
		selectSorting("Duration");
		tickCheckboxWithText("Virgin America");
		clickDone();
		assertBestFlightOnTop();
	}

	private void assertBestFlightOnTop() {
		PackageScreen.flightList().perform(waitForViewToDisplay());
		EspressoUtils.assertViewIsDisplayed(R.id.all_flights_header);
		EspressoUtils.assertViewIsDisplayed(R.id.flight_results_price_header);
		EspressoUtils.assertViewWithIdIsDisplayedAtPosition(PackageScreen.flightList(), 1, R.id.package_best_flight);
	}

	private void assertBestFlightNotDisplayed() {
		PackageScreen.flightList().perform(waitForViewToDisplay());
		EspressoUtils.assertViewWithIdIsNotDisplayedAtPosition(PackageScreen.flightList(), 1, R.id.package_best_flight);
	}

	private void clickDone() {
		onView(withId(R.id.search_btn)).perform(click());
	}

	private void selectSorting(String duration) {
		onView(withId(R.id.sort_by_selection_spinner)).perform(scrollTo(), click());
		onData(allOf(is(instanceOf(String.class)), is(duration))).perform(click());
		onView(withId(R.id.sort_by_selection_spinner)).check(matches(withSpinnerText(containsString(duration))));
	}

	private void navigateFromLaunchToFlightFilter() throws Throwable {
		SearchScreen.selectOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
		SearchScreen.searchButton().perform(click());

		HotelScreen.selectHotel("Package Happy Path");
		PackageScreen.selectRoom();

		PackageScreen.flightList().perform(waitForViewToDisplay());
		openFlightFilter();
	}

	private void openFlightFilter() {
		PackageScreen.flightsToolbarFilterMenu().perform(click());
		PackageScreen.flightFilterView().perform(waitForViewToDisplay());
	}

	@IdRes
	private void visibleWithText(int resId, String text) {
		onView(allOf(withId(resId), withText(text))).check(matches(isDisplayed()));
	}

	private void tickCheckboxWithText(String title) {
		checkBoxWithTitle(title).perform(scrollTo());
		checkBoxWithTitle(title).perform(click());
	}

	private void checkResultsCountForCheckbox(String title, String count) {
		checkBoxWithTitle(title).perform(scrollTo());
		checkBoxWithTitle(title).check(matches(hasSibling(allOf(withId(R.id.results_label), withText(count)))));
	}

	private ViewInteraction checkBoxWithTitle(String title) {
		return onView(allOf(withId(R.id.check_box), hasSibling(allOf(withId(R.id.label), withText(title)))));
	}

	private void checkFilteredFlights(String text) {
		visibleWithText(R.id.dynamic_feedback_counter, text);
	}

	private void resetFilters() {
		onView(withId(R.id.dynamic_feedback_clear_button)).perform(click());
		EspressoUtils.assertViewIsNotDisplayed(R.id.dynamic_feedback_container);
	}

	private static ViewInteraction durationSeekBar() {
		return onView(withId(R.id.duration_seek_bar));
	}

	private static ViewInteraction arrivalRangeSeekBar() {
		return onView(withId(R.id.arrival_range_bar));
	}

	private static ViewInteraction departureRangeSeekBar() {
		return onView(withId(R.id.departure_range_bar));
	}

}
