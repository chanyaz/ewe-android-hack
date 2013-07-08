package com.expedia.bookings.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ExpediaAuthenticatationService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return new ExpediaAccountAuthenticator(this).getIBinder();
	}

}
