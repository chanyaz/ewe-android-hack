package com.expedia.bookings.gear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GearRestartReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, GearAccessoryProviderService.class);
		context.startService(serviceIntent);
	}
}
