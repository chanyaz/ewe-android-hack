package com.expedia.bookings.test.phone.pagemodels.flights;

import android.support.test.espresso.DataInteraction;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;

public class FlightsSearchResultsScreen {

	public static DataInteraction listItem() {
		return onData(anything()).inAdapterView(withId(android.R.id.list));
	}

	// Object interactions
	public static void clickListItem(int index) {
		listItem().atPosition(index).perform(click());
	}

}
