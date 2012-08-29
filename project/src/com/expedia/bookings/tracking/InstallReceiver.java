package com.expedia.bookings.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallReceiver extends BroadcastReceiver {
	private final static String RECEIVER_FORWARDED = "RECEIVER_FORWARDED";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getBooleanExtra(RECEIVER_FORWARDED, false)) {
			return;
		}

		if (intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
			intent.setComponent(null);
			intent.putExtra(RECEIVER_FORWARDED, true);

			context.sendOrderedBroadcast(intent, null);
		}
	}
}