package com.expedia.bookings.notification;

import com.google.android.gcm.GCMBaseIntentService;

import android.content.Context;
import android.content.Intent;

public class GCMIntentService extends GCMBaseIntentService {

	@Override
	public void onRegistered(Context context, String regId) {

	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onRecoverableError(Context context, String errorId) {
		return false;
	}

}
