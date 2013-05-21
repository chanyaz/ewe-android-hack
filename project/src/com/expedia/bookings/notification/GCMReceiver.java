package com.expedia.bookings.notification;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

/**
 * This class is here to override getGCMIntentServiceClassName and thus
 * allow us to move the GCMIntentService to a package other than com.expedia.bookings
 * More Info:http://dexxtr.com/post/28188228252/rename-or-change-package-of-gcmintentservice-class
 */
public class GCMReceiver extends GCMBroadcastReceiver {
	@Override
	protected String getGCMIntentServiceClassName(Context context) {
		return "com.expedia.bookings.notification.GCMIntentService";
	}
}
