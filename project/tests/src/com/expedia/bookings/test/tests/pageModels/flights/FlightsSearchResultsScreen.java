package com.expedia.bookings.test.tests.pageModels.flights;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

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

	public FlightsSearchResultsScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public TextView titleTextView() {
		return (TextView) getView(TITLE_TEXTVIEW_ID);
	}

	public TextView subtitleTextView() {
		return (TextView) getView(SUBTITLE_TEXTVIEW_ID);
	}

	public ListView searchResultListView() {
		return (ListView) getView(FLIGHT_LIST_ID);
	}

	public View sortFlightsButton() {
		return getView(SORT_FLIGHTS_VIEW_ID);
	}

	public String sortByPriceString() {
		return getString(SORT_PRICE_STRING);
	}

	public String sortByDepartsString() {
		return getString(SORT_DEPARTURE_STRING);
	}

	public String sortByArrivesString() {
		return getString(SORT_ARRIVAL_STRING);
	}

	public String sortbyDurationString() {
		return getString(SORT_DURATION_STRING);
	}

	public View searchButton() {
		return getView(SEARCH_BUTTON_ID);
	}

	public FlightsSearchResultRow getSearchResultModelFromView(View view) {
		return new FlightsSearchResultRow(view);
	}

	public String noFlightsWereFound() {
		return getString(NO_FLIGHTS_WERE_FOUND_STRING_ID);
	}

	// Object interactions

	// The clickable flights start at index 1, but to maintain
	// a 0-indexed appearance, we automatically increment the
	// passed index by 2
	public void selectFlightFromList(int index) {
		clickOnView(searchResultListView().getChildAt(index + 1));
	}

	public void clickSortFlightsButton() {
		clickOnView(sortFlightsButton());
	}

	public void clickToSortByPrice() {
		clickOnText(sortByPriceString());
	}

	public void clickToSortByDeparture() {
		clickOnText(sortByDepartsString());
	}

	public void clickToSortByArrival() {
		clickOnText(sortByArrivesString());
	}

	public void clickToSortByDuration() {
		clickOnText(sortbyDurationString());
	}

	public void clickSearchButton() {
		clickOnView(searchButton());
	}
}
