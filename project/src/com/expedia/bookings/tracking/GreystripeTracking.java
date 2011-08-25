package com.expedia.bookings.tracking;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

public class GreystripeTracking {

	private final static String APP_ID = "100003591";

	public static void trackDownload(Context context) {
		try {
			// only track the download once, by keeping track of whether we've tracked 
			// already via a SharedPreferences setting
			SharedPreferences prefs = context.getSharedPreferences("gsdnld", Context.MODE_PRIVATE);
			if (!prefs.getBoolean("tracked", false)) {
				// get the device IMEI, minus special characters
				TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				String imei = tm.getDeviceId().replace("\\W", "");

				// request the tracking url
				String url = String.format("http://ads2.greystripe.com/AdBridgeServer/" +
						"track.htm?did=%s&appid=%s&action=dl", imei, APP_ID);
				HttpGet get = new HttpGet(url);
				HttpResponse resp = (new DefaultHttpClient()).execute(get);

				// process the response (any failure throws an exception)
				(new BasicResponseHandler()).handleResponse(resp);

				// keep track of the successful tracking request, to avoid redundant requests
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("tracked", true);
				SettingUtils.commitOrApply(editor);
			}
		}
		catch (Exception e) {
			Log.w("Greystripe download tracking failed: " + e);
		}
	}
}
