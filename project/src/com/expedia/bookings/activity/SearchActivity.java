package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.BackgroundDownloader;

/**
 * This is a routing Activity that points users towards either the phone or
 * tablet version of this app.
 * 
 * It is named SearchActivity for historical reasons; this was the original
 * starting Activity for older versions of EH, and we don't want to break any
 * future installs (which may have setup quick links to EH).
 * 
 * http://android-developers.blogspot.com/2011/06/things-that-cannot-change.html
 * 
 */
public class SearchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Track the app loading
		Tracker.trackAppLoading(this);
		AdTracker.trackLaunch();

		if (NavUtils.skipLaunchScreenAndStartEHTablet(this)) {
			// Note: 2.0 will not support launch screen nor Flights on tablet ergo send user to EH tablet
		}
		else {
			// On default, go to launch screen
			NavUtils.goToLaunchScreen(this);
		}

		// Finish this Activity after routing
		finish();
	}

}
