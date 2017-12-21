package com.expedia.bookings.utils;

import android.content.Context;
import android.provider.Settings;

public class GlobalSettingsUtils {
	public static float getAnimatorDurationScale(Context context) {
		return Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f);
	}
}
