package com.expedia.bookings.test.ui.phone.pagemodels.flights;

import com.expedia.bookings.R;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

public class FlightsSearchResultsScreen {
	private static final int FLIGHT_LIST_ID = android.R.id.list;
	private static final int SORT_FLIGHTS_VIEW_ID = R.id.menu_sort;
	private static final int SORT_PRICE_STRING = R.string.sort_description_price;
	private static final int SORT_DEPARTURE_STRING = R.string.sort_description_departure;
	private static final int SORT_ARRIVAL_STRING = R.string.sort_description_arrival;
	private static final int SORT_DURATION_STRING = R.string.sort_description_duration;

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

}
