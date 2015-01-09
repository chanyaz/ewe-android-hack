package com.expedia.bookings.tracking;

import android.content.Context;
import android.os.AsyncTask;

import com.expedia.bookings.server.ExpediaServices;

public class AdImpressionTracking {
	public static void trackAdClickOrImpression(final Context context, final String url) {
		new AsyncTask<String, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(String... params) {
				ExpediaServices services = new ExpediaServices(context);
				boolean success = services.trackTravelAd(url);
				return success;
			}
		}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}
}
