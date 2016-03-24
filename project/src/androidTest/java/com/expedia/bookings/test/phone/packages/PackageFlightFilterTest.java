package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;

import android.support.annotation.IdRes;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
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
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class PackageFlightFilterTest extends PackageTestCase {

	public void testPackageFlightsFiltersOverview() throws Throwable {
		openFlightFilter();
		scrollToViewWithId(R.id.duration_range_min_text);
		visibleWithText(R.id.duration_range_min_text, "5hr");
		visibleWithText(R.id.duration_range_max_text, "12hr");
		scrollToViewWithId(R.id.departure_range_min_text);
		visibleWithText(R.id.departure_range_min_text, "8:00");
		visibleWithText(R.id.departure_range_max_text, "10:00");
		scrollToViewWithId(R.id.arrival_range_min_text);
		visibleWithText(R.id.arrival_range_min_text, "11:00");
		visibleWithText(R.id.arrival_range_max_text, "17:00");

		// Fix these counts for best flight #6747
		checkResultsCountForCheckbox("Nonstop", "3");
		checkResultsCountForCheckbox("2+ Stops", "1");
		checkResultsCountForCheckbox("Hawaiian Airlines", "1");
		checkResultsCountForCheckbox("United", "2");
		checkResultsCountForCheckbox("Virgin America", "1");
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

	public void testSorting() throws Throwable {
		openFlightFilter();

		//Check default sort is price
		onView(withId(R.id.sort_by_selection_spinner)).check(matches(withSpinnerText(containsString("Price"))));

		//sort by duration
		selectSorting("Duration");
		Common.delay(1);
		done();
		Common.delay(1);
		assertBestFlightOnTop();
	}

	public void testBestFlightFilter() throws Throwable {
		openFlightFilter();
		tickCheckboxWithText("Hawaiian Airlines");
		done();
		Common.delay(1);
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

	private void done() {
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
		Common.delay(1);

		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(1);

		HotelScreen.selectRoom();
		Common.delay(1);

		PackageScreen.outboundFlight().perform(click());
		Common.delay(1);

		PackageScreen.flightsToolbarFilterMenu().perform(click());
		Common.delay(1);
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
	}
}
