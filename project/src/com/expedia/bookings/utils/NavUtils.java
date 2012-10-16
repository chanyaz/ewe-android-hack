package com.expedia.bookings.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.expedia.bookings.activity.ActivityKillReceiver;
import com.expedia.bookings.activity.BookingFragmentActivity;
import com.expedia.bookings.activity.BookingInfoActivity;
import com.expedia.bookings.activity.ConfirmationFragmentActivity;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.activity.FlightSearchResultsActivity;
import com.expedia.bookings.activity.FlightUnsupportedPOSActivity;
import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.activity.SearchFragmentActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.ConfirmationState;
import com.expedia.bookings.data.ConfirmationState.Type;
import com.expedia.bookings.data.Db;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;

/**
 * Utilities for navigating the app (between Activities)
 *
 */
public class NavUtils {

	public static void goToLaunchScreen(Context context) {
		Intent intent = new Intent(context, LaunchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}

	public static void goToHotels(Context context) {
		sendKillActivityBroadcast(context);

		Class<? extends Activity> routingTarget;

		// #7090: First, check to see if the user last confirmed a booking.  If that is the case,
		//        then we should forward the user to the ConfirmationActivity
		if (ConfirmationState.hasSavedData(context, Type.HOTEL)) {
			routingTarget = ConfirmationFragmentActivity.class;
		}

		// 13820: Check if a booking is in process at this moment (in case BookingInfoActivity died)
		else if (BackgroundDownloader.getInstance().isDownloading(BookingInfoActivity.BOOKING_DOWNLOAD_KEY)) {
			routingTarget = BookingInfoActivity.class;
		}

		// Send user to EH phone by default
		else {
			routingTarget = PhoneSearchActivity.class;
		}

		// Launch activity based on routing selection
		Intent intent = new Intent(context, routingTarget);
		context.startActivity(intent);
	}

	public static void goToFlights(Context context) {
		if (FlightUnsupportedPOSActivity.isSupportedPOS(context)) {
			sendKillActivityBroadcast(context);
			context.startActivity(new Intent(context, FlightSearchActivity.class));
		}
		else {
			// Because the user can't actually navigate forward from here, perhaps it makes sense to preserve the
			// backstack so as not to add insult to injury (can't access Flights, lost activity backstack)
			context.startActivity(new Intent(context, FlightUnsupportedPOSActivity.class));
		}
	}

	// Assumes we are already searching in flights, but are not on the flight
	// search screen anymore
	public static void restartFlightSearch(Context context) {
		// Clear out old data
		Db.setBillingInfo(new BillingInfo());
		Db.getFlightSearch().setSearchResponse(null);

		// Launch search activity (new search should start automatically due to blank data)
		Intent intent = new Intent(context, FlightSearchResultsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	/**
	 * Helper method for determining whether or not to skip launch and start EH tablet
	 * @param context
	 * @return true if EHTablet should be (and has been) launched
	 */
	public static boolean skipLaunchScreenAndStartEHTablet(Context context) {
		// #7090: First, check to see if the user last confirmed a booking.  If that is the case,
		//        then we should forward the user to the ConfirmationActivity
		if (ExpediaBookingApp.useTabletInterface(context) && ConfirmationState.hasSavedData(context, Type.HOTEL)) {
			Intent intent = new Intent(context, ConfirmationFragmentActivity.class);
			context.startActivity(intent);
			return true;
		}

		// 13820: Check if a booking is in process at this moment (in case BookingFragmentActivity died)
		else if (BackgroundDownloader.getInstance().isDownloading(BookingFragmentActivity.BOOKING_DOWNLOAD_KEY)) {
			Intent intent = new Intent(context, BookingFragmentActivity.class);
			context.startActivity(intent);
			return true;
		}

		else if (ExpediaBookingApp.useTabletInterface(context)) {
			Intent intent = new Intent(context, SearchFragmentActivity.class);
			context.startActivity(intent);
			return true;
		}
		return false;
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

	private static void sendKillActivityBroadcast(Context context) {
		// Send the kill activity broadcast to ensure the activity backstack is erased
		//
		// Note: All activities must register a LocalBroadcastReceiver on the KILL_ACTIVITY
		// intent to guarantee the backstack is actually erased.
		Intent kill = new Intent();
		kill.setAction(ActivityKillReceiver.BROADCAST_KILL_ACTIVITY_INTENT);
		LocalBroadcastManager.getInstance(context).sendBroadcast(kill);
	}
}
