package com.expedia.bookings.utils;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.mobiata.android.Log;

/**
 * This is a Util class for dealing with the Calendar API
 * The Calendar API is for adding/manipulating events on a users google cal (or other system calendar)
 */
public class CalendarAPIUtils {
	/**
	 * Does this device support the calendar api?
	 * @param context
	 * @return
	 */
	public static boolean deviceSupportsCalendarAPI(Context context) {
		Uri data = null;
		try {
			Class<?> clz = Class.forName("android.provider.CalendarContract");
			for (Class<?> c : clz.getDeclaredClasses()) {
				if (c.getName().equals("android.provider.CalendarContract$Events")) {
					Field f = c.getField("CONTENT_URI");
					data = (Uri) f.get(null);
				}
			}
		}
		catch (Exception e) {
			Log.d("Reflection error trying to look for calendar support", e);
		}
		finally {
			if (data == null) {
				Log.d("Device does not support calendaring.");
				return false;
			}
		}

		Intent dummy = new Intent(Intent.ACTION_INSERT);
		dummy.setData(data);

		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(dummy, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
}
