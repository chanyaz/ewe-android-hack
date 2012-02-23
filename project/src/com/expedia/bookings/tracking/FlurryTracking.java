package com.expedia.bookings.tracking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.provider.Settings.Secure;

import com.mobiata.android.Log;
import com.mobiata.android.net.AndroidHttpClient;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * Does Flurry conversion tracking.
 * 
 * See more details here: https://team.mobiata.com/wiki/Flurry
 * 
 * Inspired by redmine #11533
 */
public class FlurryTracking {

	private static String PREF_TRACKED = "Flurry.Tracked";

	public static void trackConversion(Context context) {
		if (!SettingUtils.get(context, PREF_TRACKED, false)) {
			Log.i("Tracking Flurry conversion.");

			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			params.add(new BasicNameValuePair("installTime", Calendar.getInstance().getTimeInMillis() + ""));
			params.add(new BasicNameValuePair("udid", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID)));
			params.add(new BasicNameValuePair("deviceType", "android"));
			params.add(new BasicNameValuePair("appName", "expedia_hotels"));

			HttpGet get = NetUtils.createHttpGet("http://flurry.mobiata.com/service", params);
			AndroidHttpClient client = AndroidHttpClient.newInstance("Mobiata/1.0");
			try {
				HttpResponse response = client.execute(get);
				Log.i("Flurry tracking response: " + NetUtils.toString(response.getEntity()));
				SettingUtils.save(context, PREF_TRACKED, true);
			}
			catch (IOException e) {
				Log.w("Error tracking Flurry conversion", e);
			}
			finally {
				client.close();
			}
		}
	}
}
