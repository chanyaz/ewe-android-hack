package com.expedia.bookings.test.tests.pageModels.tablet;

import java.util.concurrent.atomic.AtomicReference;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.expedia.bookings.test.utilsEspresso.ViewActions.getChildViewText;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasSibling;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by dmadan on 6/9/14.
 */
public class SortFilter {

	public static void clickHotelSortFilterButton() {
		onView(withId(R.id.top_right_text_button)).perform(click());
	}

	public static void clickToSortHotelByPrice() {
		onView(withId(R.id.sort_by_price_button)).perform(click());
	}

	public static void clickToSortHotelByRating() {
		onView(withId(R.id.sort_by_rating_button)).perform(click());
	}

	public static void clickToSortByPrice() {
		onView(allOf(withId(R.id.flight_sort_price), withParent(withId(R.id.flight_sort_control)))).perform(click());
	}

	public static void clickToSortByArrival() {
		onView(allOf(withId(R.id.flight_sort_arrives), withParent(withId(R.id.flight_sort_control)))).perform(click());
	}

	public static void clickToSortByDeparture() {
		onView(allOf(withId(R.id.flight_sort_departs), withParent(withId(R.id.flight_sort_control)))).perform(click());
	}

	public static void clickToSortByDuration() {
		onView(allOf(withId(R.id.flight_sort_duration), withParent(withId(R.id.flight_sort_control)))).perform(click());
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
