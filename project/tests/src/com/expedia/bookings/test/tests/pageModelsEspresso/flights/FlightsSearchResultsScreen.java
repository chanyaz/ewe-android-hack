package com.expedia.bookings.test.tests.pageModelsEspresso.flights;

import android.view.View;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/7/14.
 */
public class FlightsSearchResultsScreen extends ScreenActions {
	private static final int TITLE_TEXTVIEW_ID = R.id.title_text_view;
	private static final int SUBTITLE_TEXTVIEW_ID = R.id.subtitle_text_view;
	private static final int NO_FLIGHTS_WERE_FOUND_STRING_ID = R.string.error_no_flights_found;
	private static final int FLIGHT_LIST_ID = android.R.id.list;
	private static final int SORT_FLIGHTS_VIEW_ID = R.id.menu_sort;
	private static final int SORT_PRICE_STRING = R.string.sort_description_price;
	private static final int SORT_DEPARTURE_STRING = R.string.sort_description_departure;
	private static final int SORT_ARRIVAL_STRING = R.string.sort_description_arrival;
	private static final int SORT_DURATION_STRING = R.string.sort_description_duration;
	private static final int SEARCH_BUTTON_ID = R.id.menu_search;

// Object access

	public static ViewInteraction titleTextView() {
		return onView(withId(TITLE_TEXTVIEW_ID));
	}

	public static ViewInteraction subtitleTextView() {
		return onView(withId(SUBTITLE_TEXTVIEW_ID));
	}

	public static ViewInteraction searchResultListView() {
		return onView(withId(FLIGHT_LIST_ID));
	}

	public static ViewInteraction sortFlightsButton() {
		return onView(withId(SORT_FLIGHTS_VIEW_ID));
	}

	public static ViewInteraction sortByPriceString() {
		return onView(withText(SORT_PRICE_STRING));
	}

	public static ViewInteraction sortByDepartsString() {
		return onView(withText(SORT_DEPARTURE_STRING));
	}

	public static ViewInteraction sortByArrivesString() {
		return onView(withText(SORT_ARRIVAL_STRING));
	}

	public static ViewInteraction sortbyDurationString() {
		return onView(withText(SORT_DURATION_STRING));
	}

	public static ViewInteraction searchButton() {
		return onView(withId(SEARCH_BUTTON_ID));
	}

	public static FlightsSearchResultRow getSearchResultModelFromView(View view) {
		return new FlightsSearchResultRow(view);
	}

	public static ViewInteraction firstListItem() {
		return onView(withId(android.R.id.list));
	}

	public static ViewInteraction noFlightsWereFound() {
		return onView(withText(NO_FLIGHTS_WERE_FOUND_STRING_ID));
	}

	// Object interactions
	public static void clickFirstListItem() {

		firstListItem().perform(click());
	}

	public static void clickSortFlightsButton() {
		sortFlightsButton().perform(click());
	}

	public static void clickToSortByPrice() {
		sortByPriceString().perform(click());
	}

	public static void clickToSortByDeparture() {
		sortByDepartsString().perform(click());
	}

	public static void clickToSortByArrival() {
		sortByArrivesString().perform(click());
	}

	public static void clickToSortByDuration() {
		sortbyDurationString().perform(click());
	}

	public static void clickSearchButton() {
		searchButton().perform(click());
	}
}