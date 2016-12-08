package com.expedia.bookings.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DebugMenuFactory {
	public static DebugMenu newInstance(@NonNull Activity hostActivity, @Nullable Class<? extends Activity> settingsActivityClass) {
		return new DebugMenuImpl(hostActivity, settingsActivityClass);
	}
}
