package com.expedia.bookings.account;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.Log;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;

public class AccountSyncAdapter extends AbstractThreadedSyncAdapter {

	public AccountSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public AccountSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
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

				//TODO: Do we want to update already selected travelers? Probably not.. but maybe?
			}
		}

	}

}
