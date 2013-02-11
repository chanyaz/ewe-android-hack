package com.expedia.bookings.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OmnitureReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
			OmnitureTracking.trackAppInstallCustom(context);
			OmnitureTracking.trackGooglePlayReferralLink(context, intent);
		}
	}
}
