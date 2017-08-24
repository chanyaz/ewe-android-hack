package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class ActivityKillReceiver extends BroadcastReceiver {

	public static final String BROADCAST_KILL_ACTIVITY_INTENT = "com.expedia.bookings.activity.KILL";

	private final Activity mActivity;

	public ActivityKillReceiver(Activity activity) {
		mActivity = activity;
	}

	public void onCreate() {
		// Register a (Local)BroadcastReceiver to listen for the KILL_ACTIVITY intent
		IntentFilter intentFilter = new IntentFilter(BROADCAST_KILL_ACTIVITY_INTENT);
		LocalBroadcastManager.getInstance(mActivity).registerReceiver(this, intentFilter);
	}

	public void onDestroy() {
		LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(this);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (BROADCAST_KILL_ACTIVITY_INTENT.equals(intent.getAction())) {
			mActivity.finish();
		}
	}

}
