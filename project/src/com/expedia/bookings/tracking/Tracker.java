package com.expedia.bookings.tracking;

import android.app.Application;
import android.content.Context;

import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.omniture.AppMeasurement;

/**
 * Contains specific events to track.
 */
public class Tracker {

	// The SettingUtils key for the last version tracked
	private static final String TRACK_VERSION = "tracking_version";

	public static void trackAppLoading(final Context context) {
		Log.d("Tracking \"App.Loading\" pageLoad...");

		AppMeasurement s = new AppMeasurement((Application) context.getApplicationContext());

		TrackingUtils.addStandardFields(context, s);

		s.pageName = "App.Loading";

		// Determine if this is a new install, an upgrade, or just a regular launch
		String trackVersion = SettingUtils.get(context, TRACK_VERSION, null);
		String currentVersion = AndroidUtils.getAppVersion(context);

		// Start a background thread to do conversion tracking
		new Thread(new Runnable() {
			public void run() {
				// Millennial tracking (possibly)
				if (!MillennialTracking.hasTrackedMillennial(context) && NetUtils.isOnline(context)) {
					MillennialTracking.trackConversion(context);
				}

				// GreyStripe tracking
				GreystripeTracking.trackDownload(context);
			}
		}).start();

		boolean save = false;
		if (trackVersion == null) {
			// New install
			s.events = "event28";
			save = true;
		}
		else if (!trackVersion.equals(currentVersion)) {
			// App was upgraded
			s.events = "event29";
			save = true;
		}
		else {
			// Regular launch
			s.events = "event27";
		}

		if (save) {
			// Save new data
			SettingUtils.save(context, TRACK_VERSION, currentVersion);
		}

		// Send the tracking data
		s.track();
	}

}
