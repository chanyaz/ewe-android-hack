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

	private static int sTitleTextViewID = R.id.title_text_view;
	private static int sSubtitleTextViewID = R.id.subtitle_text_view;

	private static int sFlightListID = android.R.id.list;

	private static int sSortFlightsViewID = R.id.menu_sort;
	private static int sSortPriceViewID = R.id.menu_select_sort_price;
	private static int sSortDepartsViewID = R.id.menu_select_sort_departs;
	private static int sSortArrivesViewID = R.id.menu_select_sort_arrives;
	private static int sSortDurationViewID = R.id.menu_select_sort_duration;

	private static int sSearchButtonID = R.id.menu_search;

	public FlightsSearchResultsScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public TextView titleTextView() {
		return (TextView) getView(sTitleTextViewID);
	}

	public TextView subtitleTextView() {
		return (TextView) getView(sSubtitleTextViewID);
	}

	public ListView searchResultListView() {
		return (ListView) getView(sFlightListID);
	}

	public View sortFlightsButton() {
		return getView(sSortFlightsViewID);
	}

	public View sortByPriceButton() {
		return getView(sSortPriceViewID);
	}

	public View sortByDepartsButton() {
		return getView(sSortDepartsViewID);
	}

	public View sortByArrivesButton() {
		return getView(sSortArrivesViewID);
	}

	public View sortByDurationButton() {
		return getView(sSortDurationViewID);
	}

	public View searchButton() {
		return getView(sSearchButtonID);
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
		clickOnView(sortByPriceButton());
	}

	public void clickToSortByDeparture() {
		clickOnView(sortByDepartsButton());
	}

	public void clickToSortByArrival() {
		clickOnView(sortByArrivesButton());
	}

	public void clickToSortByDuration() {
		clickOnView(sortByDurationButton());
	}

	public void clickSearchButton() {
		clickOnView(searchButton());
	}
}
