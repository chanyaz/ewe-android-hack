package com.expedia.bookings.utils;

import android.app.Activity;
import android.support.annotation.NonNull;

public class DebugMenuFactory {
	public static DebugMenu newInstance(@NonNull Activity hostActivity) {
		return new NoOpDebugMenuImpl();
	}
}
