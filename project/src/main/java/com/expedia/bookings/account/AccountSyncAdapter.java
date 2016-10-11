package com.expedia.bookings.account;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.Log;

/**
 * This is the sync adapter we use to update our User.
 */
public class AccountSyncAdapter extends AbstractThreadedSyncAdapter {

	public AccountSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
			SyncResult syncResult) {
		Context context = getContext();
		if (User.isLoggedIn(context)) {
			ExpediaServices services = new ExpediaServices(context);
			SignInResponse results = services.signIn(ExpediaServices.F_FLIGHTS | ExpediaServices.F_HOTELS);
			if (results == null || results.hasErrors()) {
				Log.e("Services.signIn has failed in onPerformSync. No data will be updated.");
			}
			else {
				// Update our user object
				User user = results.getUser();
				user.save(context);
				Db.setUser(user);
				Log.d("AccountSyncAdapter.onPerformSync has completed successfully, updating the User object in Db.");
			}
		}
	}
}
