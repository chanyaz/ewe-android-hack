package com.expedia.bookings.tracking;

import java.net.URLEncoder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.mobiata.android.Log;

public class MdotmReceiver extends BroadcastReceiver {
	public String postBackUrl = "";
	public String deviceId = "0";
	public String androidId = "0";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("Tracking install with MdotmReceiver...");

		String referrer = "";
		try {
			referrer = intent.getStringExtra("referrer");
			if (referrer == null) {
				referrer = "null_referrer_found";
			}
		}
		catch (Exception e) {
			Log.w("Could not retrieve referrer for MdotM", e);
			referrer = "exception_found_retrieving_referrer";
		}

		try {
			final TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			deviceId = telephonyManager.getDeviceId();
		}
		catch (Exception e) {
			Log.w("Could not retrieve deviceId for MdotM", e);
			deviceId = "0";
		}

		try {
			androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		}
		catch (Exception e) {
			Log.w("Could not retrieve androidId for MdotM", e);
			androidId = "0";
		}

		String packageName = "";
		Package packageObj = this.getClass().getPackage();
		if (packageObj == null) {
			packageName = "null_package";
		}
		else {
			packageName = packageObj.getName();
		}

		if (deviceId == null) {
			deviceId = "0";
		}

		if (androidId == null) {
			androidId = "0";
		}

		postBackUrl = "http://ads.mdotm.com/ads/receiver.php?referrer=" + URLEncoder.encode(referrer) + "&package="
				+ URLEncoder.encode(packageName) + "&deviceid=" + URLEncoder.encode(deviceId) + "&androidid="
				+ URLEncoder.encode(androidId);
		makePostBack.start();
	}

	private Thread makePostBack = new Thread() {
		public void run() {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(postBackUrl);
				httpClient.execute(httpGet);
			}
			catch (Exception e) {
				Log.w("Could not track install for MdotM", e);
			}
		}
	};
}
