package com.expedia.bookings.test.ui.phone.pagemodels.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

/**
 * Created by dmadan on 4/7/14.
 */
public class FlightsSearchResultsScreen extends ScreenActions {
	private static final int FLIGHT_LIST_ID = android.R.id.list;
	private static final int SORT_FLIGHTS_VIEW_ID = R.id.menu_sort;
	private static final int SORT_PRICE_STRING = R.string.sort_description_price;
	private static final int SORT_DEPARTURE_STRING = R.string.sort_description_departure;
	private static final int SORT_ARRIVAL_STRING = R.string.sort_description_arrival;
	private static final int SORT_DURATION_STRING = R.string.sort_description_duration;
	private static final int SEARCH_BUTTON_ID = R.id.menu_search;

	// Object access

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

	public static DataInteraction listItem() {
		return onData(anything()).inAdapterView(withId(android.R.id.list));
	}

	// Object interactions
	public static void clickListItem(int index) {
		listItem().atPosition(index).perform(click());
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
