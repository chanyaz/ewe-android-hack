package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import android.support.annotation.IdRes;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class PackageFlightFilterTest extends PackageTestCase {

	public void testPackageFlightsFiltersOverview() throws Throwable {
		openFlightFilter();
		scrollToViewWithId(R.id.price_range_min_text);
		visibleWithText(R.id.price_range_min_text, "$3,863");
		visibleWithText(R.id.price_range_max_text, "$4,211");
		scrollToViewWithId(R.id.duration_range_min_text);
		visibleWithText(R.id.duration_range_min_text, "5h");
		visibleWithText(R.id.duration_range_max_text, "9h");
		scrollToViewWithId(R.id.departure_range_min_text);
		visibleWithText(R.id.departure_range_min_text, "8:00");
		visibleWithText(R.id.departure_range_max_text, "10:00");
		scrollToViewWithId(R.id.arrival_range_min_text);
		visibleWithText(R.id.arrival_range_min_text, "11:00");
		visibleWithText(R.id.arrival_range_max_text, "17:00");
	}

	public void testCheckableFilters() throws Throwable {
		openFlightFilter();

		tickCheckboxWithText("Nonstop");
		checkFilteredFlights("3 Results");
		resetFilters();

		tickCheckboxWithText("1 Stop");
		checkFilteredFlights("1 Result");
		resetFilters();

		tickCheckboxWithText("Nonstop");
		tickCheckboxWithText("1 Stop");
		checkFilteredFlights("4 Results");
		resetFilters();

		tickCheckboxWithText("Hawaiian Airlines");
		checkFilteredFlights("1 Result");
		resetFilters();

		tickCheckboxWithText("United");
		checkFilteredFlights("2 Results");
		resetFilters();

		tickCheckboxWithText("Virgin America");
		checkFilteredFlights("1 Result");
		resetFilters();

		tickCheckboxWithText("Hawaiian Airlines");
		tickCheckboxWithText("United");
		tickCheckboxWithText("Virgin America");
		checkFilteredFlights("4 Results");
	}

	public void testDurationSorting() throws Throwable {
		openFlightFilter();
		selectSorting("Duration");
		Common.delay(1);
		done();
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
		PackageScreen.destination().perform(typeText("SFO"));
		PackageScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		PackageScreen.arrival().perform(typeText("DTW"));
		PackageScreen.selectLocation("Detroit, MI (DTW-Detroit Metropolitan Wayne County)");
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
		onView(allOf(withId(R.id.check_box), hasSibling(allOf(withId(R.id.label), withText(title)))))
			.perform(scrollTo());
		onView(allOf(withId(R.id.check_box), hasSibling(allOf(withId(R.id.label), withText(title))))).perform(click());
	}

	private void checkFilteredFlights(String text) {
		visibleWithText(R.id.dynamic_feedback_counter, text);
	}

	private void resetFilters() {
		onView(withId(R.id.dynamic_feedback_clear_button)).perform(click());
	}
}
