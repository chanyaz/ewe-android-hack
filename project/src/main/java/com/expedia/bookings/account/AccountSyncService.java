package com.expedia.bookings.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AccountSyncService extends Service {

	private static final Object SYNC_ADAPTER_LOCK = new Object();
	private static AccountSyncAdapter sSyncAdapter = null;

	@Override
	public void onCreate() {
		super.onCreate();

		synchronized (SYNC_ADAPTER_LOCK) {
			if (sSyncAdapter == null) {
				sSyncAdapter = new AccountSyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return sSyncAdapter.getSyncAdapterBinder();
	}

}
