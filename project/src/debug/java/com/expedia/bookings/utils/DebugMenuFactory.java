package com.expedia.bookings.utils;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.expedia.bookings.preference.ExpediaBookingPreferenceActivity;

public class DebugMenuFactory {
	public static DebugMenu newInstance(@NonNull Activity hostActivity) {
		return new DebugMenuImpl(hostActivity, ExpediaBookingPreferenceActivity.class);
	}
}
