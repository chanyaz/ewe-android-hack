package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;

import android.support.annotation.IdRes;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class PackageFlightFilterTest extends PackageTestCase {

	public void testPackageFlightsFiltersOverview() throws Throwable {
		openFlightFilter();
		scrollToViewWithId(R.id.duration);
		visibleWithText(R.id.duration, "17hr");
		scrollToViewWithId(R.id.departure_range_min_text);
		visibleWithText(R.id.departure_range_min_text, "12:00 am");
		visibleWithText(R.id.departure_range_max_text, "11:59 pm");
		scrollToViewWithId(R.id.arrival_range_min_text);
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

	public void testCheckableFilters() throws Throwable {
		openFlightFilter();

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

	public void testSliders() throws Throwable {
		openFlightFilter();

		//filter by duration
		scrollToViewWithId(R.id.duration);
		durationSeekBar().perform(ViewActions.setCustomSeekBarTo(9));
		visibleWithText(R.id.duration, "9hr");
		checkFilteredFlights("2 Results");

		//filter by departure range
		scrollToViewWithId(R.id.departure_range_min_text);
		departureRangeSeekBar().perform(ViewActions.setCustomRangeSeekBarTo(8, 9));
		visibleWithText(R.id.departure_range_min_text, "8:00 am");
		visibleWithText(R.id.departure_range_max_text, "9:00 am");
		checkFilteredFlights("1 Result");

		//filter by arrival range
		scrollToViewWithId(R.id.arrival_range_min_text);
		arrivalRangeSeekBar().perform(ViewActions.setCustomRangeSeekBarTo(12, 16));
		visibleWithText(R.id.arrival_range_min_text, "12:00 pm");
		visibleWithText(R.id.arrival_range_max_text, "4:00 pm");
		checkFilteredFlights("0 Results");

		resetFilters();
	}

	public void testZeroResults() throws Throwable {
		openFlightFilter();

		scrollToViewWithId(R.id.duration);
		durationSeekBar().perform(ViewActions.setCustomSeekBarTo(1));
		visibleWithText(R.id.duration, "1hr");
		checkFilteredFlights("0 Results");

		clickDone();
		//dynamic feedback counter still displayed
		checkFilteredFlights("0 Results");
		Common.pressBack();
		checkFilteredFlights("0 Results");

		resetFilters();
		clickDone();
		PackageScreen.flightList().perform(waitForViewToDisplay());
		assertBestFlightOnTop();
	}

	public void testFlightResultsFiltered() throws Throwable {
		openFlightFilter();

		selectSorting("Duration");
		tickCheckboxWithText("Virgin America");
		tickCheckboxWithText("Hawaiian Airlines");
		checkFilteredFlights("2 Results");
		clickDone();
		PackageScreen.flightList().perform(waitForViewToDisplay());

		EspressoUtils
			.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), 0, R.id.flight_time_detail_text_view,
				"8:20 am - 11:00 am");
		EspressoUtils
			.assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), 1, R.id.flight_time_detail_text_view,
				"9:50 am - 11:40 pm");
	}

	public void testSorting() throws Throwable {
		openFlightFilter();

		//Check default sort is price
		onView(withId(R.id.sort_by_selection_spinner)).check(matches(withSpinnerText(containsString("Price"))));

		//sort by duration
		selectSorting("Duration");
		clickDone();
		PackageScreen.flightList().perform(waitForViewToDisplay());
		assertBestFlightOnTop();
	}

	public void testBestFlightFilter() throws Throwable {
		openFlightFilter();
		tickCheckboxWithText("Hawaiian Airlines");
		clickDone();
		PackageScreen.flightList().perform(waitForViewToDisplay());
		onView(withId(R.id.package_best_flight)).check(matches(not(isDisplayed())));

		PackageScreen.flightsToolbarFilterMenu().perform(click());
		PackageScreen.flightFilterView().perform(waitForViewToDisplay());
		resetFilters();
		// best flight banner hidden if only flight in filtered list
		tickCheckboxWithText("United");
		clickDone();
		PackageScreen.flightList().perform(waitForViewToDisplay());
		onView(withId(R.id.package_best_flight)).check(matches(not(isDisplayed())));
	}

	private void assertBestFlightOnTop() {
		onView(withId(R.id.all_flights_header)).check(matches(isDisplayed()));
		assertViewWithIdIsDisplayedAtPosition(PackageScreen.flightList(), 1, R.id.package_best_flight);
	}

	private void assertViewWithIdIsDisplayedAtPosition(ViewInteraction viewInteraction, int position, int id) {
		viewInteraction.check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), isDisplayed()))));
	}

	private void clickDone() {
		onView(withId(R.id.search_btn)).perform(click());
	}

	private void selectSorting(String duration) {
		onView(withId(R.id.sort_by_selection_spinner)).perform(click());
		onData(allOf(is(instanceOf(String.class)), is(duration))).perform(click());
		onView(withId(R.id.sort_by_selection_spinner)).check(matches(withSpinnerText(containsString(duration))));
	}

	private void openFlightFilter() throws Throwable {
		PackageScreen.selectDepartureAndArrival();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		PackageScreen.selectDates(startDate, endDate);
		PackageScreen.searchButton().perform(click());

		PackageScreen.hotelBundle().perform(waitForViewToDisplay());
		PackageScreen.hotelBundle().perform(click());

		HotelScreen.selectHotel("Package Happy Path");
		HotelScreen.selectRoom();

		PackageScreen.outboundFlight().perform(waitForViewToDisplay());
		PackageScreen.outboundFlight().perform(click());

		PackageScreen.flightList().perform(waitForViewToDisplay());
		PackageScreen.flightsToolbarFilterMenu().perform(click());
		PackageScreen.flightFilterView().perform(waitForViewToDisplay());
	}

	@IdRes
	private void visibleWithText(int resId, String text) {
		onView(allOf(withId(resId), withText(text))).check(matches(isDisplayed()));
	}

	@IdRes
	private void scrollToViewWithId(int resId) {
		onView(allOf(withId(resId))).perform(scrollTo());
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
