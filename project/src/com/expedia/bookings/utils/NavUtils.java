package com.expedia.bookings.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.support.v4.content.LocalBroadcastManager;
import com.expedia.bookings.activity.*;
import com.expedia.bookings.data.Db;
import com.mobiata.android.Log;

/**
 * Utilities for navigating the app (between Activities)
 *
 */
public class NavUtils {

	public static void goToLaunchScreen(Context context) {
		sendKillActivityBroadcast(context);

		Intent intent = new Intent(context, LaunchActivity.class);
		context.startActivity(intent);
	}

	public static void goToHotels(Context context) {
		sendKillActivityBroadcast(context);

		// Start the Hotels activity
		// TODO: make this smarter for future when user can access EH tablet using NavUtils
		Intent intent = new Intent(context, PhoneSearchActivity.class);
		context.startActivity(intent);
	}

	private static void sendKillActivityBroadcast(Context context) {
		// Send the kill activity broadcast to ensure the activity backstack is erased
		//
		// Note: All activities must register a LocalBroadcastReceiver on the KILL_ACTIVITY
		// intent to guarantee the backstack is actually erased.
		Intent kill = new Intent();
		kill.setAction(ActivityKillReceiver.BROADCAST_KILL_ACTIVITY_INTENT);
		LocalBroadcastManager.getInstance(context).sendBroadcast(kill);
	}

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
}
