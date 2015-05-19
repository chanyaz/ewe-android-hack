package com.expedia.bookings.activity;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.expedia.bookings.utils.NavUtils;
import com.facebook.AppEventsLogger;

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
public class RouterActivity extends Activity {

	private Context mContext;

	private static final String OPENED_FROM_WIDGET = "OPENED_FROM_WIDGET";

	public static Intent createIntent(Context context, boolean openedFromWidget) {
		Intent intent = new Intent(context, RouterActivity.class);
		if (openedFromWidget) {
			intent.putExtra(OPENED_FROM_WIDGET, true);
		}
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;

		// Track the app loading
		OmnitureTracking.trackAppLoading(mContext);
		AdTracker.trackLaunch(mContext);

		// Update data
		ItineraryManager.getInstance().startSync(false, false, true);

		//Hi Facebook!
		facebookInstallTracking();

		cleanupOldCookies();

		if (NavUtils.skipLaunchScreenAndStartEHTablet(this)) {
			// Note: 2.0 will not support launch screen nor Flights on tablet ergo send user to EH tablet
		}
		else {
			boolean forceShowWaterfall = false;
			if (getIntent().getBooleanExtra(OPENED_FROM_WIDGET, false)) {
				// We're being ultra-safe here and only sending a kill broadcast if opened from
				// the widget.  This is so that the widget *always* opens to the launch screen.
				NavUtils.sendKillActivityBroadcast(this);

				// If opened from widget, we want to always show the reverse waterfall
				forceShowWaterfall = true;
			}

			// On default, go to launch screen
			NavUtils.goToLaunchScreen(this, forceShowWaterfall);
		}

		// Finish this Activity after routing
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
	}

	/**
	 * Tell facebook we installed the app every time we launch!
	 * This is asynchronous, and after we get a success message back from FB this call no longer does anything at all.
	 */
	private void facebookInstallTracking() {
		AppEventsLogger.activateApp(this, ExpediaServices.getFacebookAppId(this));
	}

	private static final String COOKIE_FILE_V2 = "cookies-2.dat";
	private static final String COOKIE_FILE_V3 = "cookies-3.dat";
	private void cleanupOldCookies() {
		String[] files = new String[]{
			COOKIE_FILE_V2,
			COOKIE_FILE_V3,
		};
		// Nuke app data if old files exist
		// Delete old cookie files
		boolean cleanedSomething = false;
		for (String file : files) {
			File old = getFileStreamPath(file);
			if (old.exists()) {
				cleanedSomething = true;
				old.delete();
			}
		}

		if (cleanedSomething) {
			ClearPrivateDataUtil.clear(this);
		}
	}

}
