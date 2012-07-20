package com.expedia.bookings.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.activity.FlightSearchResultsActivity;
import com.expedia.bookings.activity.FlightTripOverviewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightTripLeg;
import com.mobiata.android.Log;

/**
 * Utilities for navigating the app (between Activities)
 *
 */
public class NavUtils {

	public static void onDataMissing(Activity activity) {
		Log.i("Key data missing - resetting the app!");

		// Reset the db
		Db.clear();

		// Go back to the start
		Intent intent = new Intent(activity, FlightSearchActivity.class);
		intent.putExtra(FlightSearchActivity.EXTRA_DATA_EXPIRED, true);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);

		// Finish the current Activity
		activity.finish();
	}

	public static void onFlightLegSelected(Context context) {
		FlightSearch search = Db.getFlightSearch();

		FlightTripLeg[] selectedLegs = search.getSelectedLegs();
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
