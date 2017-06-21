package com.expedia.bookings.tracking;

import java.net.URLDecoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

public class InstallReceiver extends BroadcastReceiver {
	private final static String RECEIVER_FORWARDED = "RECEIVER_FORWARDED";
	private final static String MAT_DEEPLINK_PARAM = "mat_deeplink=";
	public final static String REWARDS_USER_NAME = "REWARDS_USER_NAME";
	public final static String REFERRED_BY = "REFERRED_BY";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("Install receiver called.");
		if (intent.getBooleanExtra(RECEIVER_FORWARDED, false)) {
			return;
		}
		if (intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
			String referrer = intent.getStringExtra("referrer");
			Log.d("Referrer value from install: " + referrer);
			if (referrer != null) {
				try {
					SettingUtils.save(context, REFERRED_BY, referrer);
					int deeplinkStart = referrer.indexOf(MAT_DEEPLINK_PARAM);
					if (deeplinkStart != -1) {
						deeplinkStart += MAT_DEEPLINK_PARAM.length();
						int deeplinkEnd = referrer.indexOf("&", deeplinkStart);
						String deeplink;
						if (deeplinkEnd == -1) {
							deeplink = referrer.substring(deeplinkStart);
						}
						else {
							deeplink = referrer.substring(deeplinkStart, deeplinkEnd);
						}

						deeplink = URLDecoder.decode(deeplink, "UTF-8");
						Log.d("Referrer deeplink:" + deeplink);

						if (Strings.isNotEmpty(deeplink)) {
							Intent deepLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deeplink));
							deepLinkIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(deepLinkIntent);
						}
					}
				}
				catch (Exception e) {
					Log.e("Referrer deeplink error:" + e.getMessage());
				}
			}

			intent.setComponent(null);
			intent.setPackage(context.getPackageName());
			intent.putExtra(RECEIVER_FORWARDED, true);

			context.sendBroadcast(intent);
		}
	}
}
