package com.expedia.bookings.test.tests.pageModels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class LaunchScreen extends LaunchActionBar {

	private static int sHotelsButtonID = R.id.hotels_button;
	private static int sFlightsButtonID = R.id.flights_button;
	public LaunchActionBar mLaunchActionBar;

	public LaunchScreen(Instrumentation instrumentation, Activity activity, Resources res) {
		super(instrumentation, activity, res);
	}

	private View hotelLaunchButton() {
		return getView(sHotelsButtonID);
	}

	private View flightLaunchButton() {
		return getView(sFlightsButtonID);
	}

	public void launchHotels() {
		clickOnView(hotelLaunchButton());
	}

	public void launchFlights() {
		clickOnView(flightLaunchButton());
	}

}
