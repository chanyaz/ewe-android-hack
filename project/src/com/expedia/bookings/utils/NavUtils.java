package com.expedia.bookings.utils;

import com.expedia.bookings.activity.FlightSearchResultsActivity;
import com.expedia.bookings.activity.FlightTripOverviewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;

import android.content.Context;
import android.content.Intent;

/**
 * Utilities for navigating the app (between Activities)
 *
 */
public class NavUtils {

	public static void onFlightLegSelected(Context context) {
		FlightSearch search = Db.getFlightSearch();

		FlightLeg[] selectedLegs = search.getSelectedLegs();
		int lastUnselected = -1;
		for (int a = 0; a < selectedLegs.length; a++) {
			if (selectedLegs[a] == null) {
				lastUnselected = a;
				break;
			}
		}

		if (lastUnselected != -1) {
			// If the user hasn't selected all legs yet, push them to select the next leg
			Intent intent = new Intent(context, FlightSearchResultsActivity.class);
			intent.putExtra(FlightSearchResultsActivity.EXTRA_LEG_POSITION, lastUnselected);
			context.startActivity(intent);
		}
		else {
			Intent intent = new Intent(context, FlightTripOverviewActivity.class);
			intent.putExtra(FlightTripOverviewActivity.EXTRA_TRIP_KEY, search.getSelectedFlightTrip().getProductKey());
			context.startActivity(intent);
		}
	}
}
