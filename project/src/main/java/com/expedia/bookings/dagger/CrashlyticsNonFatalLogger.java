package com.expedia.bookings.dagger;

import org.jetbrains.annotations.NotNull;

import com.crashlytics.android.Crashlytics;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.services.NonFatalLoggerInterface;

class CrashlyticsNonFatalLogger implements NonFatalLoggerInterface {
	@Override
	public void logException(@NotNull Exception e) {
		if (!ExpediaBookingApp.isAutomation()) {
			Crashlytics.logException(e);
		}
	}
}
