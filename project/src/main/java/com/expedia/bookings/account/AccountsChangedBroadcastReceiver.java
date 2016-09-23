package com.expedia.bookings.account;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.mobiata.android.Log;

/**
 * This receiver listens for LOGIN_ACCOUNTS_CHANGED_ACTION broadcast,
 * and ensures that if there are no Expedia accounts, that our user is
 * logged out.
 */
public class AccountsChangedBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("AccountsChangedBroadcastReceiver called");
		if (ExpediaBookingApp.isAutomation()) {
			Log.d("AccountsChangedBroadcastReceiver automation so ignoring broadcast");
			return;
		}

		if (!User.isLoggedInToAccountManager(context)) {
			Log.d("AccountsChangedBroadcastReceiver signing out user");
			User.signOut(context);
			//We start a sync so that any listeners will get notified of onSyncFinished()
			ItineraryManager.getInstance().startSync(true);
		}
	}
}
