package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.mobiata.android.util.SettingUtils;

public class BookingSuppressionUtils {

	public static boolean shouldSuppressFinalBooking(Context context, int preferenceResId) {
		if (BuildConfig.USABILITY) {
			return true;
		}
		return BuildConfig.DEBUG && SettingUtils.get(context, preferenceResId, true);
	}

}
