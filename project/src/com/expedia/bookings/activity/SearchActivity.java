package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.amobee.agency.tracking.AmobeeReceiver;
import com.amobee.agency.tracking.AmobeeReceiver.Goal;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.utils.ConfirmationUtils;

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
		AmobeeReceiver.amobeeTracking(Goal.LAUNCH, getApplicationContext(), "ExpediaHotelsAndroidPhone");

		// #7090: First, check to see if the user last confirmed a booking.  If that is the case,
		//        then we should forward the user to the ConfirmationActivity
		boolean hasSavedConfirmationData = ConfirmationUtils.hasSavedConfirmationData(this);

		// Determine where to route the app
		// #11076 - for Android 3.0, we still use the phone version of the app due to crippling bugs.
		Class<? extends Activity> routingTarget;
		if (ExpediaBookingApp.useTabletInterface(this)) {
			routingTarget = (hasSavedConfirmationData) ? ConfirmationFragmentActivity.class
					: SearchFragmentActivity.class;
		}
		else {
			routingTarget = (hasSavedConfirmationData) ? ConfirmationActivity.class : PhoneSearchActivity.class;
		}

		Intent intent = new Intent(this, routingTarget);

		// Start the routing intent
		startActivity(intent);

		// Finish this Activity after routing
		finish();
	}

}
