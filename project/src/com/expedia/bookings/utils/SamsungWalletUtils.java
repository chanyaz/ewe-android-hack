package com.expedia.bookings.utils;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.mobiata.android.util.AndroidUtils;

public class SamsungWalletUtils {
	public static final String SAMSUNG_WALLET_PACKAGE_NAME = "com.sec.android.app.samsungapps";

	public static final String CHECK_TICKET_RESULT = "com.sample.partners.action.CHECK_TICKET_RESULT";

	public static final int RESULT_TICKET_EXISTS = 100;
	public static final int RESULT_TICKET_NOT_FOUND = 200;
	public static final int RESULT_NETWORK_ERROR = 300;
	public static final int RESULT_INTERNAL_ERROR = 400;
	public static final int RESULT_LIMIT_EXCEEDED = 500;

	public interface Callback {
		public void onResult(int result);
	}

	public static boolean isAvailable(Context context) {
		return AndroidUtils.isPackageInstalled(context, SAMSUNG_WALLET_PACKAGE_NAME);
	}

	public static Intent checkTicketIntent(Context context, String ticketId) {
		Intent intent = new Intent(); intent.setAction("com.sec.android.wallet.action.CHECK_TICKET");

		intent.putExtra("TICKET_ID", ticketId);
		intent.putExtra("BOUNCE_ID", ""+System.currentTimeMillis());
		intent.putExtra("RESULT_ACTION", CHECK_TICKET_RESULT);

		return intent;
	}

	public static IntentFilter checkTicketFilter(Context context) {
		IntentFilter filter = new IntentFilter(CHECK_TICKET_RESULT);
		return filter;
	}

	private static class SamsungWalletReceiver extends BroadcastReceiver {
		private Callback mCallback;

		public SamsungWalletReceiver(Callback callback) {
			mCallback = callback;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (CHECK_TICKET_RESULT.equals(intent.getAction())) {
				mCallback.onResult(0);
				context.unregisterReceiver(this);
			}
		}
	}

	public static void checkTicket(Context context, final Callback callback, String ticketId) {
		Intent intent = checkTicketIntent(context, ticketId);
		IntentFilter filter = checkTicketFilter(context);
		SamsungWalletReceiver receiver = new SamsungWalletReceiver(callback);

		context.registerReceiver(receiver, filter);
		context.sendBroadcast(intent);
	}

	public static Intent downloadTicketIntent(Context context, String ticketId) {
		Intent intent = new Intent();
		ComponentName comp = new ComponentName("com.sec.android.wallet",
				"com.sec.android.wallet.ui.activity.ticket.ExternalTicketDownloadActivity");
		intent.setComponent(comp);

		intent.putExtra("TICKET_ID", ticketId);
		intent.putExtra("BOUNCE_ID", ""+System.currentTimeMillis());
		intent.putExtra("RESULT_ACTION", "com.sample.partners.action.DOWNLOAD_TICKET_RESULT");

		return intent;
	}

}

