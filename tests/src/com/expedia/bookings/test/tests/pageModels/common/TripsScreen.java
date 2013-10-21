package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;
import com.expedia.bookings.widget.ItinListView;

public class TripsScreen extends LaunchActionBar {

	private static final int ENTER_ITINERARY_NUMBER_ID = R.id.or_enter_itin_number_tv;
	private static final int LOG_IN_BUTTON_ID = R.id.login_button;
	private static final int FETCHING_YOUR_ITINERARIES_STRING_ID = R.string.fetching_your_itinerary;
	private static final int ITIN_LIST_VIEW_ID = android.R.id.list;

	public TripsScreen(Instrumentation instrumentation, Activity activity, Resources res, TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public ItinListView itineraryListView() {
		return (ItinListView) getView(ITIN_LIST_VIEW_ID);
	}

	public String fetchingYourItineraries() {
		return getString(FETCHING_YOUR_ITINERARIES_STRING_ID);
	}

	public View enterItinNumberView() {
		return getView(ENTER_ITINERARY_NUMBER_ID);
	}

	public View logInButton() {
		return getView(LOG_IN_BUTTON_ID);
	}

	public void clickEnterItinNumber() {
		clickOnView(enterItinNumberView());
	}

	public void clickOnLogInButton() {
		clickOnView(logInButton());
	}

	public void swipeToLaunchScreen() {
		int screenHeight = mRes.getDisplayMetrics().heightPixels;
		int screenWidth = mRes.getDisplayMetrics().widthPixels;
		screenHeight /= mRes.getDisplayMetrics().density;
		screenWidth /= mRes.getDisplayMetrics().density;
		drag(10, screenWidth - 10, screenHeight / 2, screenHeight / 2, 10);
	}

}
