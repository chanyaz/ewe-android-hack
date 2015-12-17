package com.expedia.bookings.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class GlobalSettingsUtils {
	@TargetApi(17)
	public static float getAnimatorDurationScale(Context context) {
		if (Build.VERSION.SDK_INT >= 17) {
			return Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f);
		}
		else {
			return Settings.System.getFloat(context.getContentResolver(), Settings.System.ANIMATOR_DURATION_SCALE, 1.0f);
		}
	}
}
