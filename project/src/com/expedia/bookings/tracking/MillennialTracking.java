package com.expedia.bookings.tracking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.mobiata.android.Log;
import com.mobiata.android.net.AndroidHttpClient;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;

public class MillennialTracking {

	private static final String GOAL_ID = "17178";

	public static boolean hasTrackedMillennial(Context context) {
		SharedPreferences settings = context.getSharedPreferences("MillennialMediaSettings", 0);
		return !settings.getBoolean("firstLaunch", true);
	}

	// Should be launched in a background Thread
	public static void trackConversion(Context context) {
		Log.i("Tracking Millennial conversion.");

		SharedPreferences settings = context.getSharedPreferences("MillennialMediaSettings", 0);
		boolean isFirstLaunch = settings.getBoolean("firstLaunch", true);
		if (isFirstLaunch)
		{
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("firstLaunch", false);
			SettingUtils.commitOrApply(editor);
		}

		// Check if we have internet
		if (!NetUtils.isOnline(context)) {
			Log.w("No internet available for Millennial tracking.");
		}
		else {
			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			params.add(new BasicNameValuePair("goalId", GOAL_ID));
			params.add(new BasicNameValuePair("auid", getMillennialId(context)));
			params.add(new BasicNameValuePair("firstlaunch", (isFirstLaunch) ? "1" : "0"));

			HttpGet get = NetUtils.createHttpGet("http://cvt.mydas.mobi/handleConversion", params);

			AndroidHttpClient client = AndroidHttpClient.newInstance("Mobiata/1.0");
			try {
				client.execute(get, new ResponseHandler<Object>() {
					public Object handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
						Log.i("Millennial tracking response code: " + response.getStatusLine().getStatusCode());
						return null;
					}
				});
			}
			catch (IOException e) {
				Log.w("Error trying to contact Millennial conversion tracking.", e);
			}
			finally {
				client.close();
			}
		}
	}

	private static String getMillennialId(Context context) {
		String id = null;
		TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
		if (tm != null)
		{
			id = tm.getDeviceId();
		}
		if (id == null || id.length() == 0) {
			id = Settings.Secure.getString(context.getContentResolver(), "android_id");
		}
		if (id == null || id.length() == 0) {
			id = "UNKNOWN";
		}
		return id;
	}
}
