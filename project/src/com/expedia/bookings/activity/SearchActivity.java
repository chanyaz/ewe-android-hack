package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.mobiata.android.BackgroundDownloader;

/**
 * This is a routing Activity that points users towards either the phone or
 * tablet version of this app.
 * 
 * It is named SearchActivity for historical reasons; this was the original
 * starting Activity for older versions of EH, and we don't want to break any
 * future installs (which may have setup quick links to EH).
 */
public class SearchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Track the app loading
		Tracker.trackAppLoading(this);
		AdTracker.trackLaunch();

		// Determine where to route the app
		Class<? extends Activity> routingTarget;

		// #7090: First, check to see if the user last confirmed a booking.  If that is the case,
		//        then we should forward the user to the ConfirmationActivity
		if (ConfirmationUtils.hasSavedConfirmationData(this)) {
			routingTarget = ConfirmationFragmentActivity.class;
		}

		// 13820: Check if a booking is in process at this moment (in case BookingInfoActivity died)
		else if (BackgroundDownloader.getInstance().isDownloading(BookingInfoActivity.BOOKING_DOWNLOAD_KEY)) {
			routingTarget = BookingInfoActivity.class;
		}

		// 13820: Check if a booking is in process at this moment (in case BookingFragmentActivity died)
		else if (BackgroundDownloader.getInstance().isDownloading(BookingFragmentActivity.BOOKING_DOWNLOAD_KEY)) {
			routingTarget = BookingFragmentActivity.class;
		}

		// Note: 2.0 will not support launch screen nor Flights on tablet ergo send user to EH tablet
		else if (ExpediaBookingApp.useTabletInterface(this)) {
			routingTarget = SearchFragmentActivity.class;
		}

		// Note: User is on a phone and app is launching "normally" to launch screen
		else {
			routingTarget = LaunchActivity.class;
		}

		Intent intent = new Intent(this, routingTarget);

		// Start the routing intent
		startActivity(intent);

		// Finish this Activity after routing
		finish();
	}

}
