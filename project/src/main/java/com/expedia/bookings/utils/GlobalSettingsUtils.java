package com.expedia.bookings.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class GlobalSettingsUtils {
	@TargetApi(17)
	public static float getAnimatorDurationScale(Context context) {
		try {
			float result;
			if (Build.VERSION.SDK_INT >= 17) {
				result = Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE);
			}
			else {
				result = Settings.System.getFloat(context.getContentResolver(), Settings.System.ANIMATOR_DURATION_SCALE);
			}
			return result;
		}
		catch (Throwable t) {
			throw new RuntimeException("Critical failure", t);
		}
	}
}
