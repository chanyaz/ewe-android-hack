package com.expedia.bookings.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.expedia.bookings.R;
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
		//This is only used for logging out users, so if the app thinks we are logged out, then we are good.
		if (User.isLoggedInOnDisk(context)) {
			String accountType = context.getString(R.string.expedia_account_type_identifier);
			AccountManager manager = AccountManager.get(context);
			Account[] accounts = manager.getAccountsByType(accountType);
			if (accounts == null || accounts.length == 0) {
				Log.d("AccountsChangedBroadcastReceiver signing out user.");
				User.signOut(context);
				//We start a sync, so that any listeners will get notified of syncfinish.
				ItineraryManager.getInstance().startSync(true);
			}
		}
	}

}
