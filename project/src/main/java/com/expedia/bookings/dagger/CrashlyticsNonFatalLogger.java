package com.expedia.bookings.dagger;

import org.jetbrains.annotations.NotNull;

import com.crashlytics.android.Crashlytics;
import com.expedia.bookings.services.NonFatalLoggerInterface;

class CrashlyticsNonFatalLogger implements NonFatalLoggerInterface {
	@Override
	public void logException(@NotNull Exception e) {
		Crashlytics.logException(e);
	}
}
