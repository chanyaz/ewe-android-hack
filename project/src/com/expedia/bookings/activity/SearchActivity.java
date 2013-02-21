package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.FlurryTracking;
import com.expedia.bookings.tracking.GreystripeTracking;
import com.expedia.bookings.tracking.MillennialTracking;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.util.NetUtils;

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

	private Context mContext;

	private static final String OPENED_FROM_WIDGET = "OPENED_FROM_WIDGET";

	public static Intent createIntent(Context context, boolean openedFromWidget) {
		Intent intent = new Intent(context, SearchActivity.class);
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
		trackAppLoading();
		OmnitureTracking.trackAppLoading(mContext);
		AdTracker.trackLaunch();

		if (NavUtils.skipLaunchScreenAndStartEHTablet(this)) {
			// Note: 2.0 will not support launch screen nor Flights on tablet ergo send user to EH tablet
		}
		else {
			// We're being ultra-safe here and only sending a kill broadcast if opened from
			// the widget.  This is so that the widget *always* opens to the launch screen.
			if (getIntent().getBooleanExtra(OPENED_FROM_WIDGET, true)) {
				NavUtils.sendKillActivityBroadcast(this);
			}

			// On default, go to launch screen
			NavUtils.goToLaunchScreen(this);
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

	private void trackAppLoading() {
		// Start a background thread to do conversion tracking
		new Thread(new Runnable() {
			public void run() {
				// Millennial tracking (possibly)
				if (!MillennialTracking.hasTrackedMillennial(mContext) && NetUtils.isOnline(mContext)) {
					MillennialTracking.trackConversion(mContext);
				}

				// GreyStripe tracking
				GreystripeTracking.trackDownload(mContext);

				// Flurry tracking
				FlurryTracking.trackConversion(mContext);
			}
		}).start();
	}

}
