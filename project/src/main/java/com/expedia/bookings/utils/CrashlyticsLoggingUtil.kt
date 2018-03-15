package com.expedia.bookings.utils

import com.crashlytics.android.Crashlytics
import com.expedia.bookings.activity.ExpediaBookingApp

object CrashlyticsLoggingUtil {

    fun logWhenNotAutomation(message: String) {
        if (!ExpediaBookingApp.isAutomation()) {
            Crashlytics.log(message)
        }
    }
}
