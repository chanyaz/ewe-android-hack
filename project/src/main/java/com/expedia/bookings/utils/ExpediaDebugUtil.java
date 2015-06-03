package com.expedia.bookings.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.text.TextUtils;
import android.widget.Toast;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

/**
 * This is a utility for debug build related tasks
 */
public class ExpediaDebugUtil {

	public static boolean isEBToolApkInstalled(Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			pm.getPackageInfo("com.expedia.tool", PackageManager.GET_ACTIVITIES);
			return true;
		}
		catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * Notifies QA with a toast that a memory crash has been saved to disk.
	 */
	public static void showExpediaDebugToastIfNeeded(Context context) {
		if (BuildConfig.DEBUG
				&& SettingUtils.get(context, context.getString(R.string.preference_debug_notify_oom_crash), false)) {
			Toast.makeText(context, "Memory crash. Open ExpediaDebug for upload.", Toast.LENGTH_LONG).show();
			SettingUtils.save(context, context.getString(R.string.preference_debug_notify_oom_crash), false);
		}
	}

	/**
	 * Returns a fake location (as set in the dev preferences) if it's available, otherwise returns null.
	 */
	public static Location getFakeLocation(Context context) {
		if (BuildConfig.DEBUG) {
			String fakeLatLng = SettingUtils.get(context, context.getString(R.string.preference_fake_current_location),
					"");
			if (!TextUtils.isEmpty(fakeLatLng)) {
				String[] split = fakeLatLng.split(",");
				if (split.length == 2) {
					Log.d("Using fake location!");
					Location location = new Location("fakeProvider");
					location.setLatitude(Double.parseDouble(split[0]));
					location.setLongitude(Double.parseDouble(split[1]));
					return location;
				}
			}
		}

		return null;
	}
}
