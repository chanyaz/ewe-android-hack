package com.expedia.bookings.test.ui.tablet.pagemodels;

import java.util.concurrent.atomic.AtomicReference;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.getChildViewText;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by dmadan on 6/9/14.
 */
public class SortFilter {

	public static void clickHotelSortFilterButton() {
		onView(withId(R.id.top_right_text_button)).perform(click());
	}

	public static void clickToSortHotelByPrice() {
		onView(withId(R.id.sort_by_selection_spinner)).perform(click());
		onView(withText(R.string.price)).perform(click());
	}

	public static void clickToSortHotelByRating() {
		onView(withId(R.id.sort_by_selection_spinner)).perform(click());
		onView(withText(R.string.rating)).perform(click());
	}

	public static void clickToSortByPrice() {
		onView(withId(R.id.flight_sort_control)).perform(click());
		onView(withText(R.string.sort_description_price)).perform(click());
	}

	public static void clickToSortByArrival() {
		onView(withId(R.id.flight_sort_control)).perform(click());
		onView(withText(R.string.sort_description_arrival)).perform(click());
	}

	public static void clickToSortByDeparture() {
		onView(withId(R.id.flight_sort_control)).perform(click());
		onView(withText(R.string.sort_description_departure)).perform(click());
	}

	public static void clickToSortByDuration() {
		onView(withId(R.id.flight_sort_control)).perform(click());
		onView(withText(R.string.sort_description_duration)).perform(click());
	}

	public static String getfilterAirlineView(int index) {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(withId(R.id.filter_airline_container)).perform(getChildViewText(index, value));
		String filterValue = value.get();
		return filterValue;
	}

	public static ViewInteraction airlineNameFilter(String airlineName) {
		return onView(allOf(withText(airlineName), hasSibling(withId(R.id.filter_refinement_textview))));
	}

	public static void clickAirlineFilter(String airlineName) {
		airlineNameFilter(airlineName).perform(scrollTo());
		airlineNameFilter(airlineName).perform(click());
	}

	public static void clickHighRatingFilterButton() {
		onView(withId(R.id.rating_high_button)).perform(click());
	}

	public static void clickMediumRatingFilterButton() {
		onView(withId(R.id.rating_medium_button)).perform(click());
	}

	public static void clickLowRatingFilterButton() {
		onView(withId(R.id.rating_low_button)).perform(click());
	}

	public static void clickAllRatingFilterButton() {
		onView(withId(R.id.rating_all_button)).perform(click());
	}

	public static void clickVIPAccessFilterButton() {
		onView(withId(R.id.filter_vip_access_switch)).perform(click());
	}

	public static ViewInteraction vipImageView() {
		return onView(allOf(withId(R.id.vip_badge), hasSibling(withId(R.id.hotel_header_hotel_name))));
	}

	public static void clickLargeRadiusFilterButton() {
		onView(withId(R.id.radius_large_button)).perform(click());
	}

	public static void clickMediumRadiusFilterButton() {
		onView(withId(R.id.radius_medium_button)).perform(click());
	}

	public static void clickSmallRadiusFilterButton() {
		onView(withId(R.id.radius_small_button)).perform(click());
	}

	public static ViewInteraction filterEditText() {
		return onView(withId(R.id.filter_hotel_name_edit_text));
	}

	public static void enterFilterText(String text) {
		filterEditText().perform(typeText(text));
	}

	public static void clearFilterText() {
		filterEditText().perform(clearText());
	}

}
